#ifndef IGNEXPLORER_NATIVE_LIB_H
#define IGNEXPLORER_NATIVE_LIB_H


JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, [[maybe_unused]] void *reserved);

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, [[maybe_unused]] void *reserved);

extern "C" JNIEXPORT jobject JNICALL
Java_fr_desfrene_ignexplorer_ignutils_ArchiveParser_parseArchiveNative(JNIEnv *env,
                                                                       [[maybe_unused]] jclass clazz,
                                                                       jbyteArray j_archive_path,
                                                                       jbyteArray j_file_dir,
                                                                       jobject progress);

#endif //IGNEXPLORER_NATIVE_LIB_H
