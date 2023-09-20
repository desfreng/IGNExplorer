#include "TileGeometry.h"
#include <optional>
#include <cmath>

jobject TileGeometry::toJava(Interface c, JNIEnv *env) const {
    if (!initialized) {
        throw std::runtime_error("Trying to export uninitialized Image Geometry.");
    }

    return env->NewObject(c.imageGeometryCls(), c.imageGeometryInit(),
                          scaleX, scaleY, width, height, lambertXOrigin, lambertYOrigin);
}

std::optional<int> convertToInt(uint32_t value) {
    if (value < std::numeric_limits<int>::max()) {
        return {value};
    } else {
        return {};
    }
}

bool TileGeometry::compatibleOrigin(const TileRecord &record) const {
    double dx = abs(lambertXOrigin - record.lambertXOrigin());
    double dy = abs(lambertYOrigin - record.lambertYOrigin());

    double nX = dx / tileLambertX();
    double nY = dy / tileLambertY();

     return std::trunc(nX) == nX && std::trunc(nY) == nY;
}

bool TileGeometry::compatible(const PixelScale &scale, uint32_t w, uint32_t h,
                              const TileRecord &tileRecord) {

    auto tileW = convertToInt(w);
    auto tileH = convertToInt(h);

    if (!tileH.has_value() || !tileW.has_value()) {
        return false;
    }

    if (!initialized) {
        scaleX = scale.x;
        scaleY = scale.y;

        height = tileH.value();
        width = tileW.value();

        lambertXOrigin = tileRecord.lambertXOrigin();
        lambertYOrigin = tileRecord.lambertYOrigin();

        initialized = true;
    }


    return h == height && w == width &&
           scale.x == scaleX && scale.y == scaleY &&
           compatibleOrigin(tileRecord);
}