#ifndef IGN_EXPLORER_TILE_GEOMETRY_H
#define IGN_EXPLORER_TILE_GEOMETRY_H


#include <jni.h>
#include "Interface.h"
#include "TiffImage.h"
#include "TileRecord.h"

class TileGeometry {
public:
    TileGeometry() = default;

    bool compatible(const PixelScale &scale,
                    uint32_t width, uint32_t height,
                    const TileRecord &tileRecord);

    jobject toJava(Interface c, JNIEnv *env) const;

    bool compatibleOrigin(const TileRecord &record) const;

private:
    bool initialized = false;

    double scaleX = 0;
    double scaleY = 0;

    int width = 0;
    int height = 0;

    double lambertXOrigin = 0;
    double lambertYOrigin = 0;

    double tileLambertX() const {
        return width * scaleX;
    }

    double tileLambertY() const {
        return height * scaleY;
    }
};


#endif //IGN_EXPLORER_TILE_GEOMETRY_H
