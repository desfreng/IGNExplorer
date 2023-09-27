package fr.desfrene.ignexplorer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.desfrene.ignexplorer.ignutils.ArchiveParser;
import fr.desfrene.ignexplorer.ignutils.MapNode;
import fr.desfrene.ignexplorer.ignutils.ProgressStatus;

public class ArchiveActivity extends AppCompatActivity {
    private TextView mainStatus;
    private TextView detailedStatus;
    private ProgressBar progressBar;
    private Button button;
    private final AtomicBoolean parsingData = new AtomicBoolean(false);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() != Activity.RESULT_OK) {
                            return;
                        }

                        Intent data = result.getData();
                        if (data == null) {
                            return;
                        }

                        if (parsingData.get()) {
                            return;
                        }

                        parseArchive(data.getData());
                    });

    // Used to load the 'ignexplorer' library on application startup.
    static {
        System.loadLibrary("ignexplorer");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        parsingData.set(false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);

        mainStatus = findViewById(R.id.mainStatusView);
        detailedStatus = findViewById(R.id.detailedStatusView);
        progressBar = findViewById(R.id.progressBar);

        detailedStatus.setMovementMethod(new ScrollingMovementMethod());

        button = findViewById(R.id.button);
        button.setOnClickListener(view -> startSelectArchiveMenu());
    }

    boolean isRestart = false;

    @Override
    protected void onRestart() {
        isRestart = true;
        super.onRestart();
    }

    @Override
    protected void onStop() {
        isRestart = false;
        super.onStop();
    }

    @Override
    protected void onStart() {
        if (!isRestart) {
            progressBar.setVisibility(View.INVISIBLE);
            startSelectArchiveMenu();
        }
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        if (parsingData.get()) {
            Snackbar mySnackbar = Snackbar.make(findViewById(R.id.coordinator),
                    R.string.snack_msg,
                    Snackbar.LENGTH_SHORT);
            mySnackbar.setAction(R.string.yes, v -> {
                executor.shutdownNow();
                super.onBackPressed();
            });
            mySnackbar.setAction(R.string.no, v -> mySnackbar.dismiss());
            mySnackbar.show();
        } else {
            super.onBackPressed();
        }
    }

    private void startSelectArchiveMenu() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        i.setType("*/*");
        activityResultLauncher.launch(Intent.createChooser(i, getString(R.string.pick_file)));
    }

    private void appendText(String text) {
        detailedStatus.append("\n" + text);
    }

    private void appendText(int textId) {
        appendText(getString(textId));
    }

    private void parseArchive(Uri uri) {
        Handler handler = new Handler(Looper.getMainLooper());

        final File cacheArchiveFile = new File(getCacheDir(), "archive.7z");
        final File imageDir = MainActivity.getImageDir(this);
        ProgressStatus status = new ProgressStatus() {
            boolean goneSmoothly;
            int fileFound;
            int fileDone;

            @Override
            public void begin() {
                handler.post(() -> {
                    goneSmoothly = true;
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setIndeterminate(true);

                    detailedStatus.setVisibility(View.VISIBLE);
                    appendText(R.string.begin_process);
                    appendText(R.string.copying_archive);

                    mainStatus.setKeepScreenOn(true);
                    mainStatus.setText(R.string.main_copy);

                    button.setEnabled(false);
                });
            }

            @Override
            public void archiveCopyDone() {
                handler.post(() -> appendText(R.string.copied_archive));
            }

            @Override
            public void beginArchiveScan() {
                handler.post(() -> {
                    appendText(R.string.scanning_archive);

                    mainStatus.setText(R.string.main_scan);
                });
            }

            @Override
            public void beginExtraction(int fileFound) {
                handler.post(() -> {
                    progressBar.setIndeterminate(false);
                    progressBar.setMin(0);
                    progressBar.setMax(fileFound + 1);
                    this.fileFound = fileFound;
                    this.fileDone = 0;
                    progressBar.setProgress(1);

                    appendText(getResources().getQuantityString(R.plurals.extracting_archive,
                            fileFound, fileFound));

                    mainStatus.setText(getString(R.string.main_extraction, fileDone, fileFound));
                });
            }

            @Override
            public void fileBegin(int threadId, byte[] fileName) {
                final String fileNameStr = new String(fileName, StandardCharsets.UTF_8);
                handler.post(() -> appendText(getString(R.string.processing_file, threadId,
                        fileNameStr)));
            }

            @Override
            public void fileDone(int threadId, byte[] fileName) {
                final String fileNameStr = new String(fileName, StandardCharsets.UTF_8);
                handler.post(() -> {
                    appendText(getString(R.string.extracted_file, threadId, fileNameStr));
                    progressBar.incrementProgressBy(1);
                    fileDone++;
                    mainStatus.setText(getString(R.string.main_extraction, fileDone, fileFound));
                });
            }

            @Override
            public void extractionDone(int fileExtracted) {
                handler.post(() -> {
                    appendText(getResources().getQuantityString(R.plurals.extracted_archive,
                            fileExtracted, fileExtracted));

                    progressBar.setProgress(progressBar.getMax());
                });
            }

            @Override
            public void beginSaveModel() {
                handler.post(() -> {
                    progressBar.setIndeterminate(true);

                    appendText(getString(R.string.saving_model));
                    mainStatus.setText(R.string.main_save);
                });
            }

            @Override
            public void beginCacheCleaning() {
                handler.post(() -> {
                    appendText(getString(R.string.cleaning_cache));
                    mainStatus.setText(R.string.main_clean);
                });
            }

            @Override
            public void end() {
                handler.post(() -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    progressBar.setIndeterminate(false);
                    progressBar.setMin(0);
                    progressBar.setMax(1);
                    progressBar.setProgress(0);

                    if (goneSmoothly) {
                        appendText(R.string.end_process);
                    } else {
                        appendText(R.string.end_process_error);
                    }

                    mainStatus.setKeepScreenOn(false);
                    if (goneSmoothly) {
                        mainStatus.setText(R.string.main_done);
                    } else {
                        mainStatus.setText(R.string.main_done_error);
                    }

                    button.setEnabled(true);
                });
            }

            @Override
            public void wrongGeometry(int threadId, byte[] fileName) {
                goneSmoothly = false;
                final String fileNameStr = new String(fileName, StandardCharsets.UTF_8);
                handler.post(() -> appendText(getString(R.string.warn_geometry, threadId,
                        fileNameStr)));
            }

            @Override
            public void noTiePoint(int threadId, byte[] fileName) {
                goneSmoothly = false;
                final String fileNameStr = new String(fileName, StandardCharsets.UTF_8);
                handler.post(() -> appendText(getString(R.string.warn_calibration,
                        threadId, fileNameStr)));
            }

            @Override
            public void copyError(IOException e) {
                handler.post(() -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    progressBar.setIndeterminate(false);
                    progressBar.setMin(0);
                    progressBar.setMax(1);
                    progressBar.setProgress(0);

                    appendText(getString(R.string.error_copy, e.getLocalizedMessage()));

                    mainStatus.setKeepScreenOn(false);
                    mainStatus.setText(R.string.main_error);

                    button.setEnabled(true);
                });
            }

            @Override
            public void extractionError(Exception e) {
                handler.post(() -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    progressBar.setIndeterminate(false);
                    progressBar.setMin(0);
                    progressBar.setMax(1);
                    progressBar.setProgress(0);

                    appendText(getString(R.string.error_extraction, e.getLocalizedMessage()));

                    mainStatus.setKeepScreenOn(false);
                    mainStatus.setText(R.string.main_error);

                    button.setEnabled(true);
                });
            }

            @Override
            public void savingError(Exception e) {
                handler.post(() -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    progressBar.setIndeterminate(false);
                    progressBar.setMin(0);
                    progressBar.setMax(1);
                    progressBar.setProgress(0);

                    appendText(getString(R.string.error_saving, e.getLocalizedMessage()));

                    mainStatus.setKeepScreenOn(false);
                    mainStatus.setText(R.string.main_error);

                    button.setEnabled(true);
                });
            }

            @Override
            public void incompatibleTileSet() {
                handler.post(() -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    progressBar.setIndeterminate(false);
                    progressBar.setMin(0);
                    progressBar.setMax(1);
                    progressBar.setProgress(0);

                    appendText(getString(R.string.error_incompatible));

                    mainStatus.setKeepScreenOn(false);
                    mainStatus.setText(R.string.main_error);

                    button.setEnabled(true);
                });
            }
        };


        executor.execute(() -> {
            parsingData.set(true);
            status.begin();

            try {
                InputStream is = getContentResolver().openInputStream(uri);

                if (is == null) {
                    throw new IOException("Unable to open file");
                }

                FileOutputStream os = new FileOutputStream(cacheArchiveFile);
                IOUtils.copy(is, os);
                is.close();
                os.close();
            } catch (IOException e) {
                status.copyError(e);
                return;
            }

            status.archiveCopyDone();

            MapNode node;
            try {
                node = MainActivity.openTiles(this);
                node = ArchiveParser.addArchiveTiles(node, cacheArchiveFile.getPath(),
                        imageDir.getPath(), status);
            } catch (ArchiveParser.NativeException e) {
                status.extractionError(e);
                return;
            } catch (MapNode.IncompatibleGeometry e) {
                status.incompatibleTileSet();
                return;
            }

            status.beginSaveModel();

            try {
                MainActivity.saveTiles(node, this);
            } catch (Exception e) {
                status.savingError(e);
                return;
            }

            status.beginCacheCleaning();
            if (cacheArchiveFile.delete()) {
                handler.post(() -> appendText(getString(R.string.cleaned_cache)));
            } else {
                handler.post(() -> appendText(getString(R.string.error_cache)));
            }

            status.end();
            parsingData.set(false);
        });
    }
}