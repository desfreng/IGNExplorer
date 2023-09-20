package fr.desfrene.ignexplorer.ignutils;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class MapNodeSaver {
    private MapNodeSaver() {

    }

    final private static TypeAdapter<File> FILE = new TypeAdapter<>() {
        @Override
        public void write(JsonWriter out, File file) throws IOException {
            out.value(file == null ? "null" : file.getPath());
        }

        @Override
        public File read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return new File(in.nextString());
        }
    };

    public static void saveMap(MapNode root, File mapSave) throws IOException {
        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapter(File.class, FILE);
        gson.registerTypeAdapter(MapNode.class, MapNode.MAP_NODE_JSON_SERIALIZER);

        Writer writer = new FileWriter(mapSave);
        gson.setPrettyPrinting().create().toJson(root, writer);
        writer.close();
    }

    public static MapNode getMap(File mapSave) throws FileNotFoundException {
        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapter(File.class, FILE);
        gson.registerTypeAdapter(MapNode.class, MapNode.MAP_NODE_JSON_DESERIALIZER);

        return gson.create().fromJson(new FileReader(mapSave), MapNode.class);
    }
}
