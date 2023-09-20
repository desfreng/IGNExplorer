package fr.desfrene.ignexplorer.ignutils;

import androidx.annotation.NonNull;

import java.io.File;

public final class MapTile {
    @NonNull
    private final String path;
    @NonNull
    private final LambertCoordinates coord;

    public MapTile(@NonNull String filePath, @NonNull LambertCoordinates c) {

        File path = new File(filePath);

        if (!path.canRead()) {
            throw new IllegalArgumentException("Cant read " + path.getPath());
        }

        this.path = path.getPath();
        this.coord = c;
    }

    @NonNull
    public LambertCoordinates getTopLeftCoordinates() {
        return coord;
    }

    @NonNull
    public String getPath() {
        return path;
    }

}