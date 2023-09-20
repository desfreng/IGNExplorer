package fr.desfrene.ignexplorer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import fr.desfrene.ignexplorer.ignutils.LambertCoordinates;
import fr.desfrene.ignexplorer.ignutils.MapNode;
import fr.desfrene.ignexplorer.ignutils.MapNodeSaver;
import fr.desfrene.ignexplorer.viewutils.MapView;

public class MainActivity extends AppCompatActivity {
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        mapView = findViewById(R.id.mapView);

        Handler handler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            MapNode root = openTiles(this);

            if (root == null) {
                handler.post(() -> {
                    Snackbar mySnackbar = Snackbar.make(findViewById(R.id.mainCoordinator),
                            R.string.no_files,
                            Snackbar.LENGTH_INDEFINITE);
                    mySnackbar.setAction(R.string.open_archive, v -> openArchives());
                    mySnackbar.show();
                });
            }

            mapView.setRoot(root);
        }).start();

        mapView.setScale(.1f);
        mapView.setCenterCoordinates(LambertCoordinates.fromLatLonDeg(6.576383, 44.391970));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.place_history) {
            return true;
        } else if (item.getItemId() == R.id.open_archive) {
            openArchives();
            return true;
        } else if (item.getItemId() == R.id.search_place) {
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    static private File mapFile(Context c) {
        return new File(c.getFilesDir(), c.getString(R.string.MAP_FILE));
    }

    public static MapNode openTiles(Context c) {
        try {
            return MapNodeSaver.getMap(mapFile(c));
        } catch (FileNotFoundException ignored) {
            return null;
        }
    }

    public static void saveTiles(MapNode n, Context c) throws IOException {
        MapNodeSaver.saveMap(n, mapFile(c));
    }

    private void openArchives() {
        Intent intent = new Intent(MainActivity.this, ArchiveActivity.class);
        startActivity(intent);
    }
}