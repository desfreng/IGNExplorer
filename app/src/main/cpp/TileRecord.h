#ifndef IGN_EXPLORER_TILE_RECORD_H
#define IGN_EXPLORER_TILE_RECORD_H


#include <jni.h>
#include <string>
#include "Interface.h"

class TileRecord {
public:
    TileRecord(double xCoord, double yCoord, std::string path);

    jobject toJava(Interface i, JNIEnv *env) const;

    double lambertXOrigin() const {
        return x;
    }

    double lambertYOrigin() const {
        return y;
    }

private:
    const double x;
    const double y;
    const std::string p;
};


#endif //IGN_EXPLORER_TILE_RECORD_H
