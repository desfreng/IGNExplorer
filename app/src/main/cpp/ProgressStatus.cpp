#include "ProgressStatus.h"

ProgressStatus::ProgressStatus(const Interface &i, jobject status_obj)
        : i(i), jobj(status_obj), running(false), started(false) {}

void ProgressStatus::poll(JNIEnv *env) {
    std::unique_lock<std::mutex> lock(msgMut);
    cv.wait(lock, [this]() { return !msgQueue.empty(); });

    while (!msgQueue.empty() && ((running && started) || (!running && !started))) {
        Msg msg = msgQueue.front();
        msgQueue.pop();

        switch (msg.type) {
            case BeginScan:
                if (started) {
                    throw std::runtime_error("Scan already started !");
                } else {
                    started = true;
                    running = true;
                    env->CallVoidMethod(jobj, i.progressBeginScan());
                }
                break;

            case BeginExtraction:
                env->CallVoidMethod(jobj, i.progressBeginExtraction(), msg.quantity);
                break;

            case ExtractionDone:
                env->CallVoidMethod(jobj, i.progressExtractionDone(), msg.quantity);
                running = false;
                break;

            case FileBegin:
                env->CallVoidMethod(jobj, i.progressFileBegin(), msg.threadId,
                                    Interface::to_bytes(msg.filename, env));
                break;

            case FileDone:
                env->CallVoidMethod(jobj, i.progressFileDone(), msg.threadId,
                                    Interface::to_bytes(msg.filename, env));
                break;

            case WrongGeometry:
                env->CallVoidMethod(jobj, i.progressWrongGeometry(), msg.threadId,
                                    Interface::to_bytes(msg.filename, env));
                break;
            case NoTiePoint:
                env->CallVoidMethod(jobj, i.progressNoTiePoint(), msg.threadId,
                                    Interface::to_bytes(msg.filename, env));
                break;
        }
    }

    if (!msgQueue.empty()) {
        throw std::runtime_error("Error on finish : last message isn't 'Extraction Done'.");
    }
}

bool ProgressStatus::needPolling() {
    return running || !started;
}

void ProgressStatus::beginScan() {
    std::lock_guard<std::mutex> lockGuard(msgMut);
    msgQueue.push(BeginScan);
    cv.notify_one();
}

void ProgressStatus::beginExtraction(int fileFound) {
    std::lock_guard<std::mutex> lockGuard(msgMut);
    msgQueue.emplace(BeginExtraction, fileFound);
    cv.notify_one();
}

void ProgressStatus::extractionDone(int fileExtracted) {
    std::lock_guard<std::mutex> lockGuard(msgMut);
    msgQueue.emplace(ExtractionDone, fileExtracted);
    cv.notify_one();
}

void ProgressStatus::fileDone(int threadId, const std::string &fileName) {
    std::lock_guard<std::mutex> lockGuard(msgMut);
    msgQueue.emplace(FileDone, threadId, fileName);
    cv.notify_one();
}

void ProgressStatus::wrongGeometry(int threadId, const std::string &fileName) {
    std::lock_guard<std::mutex> lockGuard(msgMut);
    msgQueue.emplace(WrongGeometry, threadId, fileName);
    cv.notify_one();
}

void ProgressStatus::noTiePoint(int threadId, const std::string &fileName) {
    std::lock_guard<std::mutex> lockGuard(msgMut);
    msgQueue.emplace(NoTiePoint, threadId, fileName);
    cv.notify_one();
}

void ProgressStatus::fileBegin(int threadId, const std::string &fileName) {
    std::lock_guard<std::mutex> lockGuard(msgMut);
    msgQueue.emplace(FileBegin, threadId, fileName);
    cv.notify_one();

}
