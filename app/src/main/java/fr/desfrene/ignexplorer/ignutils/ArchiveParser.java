package fr.desfrene.ignexplorer.ignutils;

import androidx.annotation.NonNull;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ArchiveParser {

    public static class NativeException extends Exception {
        public NativeException(final String what) {
            super(what);
        }
    }

    private record TileRecord(double xLamC, double yLamC, byte[] path) {
        @NonNull
        @Override
        public String toString() {
            return "TileRecord{" +
                    "xLamC=" + xLamC +
                    ", yLamC=" + yLamC +
                    ", path=" + new String(path, StandardCharsets.UTF_8) +
                    '}';
        }
    }

    private record ExtractionResult(TileGeometry geometry, TileRecord[] records) {
        @NonNull
        @Override
        public String toString() {
            return "ExtractionResult{" +
                    "geometry=" + geometry +
                    ", records (" + records.length + ")=" + Arrays.toString(records) +
                    '}';
        }
    }


    public static MapNode addArchiveTiles(MapNode node,
                                          @NonNull String archivePath,
                                          @NonNull String fileDir,
                                          @NonNull final ProgressStatus progress) throws NativeException, MapNode.IncompatibleGeometry {
        final ExtractionResult extractionResults = ArchiveParser.parseArchiveNative(
                archivePath.getBytes(StandardCharsets.UTF_8),
                fileDir.getBytes(StandardCharsets.UTF_8),
                progress);

        for (final TileRecord record : extractionResults.records) {
            String filePath = new String(record.path, StandardCharsets.UTF_8);
            final LambertCoordinates c = LambertCoordinates.fromLambert((float) record.xLamC,
                    (float) record.yLamC);
            final MapTile tile = new MapTile(filePath, c);
            node = MapNode.addTile(node, tile, extractionResults.geometry);
        }

        return node;
    }


    private static native ExtractionResult parseArchiveNative(byte[] archivePath,
                                                              byte[] fileDir,
                                                              ProgressStatus progress) throws NativeException, RuntimeException;

    private ArchiveParser() {
    }
}
