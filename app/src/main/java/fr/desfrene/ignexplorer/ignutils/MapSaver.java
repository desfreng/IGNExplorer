package fr.desfrene.ignexplorer.ignutils;

import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class MapSaver {
    private MapSaver() {

    }

    public static void saveMap(MapNode root, File mapSave) throws IOException {
        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapter(MapNode.class, MapNode.MAP_NODE_JSON_SERIALIZER);
        gson.registerTypeAdapter(MapTile.class, MapTile.MAP_TILE_JSON_SERIALIZER);

        Writer writer = new FileWriter(mapSave);
        gson.setPrettyPrinting().create().toJson(root, writer);
        writer.close();
    }

    public static MapNode getMap(File mapSave) throws FileNotFoundException {
        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapter(MapNode.class, MapNode.MAP_NODE_JSON_DESERIALIZER);
        gson.registerTypeAdapter(MapTile.class, MapTile.MAP_TILE_JSON_DESERIALIZER);

        return gson.create().fromJson(new FileReader(mapSave), MapNode.class);
    }
}
