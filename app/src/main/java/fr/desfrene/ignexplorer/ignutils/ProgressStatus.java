package fr.desfrene.ignexplorer.ignutils;

import java.io.IOException;

public interface ProgressStatus {
    void begin();

    void archiveCopyDone();

    void beginArchiveScan();

    void beginExtraction(int fileFound);

    void fileBegin(int threadId, byte[] fileName);

    void fileDone(int threadId, byte[] fileName);

    void extractionDone(int fileExtracted);

    void beginSaveModel();

    void beginCacheCleaning();

    void end();

    /* Error Handling */
    void wrongGeometry(int threadId, byte[] fileName);

    void noTiePoint(int threadId, byte[] fileName);

    void copyError(IOException e);

    void extractionError(Exception e);

    void savingError(Exception e);
}
