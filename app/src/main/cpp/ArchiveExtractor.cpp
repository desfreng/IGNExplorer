#include "ArchiveExtractor.h"

#include <utility>
#include <cassert>
#include "ImageWorker.h"
#include "SevenZ.h"

static const int poolSize = static_cast<int>(std::thread::hardware_concurrency()) - 1;

ArchiveExtractor::ArchiveExtractor(std::string archive_path, std::string file_dir,
                                   ProgressStatus *const status)
        : archive_path(std::move(archive_path)), file_dir(std::move(file_dir)),
          status(status), pool(poolSize), running(false) {

    assert(status != nullptr);
    thread = std::thread(&ArchiveExtractor::run, this);
}

void ArchiveExtractor::run() {
    running = true;
    status->beginScan();

    SevenZArchive zArchive(archive_path);
    int nbFile = 0;

    for (const SevenZFileEntry &file: zArchive) {
        std::string ext = file.fileName.extension().string();
        if (ext == ".tif") {
            nbFile++;
        }
    }

    status->beginExtraction(nbFile);

    for (const SevenZFileEntry &file: zArchive) {
        std::string ext = file.fileName.extension().string();
        if (ext != ".tif") {
            continue;
        }

        std::vector<unsigned char> fileData;
        zArchive.getData(file, fileData);
        ImageWorker worker(*this, fileData, status, file.fileName.filename());
        pool.push(worker);
    }

    pool.stop(true);
    std::lock_guard<std::mutex> lockGuard(resMutex);
    status->extractionDone(static_cast<int>(res.size()));
    running = false;
}

bool ArchiveExtractor::checkGeometry(const PixelScale &scale,
                                     uint32_t width, uint32_t height,
                                     const TileRecord &record) {
    std::lock_guard<std::mutex> lockGuard(geoMutex);
    return geometry.compatible(scale, width, height, record);
}

void ArchiveExtractor::addTileResult(TileRecord &&tile) {
    std::lock_guard<std::mutex> lockGuard(resMutex);
    res.push_back(std::move(tile));
}

std::string ArchiveExtractor::getFileDir() const {
    return file_dir;
}

jobject ArchiveExtractor::getJavaResult(Interface i, JNIEnv *env) {
    if (!thread.joinable()) {
        throw std::runtime_error("What happened ?! Cannot join thread.");
    }

    thread.join();

    if (running) {
        throw std::runtime_error("Thread is running. Can't get Extraction Result");
    }

    auto s = static_cast<jsize>(res.size());
    jobjectArray arr = env->NewObjectArray(s, i.tileRecordCls(), nullptr);

    if (arr == nullptr) {
        throw std::runtime_error("Error allocating object array");
    }

    for (int j = 0; j < res.size(); ++j) {
        env->SetObjectArrayElement(arr, j, res[j].toJava(i, env));
    }

    jobject j_geo = geometry.toJava(i, env);
    return env->NewObject(i.extractionResultCls(), i.extractionResultInit(), j_geo, arr);
}