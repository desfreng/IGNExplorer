#ifndef IGNEXPLORER_IMAGE_WORKER_H
#define IGNEXPLORER_IMAGE_WORKER_H

#include <vector>
#include "ProgressStatus.h"

class ArchiveExtractor;

class ImageWorker {

public:
    ImageWorker(ArchiveExtractor &extractor,
                const std::vector<unsigned char> &d,
                ProgressStatus *const status,
                std::string fileName);

    void operator()([[maybe_unused]] int id);

private:
    ArchiveExtractor &extractor;
    std::vector<unsigned char> data;
    ProgressStatus *const status;
    const std::string fileName;
};


#endif //IGNEXPLORER_IMAGE_WORKER_H
