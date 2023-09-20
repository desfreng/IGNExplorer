#ifndef IGN_EXPLORER_INTERFACE_H
#define IGN_EXPLORER_INTERFACE_H


#include <jni.h>
#include <string>

class Interface {

public:
    int load(JNIEnv *env);

    void unload(JNIEnv *env);

    void failure(JNIEnv *env, const std::string &exception) const;

    static jbyteArray to_bytes(const std::string &str, JNIEnv *env);

    static std::string from_bytes(jbyteArray arr, JNIEnv *env);

    jclass tileRecordCls() const {
        return tilRec;
    }

    jmethodID tileRecordInit() const {
        return tilRecInit;
    }

    jclass extractionResultCls() const {
        return extrRes;
    }

    jmethodID extractionResultInit() const {
        return extrResInit;
    }

    jclass imageGeometryCls() const {
        return imgGeo;
    }

    jmethodID imageGeometryInit() const {
        return imgGeoInit;
    }

    jmethodID progressBeginScan() const {
        return pBS;
    }

    jmethodID progressBeginExtraction() const {
        return pBE;
    }

    jmethodID progressExtractionDone() const {
        return pED;
    }

    jmethodID progressFileDone() const {
        return pFD;
    }

    jmethodID progressWrongGeometry() const {
        return pWG;
    }

    jmethodID progressNoTiePoint() const {
        return pNTP;
    }

    jmethodID progressFileBegin() const {
        return pFB;
    }

private:
    jclass tilRec;
    jmethodID tilRecInit;
    jclass extrRes;
    jmethodID extrResInit;
    jclass imgGeo;
    jmethodID imgGeoInit;
    jclass nativeExn;

    jmethodID pBS;
    jmethodID pBE;
    jmethodID pED;
    jmethodID pFB;
    jmethodID pFD;
    jmethodID pWG;
    jmethodID pNTP;
};


#endif //IGN_EXPLORER_INTERFACE_H
