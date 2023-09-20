#ifndef IGN_EXPLORER_PROGRESS_STATUS_H
#define IGN_EXPLORER_PROGRESS_STATUS_H


#include <jni.h>
#include <queue>
#include <string>
#include <mutex>
#include <variant>
#include "Interface.h"

class ProgressStatus {

public:
    ProgressStatus(const Interface &i, jobject status_obj);

    void beginScan();

    void beginExtraction(int fileFound);

    void extractionDone(int fileExtracted);

    void fileBegin(int threadId, const std::string &fileName);

    void fileDone(int threadId, const std::string &fileName);

    void wrongGeometry(int threadId, const std::string &fileName);

    void noTiePoint(int threadId, const std::string &fileName);

    bool needPolling();

    void poll(JNIEnv *env);

private:
    enum MsgTypes {
        BeginScan,
        BeginExtraction,
        ExtractionDone,
        FileBegin,
        FileDone,
        WrongGeometry,
        NoTiePoint,
    };

    struct Msg {
        Msg(MsgTypes type, int q) : type(type), quantity(q) {};

        Msg(MsgTypes type) : type(type) {};

        Msg(MsgTypes type, int i, const std::string &s) : type(type), threadId(i), filename(s) {};

        MsgTypes type;
        std::string filename;
        int threadId;
        int quantity;
    };

    std::mutex msgMut;
    std::condition_variable cv;
    std::queue<Msg> msgQueue;

    Interface i;
    jobject jobj;

    std::atomic_bool running;
    std::atomic_bool started;
};


#endif //IGN_EXPLORER_PROGRESS_STATUS_H
