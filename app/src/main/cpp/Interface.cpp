#include "Interface.h"


#include <iostream>

int Interface::load(JNIEnv *env) {
    jclass cls;

    // -- TileRecord --
    cls = env->FindClass("fr/desfrene/ignexplorer/ignutils/ArchiveParser$TileRecord");
    tilRec = reinterpret_cast<jclass>(env->NewGlobalRef(cls));
    env->DeleteLocalRef(cls);
    tilRecInit = env->GetMethodID(tilRec, "<init>", "(DD[B)V");

    if (tilRec == nullptr || tilRecInit == nullptr) {
        return JNI_ERR;
    }

    // -- ExtractionResult --
    cls = env->FindClass("fr/desfrene/ignexplorer/ignutils/ArchiveParser$ExtractionResult");
    extrRes = reinterpret_cast<jclass>(env->NewGlobalRef(cls));
    env->DeleteLocalRef(cls);
    extrResInit = env->GetMethodID(extrRes, "<init>",
                                   "(Lfr/desfrene/ignexplorer/ignutils/TileGeometry;[Lfr/desfrene/ignexplorer/ignutils/ArchiveParser$TileRecord;)V");

    if (extrRes == nullptr || extrResInit == nullptr) {
        return JNI_ERR;
    }

    // -- TileGeometry --
    cls = env->FindClass("fr/desfrene/ignexplorer/ignutils/TileGeometry");
    imgGeo = reinterpret_cast<jclass>(env->NewGlobalRef(cls));
    env->DeleteLocalRef(cls);
    imgGeoInit = env->GetMethodID(imgGeo, "<init>", "(FFIIFF)V");

    if (imgGeo == nullptr || imgGeoInit == nullptr) {
        return JNI_ERR;
    }

    // -- NativeException --
    cls = env->FindClass("fr/desfrene/ignexplorer/ignutils/ArchiveParser$NativeException");
    nativeExn = reinterpret_cast<jclass>(env->NewGlobalRef(cls));
    env->DeleteLocalRef(cls);

    if (nativeExn == nullptr) {
        return JNI_ERR;
    }

    // -- ProgressStatus MethodID --
    cls = env->FindClass("fr/desfrene/ignexplorer/ignutils/ProgressStatus");
    pBS = env->GetMethodID(cls, "beginArchiveScan", "()V");
    pBE = env->GetMethodID(cls, "beginExtraction", "(I)V");
    pED = env->GetMethodID(cls, "extractionDone", "(I)V");
    pFB = env->GetMethodID(cls, "fileBegin", "(I[B)V");
    pFD = env->GetMethodID(cls, "fileDone", "(I[B)V");
    pWG = env->GetMethodID(cls, "wrongGeometry", "(I[B)V");
    pNTP = env->GetMethodID(cls, "noTiePoint", "(I[B)V");
    env->DeleteLocalRef(cls);

    if (pBS == nullptr || pBE == nullptr || pED == nullptr ||
        pFB == nullptr || pFD == nullptr || pWG == nullptr ||
        pNTP == nullptr) {
        return JNI_ERR;
    }

    return JNI_OK;
}

void Interface::unload(JNIEnv *env) {
    env->DeleteGlobalRef(tilRec);
    env->DeleteGlobalRef(extrRes);
    env->DeleteGlobalRef(imgGeo);
    env->DeleteGlobalRef(nativeExn);
}

jbyteArray Interface::to_bytes(const std::string &str, JNIEnv *env) {
    auto s = static_cast<jsize>(str.size());
    jbyteArray arr = env->NewByteArray(s);
    if (arr == nullptr) {
        throw std::runtime_error("Error allocating object array");
    }

    env->SetByteArrayRegion(arr, 0, s, reinterpret_cast<const jbyte *>(str.c_str()));
    return arr;
}

std::string Interface::from_bytes(jbyteArray arr, JNIEnv *env) {
    jsize length = env->GetArrayLength(arr);
    jbyte *b = env->GetByteArrayElements(arr, nullptr);
    std::string buffer(b, b + length);
    env->ReleaseByteArrayElements(arr, b, 0);
    std::cout << "DEBUG INTERFACE : " << buffer << std::endl;
    return buffer;
}

void Interface::failure(JNIEnv *env, const std::string &exception) const {
    env->ThrowNew(nativeExn, ("Error : " + exception).c_str());
}
