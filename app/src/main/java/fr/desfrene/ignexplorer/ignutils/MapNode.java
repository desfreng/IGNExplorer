package fr.desfrene.ignexplorer.ignutils;

import android.graphics.PointF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;

import java.util.HashMap;

public final class MapNode {

    public static class IncompatibleGeometry extends Exception {
    }

    final TileGeometry geometry;
    public final HashMap<PointF, MapTile> tiles = new HashMap<>();

    private MapNode(@NonNull TileGeometry geo) {
        this.geometry = geo;
    }

    public TileGeometry getGeometry() {
        return geometry;
    }

    private void addChild(MapTile area, @NonNull final TileGeometry geo) throws IncompatibleGeometry {
        if (!geometry.isCompatibleWith(geo)) {
            throw new IncompatibleGeometry();
        }

        PointF c = new PointF();
        geometry.normalizeCoord(area.getTopLeftCoordinates().getPointF(), c);

        if (!tiles.containsKey(c)) {
            tiles.put(c, area);
        }
    }

    @NonNull
    public static MapNode addTile(@Nullable MapNode root, @NonNull final MapTile tile,
                                  @NonNull final TileGeometry geo) throws IncompatibleGeometry {
        if (root == null) {
            root = new MapNode(geo);
        }

        root.addChild(tile, geo);
        return root;
    }

    final static PointF tmpPointF = new PointF();

    @Nullable
    public MapTile findTile(@NonNull PointF lambertC) {
        geometry.normalizeCoord(lambertC, tmpPointF);
        return tiles.get(tmpPointF);
    }

    public final static JsonSerializer<MapNode> MAP_NODE_JSON_SERIALIZER =
            (node, type, context) -> {
                final JsonArray tileArr = new JsonArray();
                for (MapTile tile : node.tiles.values()) {
                    tileArr.add(context.serialize(tile));
                }

                final JsonObject obj = new JsonObject();
                obj.add("geometry", context.serialize(node.geometry));
                obj.add("tiles", tileArr);

                return obj;
            };

    public final static JsonDeserializer<MapNode> MAP_NODE_JSON_DESERIALIZER =
            (jsonElement, type, context) -> {
                final JsonObject obj = jsonElement.getAsJsonObject();

                final TileGeometry geo = context.deserialize(obj.get("geometry"),
                        TileGeometry.class);
                assert (geo != null);

                MapNode node = new MapNode(geo);

                final JsonArray arr = obj.get("tiles").getAsJsonArray();

                for (JsonElement elm : arr) {
                    MapTile tile = context.deserialize(elm, MapTile.class);
                    try {
                        node = MapNode.addTile(node, tile, geo);
                    } catch (IncompatibleGeometry e) {
                        throw new RuntimeException(e);
                    }
                }

                return node;
            };
}
