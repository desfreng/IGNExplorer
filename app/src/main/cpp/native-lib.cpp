#include <jni.h>
#include <string>
#include <cassert>

#include "native-lib.h"
#include "Interface.h"
#include "ArchiveExtractor.h"


static Interface i;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, [[maybe_unused]] void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    if (int rc = i.load(env) != JNI_OK) {
        return rc;
    }

    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, [[maybe_unused]] void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return;
    }

    i.unload(env);
}


extern "C" JNIEXPORT jobject JNICALL
Java_fr_desfrene_ignexplorer_ignutils_ArchiveParser_parseArchiveNative(JNIEnv *env,
                                                                       [[maybe_unused]] jclass clazz,
                                                                       jbyteArray j_archive_path,
                                                                       jbyteArray j_file_dir,
                                                                       jobject progress) {
    try {
        std::string archive_path = Interface::from_bytes(j_archive_path, env);
        std::string file_dir = Interface::from_bytes(j_file_dir, env);
        ProgressStatus status(i, progress);

        ArchiveExtractor extractor(archive_path, file_dir, &status);

        while (status.needPolling()) {
            status.poll(env);
        }

        return extractor.getJavaResult(i, env);
    } catch (const std::exception &e) {
        i.failure(env, e.what());
        return nullptr;
    }
}