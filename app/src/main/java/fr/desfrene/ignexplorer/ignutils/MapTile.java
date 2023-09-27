package fr.desfrene.ignexplorer.ignutils;

import androidx.annotation.NonNull;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;

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

    @NonNull
    @Override
    public String toString() {
        return "MapTile{" + "path='" + path + '\'' + ", coord=" + coord + '}';
    }

    public final static JsonSerializer<MapTile> MAP_TILE_JSON_SERIALIZER =
            (tile, type, context) -> {
        final JsonObject obj = new JsonObject();
        obj.addProperty("x", tile.coord.getPointF().x);
        obj.addProperty("y", tile.coord.getPointF().y);
        obj.addProperty("path", tile.path);
        return obj;
    };

    public final static JsonDeserializer<MapTile> MAP_TILE_JSON_DESERIALIZER = (jsonElement, type
            , context) -> {
        final JsonObject obj = jsonElement.getAsJsonObject();
        final float x = obj.get("x").getAsFloat();
        final float y = obj.get("y").getAsFloat();
        final String path = obj.get("path").getAsString();
        return new MapTile(path, LambertCoordinates.fromLambert(x, y));
    };

}