package fr.desfrene.ignexplorer.viewutils;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import fr.desfrene.ignexplorer.ignutils.LambertCoordinates;
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
     * Screen Width and Height in Pixel
     */
    private final Point screenRect = new Point();

    @Nullable
    private TileGeometry tileGeometry;
    private final Rect bitmapRect = new Rect();

    public ViewPort() {
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setCenter(LambertCoordinates c) {
        center.set(c.getPointF());
    }

    public void setViewGeometry(final int width, final int height) {
        screenRect.set(width, height);
    }

    public void setRoot(TileGeometry geometry) {
        tileGeometry = geometry;
        bitmapRect.set(0, 0, tileGeometry.getTileWidth(), tileGeometry.getTileHeight());
    }

    public boolean validState() {
        return tileGeometry != null &&
                screenRect.x > 0 && screenRect.y > 0 &&
                center.x != 0 && center.y != 0;
    }

    public Rect getBitmapRect() {
        if (validState()) {
            return bitmapRect;
        } else {
            return null;
        }
    }

    public float getScale() {
        if (validState()) {
            return scale;
        } else {
            return -1;
        }
    }

    public float getTileLambertHeight() {
        if (validState()) {
            return tileGeometry.getTileLambertHeight();
        } else {
            return -1;
        }
    }

    public float getTileLambertWidth() {
        if (validState()) {
            return tileGeometry.getTileLambertWidth();
        } else {
            return -1;
        }
    }

    private int tilePixelWidth() {
        if (validState()) {
            return (int) (getTileLambertWidth() * scale);
        } else {
            return -1;
        }
    }

    private int tilePixelHeight() {
        if (validState()) {
            return (int) (getTileLambertHeight() * scale);
        } else {
            return -1;
        }
    }

    public float screenLambertLeft() {
        if (validState()) {
            return center.x - screenRect.x / (2 * scale);
        } else {
            return -1;
        }
    }

    public float screenLambertTop() {
        if (validState()) {
            return center.y + screenRect.y / (2 * scale);
        } else {
            return -1;
        }
    }

    Point topLeftPixelTile = new Point();
    PointF topLeftLambertTile = new PointF();

    public void rectOf(@NonNull final PointF lambertPos, @NonNull final Rect screenRect) {
        if (!validState()) {
            return;
        }
        tileGeometry.normalizeCoord(lambertPos, topLeftLambertTile);
        // tmpPointF is the TopLeft Corner of the Tile (LambertSpace)
        lambertToScreen(topLeftLambertTile, topLeftPixelTile);
        // tmp is the TopLeft Corner of the Tile (PixelSpace)

        screenRect.set(topLeftPixelTile.x, topLeftPixelTile.y,
                topLeftPixelTile.x + tilePixelWidth(),
                topLeftPixelTile.y + tilePixelHeight());
    }

    public void screenToLambert(@NonNull final Point screenPos, @NonNull final PointF lambertPos) {
        if (!validState()) {
            return;
        }

        float dx_pixel = screenPos.x - screenRect.x / 2f;
        float dy_pixel = screenPos.y - screenRect.y / 2f;
        lambertPos.set(center.x + dx_pixel / scale, center.y - dy_pixel / scale);
    }

    public void lambertToScreen(@NonNull final PointF lambertPos, @NonNull final Point screenPos) {
        if (!validState()) {
            return;
        }

        float dx_lambert = lambertPos.x - center.x;
        float dy_lambert = lambertPos.y - center.y;
        screenPos.set(Math.round(screenRect.x / 2f + dx_lambert * scale),
                Math.round(screenRect.y / 2f - dy_lambert * scale));
    }
}











