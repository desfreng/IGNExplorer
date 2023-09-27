#include "ImageWorker.h"
#include "ArchiveExtractor.h"
#include <sstream>
#include <utility>

#include "TiffImage.h"
#include "WebPImage.h"

ImageWorker::ImageWorker(ArchiveExtractor &extractor,
                         const std::vector<unsigned char> &d,
                         ProgressStatus *const status,
                         std::string fileName)
        : extractor(extractor), status(status),
          data(d.begin(), d.end()), fileName(std::move(fileName)) {}

void ImageWorker::operator()(int id) {
    status->fileBegin(id, fileName);
    TiffImage tiffImg = TiffImage::openTiffFromMemory(data);
    PixelScale scale = tiffImg.getPixelScale();

    std::vector<TiePoint> tiePoint = tiffImg.getTiePoints();
    if (tiePoint.empty()) {
        status->noTiePoint(id, fileName);
        return;
    }

    double left = tiePoint[0].coordX - tiePoint[0].imgX * scale.x;
    double top = tiePoint[0].coordY + tiePoint[0].imgY * scale.y;

    std::ostringstream os;
    os << extractor.getFileDir() << "/"
       << static_cast<int>(left) << "_" << static_cast<int>(top) << ".webp";
    std::string filePath = os.str();

    TileRecord rec(left, top, filePath);

    if (!extractor.checkGeometry(scale, tiffImg.getImageWidth(), tiffImg.getImageHeight(), rec)) {
        status->wrongGeometry(id, fileName);
        return;
    }

    WebPImage webPImage = WebPImage::fromTiff(tiffImg);
    webPImage.save(filePath);

    extractor.addTileResult(std::move(rec));
    status->fileDone(id, fileName);
}
