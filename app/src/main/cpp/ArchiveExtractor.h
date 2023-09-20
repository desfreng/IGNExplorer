#ifndef IGNEXPLORER_ARCHIVE_EXTRACTOR_H
#define IGNEXPLORER_ARCHIVE_EXTRACTOR_H

#include <thread>
#include <vector>

#include "ctpl_stl.h"

#include "ProgressStatus.h"
#include "TileRecord.h"
#include "TileGeometry.h"
#include "ImageWorker.h"


class ArchiveExtractor {
public:
    ArchiveExtractor(std::string archive_path, std::string file_dir,
                     ProgressStatus *const status);

    jobject getJavaResult(Interface i, JNIEnv *env);

protected:
    friend ImageWorker;

    bool checkGeometry(const PixelScale &scale,
                       uint32_t width, uint32_t height,
                       const TileRecord &record);

    void addTileResult(TileRecord &&tile);

    std::string getFileDir() const;

private:
    void run();

    std::thread thread;
    std::atomic_bool running;

    ctpl::thread_pool pool;

    const std::string archive_path;
    const std::string file_dir;
    TileGeometry geometry;
    ProgressStatus *const status;

    std::mutex geoMutex = {};
    std::mutex resMutex = {};
    std::vector<const TileRecord> res = {};
};


#endif //IGNEXPLORER_ARCHIVE_EXTRACTOR_H
