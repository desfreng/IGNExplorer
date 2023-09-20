package fr.desfrene.ignexplorer.viewutils;

import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

import androidx.annotation.NonNull;

import fr.desfrene.ignexplorer.ignutils.LambertCoordinates;
import fr.desfrene.ignexplorer.ignutils.MapNode;
import fr.desfrene.ignexplorer.ignutils.TileGeometry;

final public class ViewPort {
    /**
     * Lambert Coordinates of the Center Point of the Screen
     */
    private final PointF center = new PointF();

    /**
     * Scale in Pixel per Lambert Unit
     */
    private float scale = 1f;

    /**
     * Lambert Reference Point (A top left corner of a tile in the screen)
     */
    private final TileGeometry tileGeometry;

    public ViewPort(TileGeometry tileGeometry) {
        this.tileGeometry = tileGeometry;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setCenter(LambertCoordinates c) {
        c.fillInto(center);
    }

    public float scale() {
        return scale;
    }

    public PointF center() {
        return center;
    }

    public float tilePixelWidth() {
        return tileGeometry.getTileLambertWidth() * scale;
    }

    public float tilePixelHeight() {
        return tileGeometry.getTileLambertHeight() * scale;
    }

    public void rectOf(@NonNull final PointF screenPos, @NonNull final RectF screenRect) {
        // TODO
    }

    public void screenToLambert(@NonNull final PointF screenPos, @NonNull final PointF lambertPos) {
        // TODO
    }

    public void lambertToScreen(@NonNull final PointF lambertPos, @NonNull final PointF screenPos) {
        // TODO
    }
}
