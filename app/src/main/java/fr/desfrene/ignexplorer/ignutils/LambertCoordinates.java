package fr.desfrene.ignexplorer.ignutils;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.exp;
import static java.lang.Math.log;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;
import static java.lang.Math.toRadians;

import android.graphics.PointF;

import androidx.annotation.NonNull;

public class LambertCoordinates {
    private static final float LAMBERT_E = 0.081819191f;
    private static final float LAMBERT_LAMBDA_C = 0.05235987756f;
    private static final float LAMBERT_N = 0.7256077651f;
    private static final float LAMBERT_C = 11754255.426f;
    private static final float LAMBERT_XS = 700000;
    private static final float LAMBERT_YS = 12655612.0499f;
    private final PointF c = new PointF();
//    private final transient PointF latLon = new PointF(); // x ~ latitude | y ~ longitude

    private LambertCoordinates(final @NonNull PointF p) {
        // x, y in Lambert93
        c.set(p);
//        latLon.set(latLonFromLambert(c));
    }

    public static LambertCoordinates fromLatLonRad(double lat, double lon) {
        return new LambertCoordinates(lambertFromLatLon(lat, lon));
    }

    public static LambertCoordinates fromLatLonDeg(double lat, double lon) {
        return fromLatLonRad(toRadians(lat), toRadians(lon));
    }

    public static LambertCoordinates fromLambert(float x, float y) {
        return new LambertCoordinates(new PointF(x, y));
    }

    final public PointF getPointF() {
        return c;
    }

    /*
    public double getLatitude() {
        return toDegrees(getLatitudeRad());
    }

    public double getLongitude() {
        return toDegrees(getLongitudeRad());
    }

    public double getLatitudeRad() {
        return lonLat.x;
    }

    public double getLongitudeRad() {
        return lonLat.y;
    }

    public float getLambertX() {
        return c.x;
    }

    public float getLambertY() {
        return c.y;
    }
    */

    private static double latIsoFromLat(double lat) {
        return log(tan(PI / 4 + lat / 2) * pow((1 - LambertCoordinates.LAMBERT_E * sin(lat)) / (1 + LambertCoordinates.LAMBERT_E * sin(lat)), LambertCoordinates.LAMBERT_E / 2));
    }

    private static double latFromLatIso(double latIso) {
        double lat = 2 * atan(exp(latIso)) - PI / 2;
        double oldLat = 0;

        while (lat != oldLat) {
            oldLat = lat;
            lat = 2 * atan(exp(latIso) * pow((1 + LambertCoordinates.LAMBERT_E * sin(lat)) / (1 - LambertCoordinates.LAMBERT_E * sin(lat)), LambertCoordinates.LAMBERT_E / 2)) - PI / 2;
        }

        return lat;
    }

    private static PointF lambertFromLatLon(double phi, double lambda) {
        // (phi: latitude, lambda: longitude)

        double L = latIsoFromLat(phi);
        float x =
                (float) (LambertCoordinates.LAMBERT_XS + LambertCoordinates.LAMBERT_C * exp(-LambertCoordinates.LAMBERT_N * L) * sin(LambertCoordinates.LAMBERT_N * (lambda - LambertCoordinates.LAMBERT_LAMBDA_C)));
        float y =
                (float) (LambertCoordinates.LAMBERT_YS - LambertCoordinates.LAMBERT_C * exp(-LambertCoordinates.LAMBERT_N * L) * cos(LambertCoordinates.LAMBERT_N * (lambda - LambertCoordinates.LAMBERT_LAMBDA_C)));
        return new PointF(x, y);
    }

    private static PointF latLonFromLambert(final @NonNull PointF c) {
        double R =
                sqrt((c.x - LambertCoordinates.LAMBERT_XS) * (c.x - LambertCoordinates.LAMBERT_XS) + (c.y - LambertCoordinates.LAMBERT_YS) * (c.y - LambertCoordinates.LAMBERT_YS));
        double gamma =
                atan((c.x - LambertCoordinates.LAMBERT_XS) / (LambertCoordinates.LAMBERT_YS - c.y));
        double lambda = LambertCoordinates.LAMBERT_LAMBDA_C + gamma / LambertCoordinates.LAMBERT_N;
        double L = (-1 / LambertCoordinates.LAMBERT_N) * log(abs(R / LambertCoordinates.LAMBERT_C));
        double phi = latFromLatIso(L);

        // (latitude, longitude)
        return new PointF((float) phi, (float) lambda);
    }
}