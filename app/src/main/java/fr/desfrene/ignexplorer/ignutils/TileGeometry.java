package fr.desfrene.ignexplorer.ignutils;

import android.graphics.Point;
import android.graphics.PointF;

import androidx.annotation.NonNull;

public final class TileGeometry {
    private final PointF scale;
    private final Point tileDim;
    private final PointF lamOrig;

    public TileGeometry(float scaleX, float scaleY,
                        int width, int height,
                        float lambertXOrigin, float lambertYOrigin) {
        this.scale = new PointF(scaleX, scaleY);
        this.tileDim = new Point(width, height);
        this.lamOrig = new PointF(lambertXOrigin, lambertYOrigin);
    }

    private int getTileWidth() {
        return tileDim.x;
    }

    private int getTileHeight() {
        return tileDim.y;
    }

    public float getTileLambertWidth() {
        return getTileWidth() * scale.x;
    }

    public float getTileLambertHeight() {
        return getTileHeight() * scale.y;
    }

    public void normalizeCoord(final PointF lambertC, final PointF result) {
        if (lambertC != null) {
            float dX = lambertC.x - lamOrig.x;
            float dY = lambertC.y - lamOrig.y;

            int nX = (int) Math.floor(dX / getTileLambertWidth());
            int nY = (int) Math.ceil(dY / getTileLambertHeight());

            result.set(lamOrig.x + nX * getTileLambertWidth(),
                    lamOrig.y + nY * getTileLambertHeight());
        }
    }

    final static PointF tmpPointF = new PointF();

    public void normalizeCoord(final LambertCoordinates topLeftCoordinates, final PointF res) {
        topLeftCoordinates.fillInto(tmpPointF);
        normalizeCoord(tmpPointF, res);
    }

    public boolean isCompatibleWith(@NonNull TileGeometry geo) {
        if (!scale.equals(geo.scale) && tileDim.equals(geo.tileDim)) {
            return false;
        }

        geo.normalizeCoord(lamOrig, tmpPointF);
        return tmpPointF.equals(lamOrig);
    }
}
