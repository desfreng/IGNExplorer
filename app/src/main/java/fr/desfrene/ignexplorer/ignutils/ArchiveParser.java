package fr.desfrene.ignexplorer.ignutils;

import androidx.annotation.NonNull;

import java.nio.charset.StandardCharsets;

public enum ArchiveParser {
    ;

    public static class NativeException extends Exception {
        public NativeException(final String what) {
            super(what);
        }
    }

    private record TileRecord(double xLamC, double yLamC, byte[] path) {
    }

    private record ExtractionResult(TileGeometry geometry, TileRecord[] records) {
    }


    public static MapNode addArchiveTiles(MapNode node,
                                          @NonNull String archivePath,
                                          @NonNull String fileDir,
                                          @NonNull final ProgressStatus progress) throws NativeException {
        final ExtractionResult extractionResults = ArchiveParser.parseArchiveNative(
                archivePath.getBytes(StandardCharsets.UTF_8),
                fileDir.getBytes(StandardCharsets.UTF_8),
                progress);

        for (final TileRecord record : extractionResults.records) {
            String filePath = new String(record.path, StandardCharsets.UTF_8);
            final LambertCoordinates c = LambertCoordinates.fromLambert(record.xLamC, record.yLamC);
            final MapTile tile = new MapTile(filePath, c);

            node = MapNode.addTile(node, tile, extractionResults.geometry);
        }

        return node;
    }


    private static native ExtractionResult parseArchiveNative(byte[] archivePath,
                                                              byte[] fileDir,
                                                              ProgressStatus progress) throws NativeException, RuntimeException;

}
