#include "TileRecord.h"

TileRecord::TileRecord(double xCoord, double yCoord, std::string path)
        : x(xCoord), y(yCoord), p(std::move(path)) {}

jobject TileRecord::toJava(Interface i, JNIEnv *env) const {
    return env->NewObject(i.tileRecordCls(), i.tileRecordInit(),
                          x, y, Interface::to_bytes(p, env));
}

