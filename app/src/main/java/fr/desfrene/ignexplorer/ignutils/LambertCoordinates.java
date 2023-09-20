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
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import android.graphics.PointF;

import androidx.annotation.NonNull;

import java.util.Objects;

public class LambertCoordinates {
    private static final float LAMBERT_E = 0.081819191f;
    private static final float LAMBERT_LAMBDA_C = 0.05235987756f;
    private static final float LAMBERT_N = 0.7256077651f;
    private static final float LAMBERT_C = 11754255.426f;
    private static final float LAMBERT_XS = 700000;
    private static final float LAMBERT_YS = 12655612.0499f;

    private final float lambertX;
    private final float lambertY;
    private transient double[] lonLat;

    private LambertCoordinates(float x, float y) {
        // x, y in Lambert93
        lambertX = x;
        lambertY = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LambertCoordinates that = (LambertCoordinates) o;
        return Double.compare(lambertX, that.lambertX) == 0 && Double.compare(lambertY,
                that.lambertY) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lambertX, lambertY);
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("LambertCoordinates{%s, %s}", getLambertX(), getLambertY());
    }

    public static LambertCoordinates fromLatLonRad(double lon, double lat) {
        float[] lambert = lambertFromLatLon(lon, lat);

        return fromLambert(lambert[0], lambert[1]);
    }

    public static LambertCoordinates fromLatLonDeg(double lon, double lat) {
        return fromLatLonRad(toRadians(lon), toRadians(lat));
    }

    public static LambertCoordinates fromLambert(double x, double y) {
        return new LambertCoordinates((float) x, (float) y);
    }

    public LambertCoordinates translate(float dx, float dy) {
        return LambertCoordinates.fromLambert(getLambertX() + dx, getLambertY() + dy);
    }

    public void translateTo(float dx, float dy, PointF res) {
        res.set(getLambertX() + dx, getLambertY() + dy);
    }

    public void fillInto(final @NonNull PointF p) {
        p.set(getLambertX(), getLambertY());
    }

    public double getLatitude() {
        return toDegrees(getLatitudeRad());
    }

    public double getLongitude() {
        return toDegrees(getLongitudeRad());
    }

    public double getLatitudeRad() {
        if (lonLat == null) {
            lonLat = latLonFromLambert(lambertX, lambertY);
        }
        return lonLat[1];
    }

    public double getLongitudeRad() {
        if (lonLat == null) {
            lonLat = latLonFromLambert(lambertX, lambertY);
        }
        return lonLat[0];
    }

    public float getLambertX() {
        return lambertX;
    }

    public float getLambertY() {
        return lambertY;
    }

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

    private static float[] lambertFromLatLon(double lambda, double phi) {
        double L = latIsoFromLat(phi);
        float x =
                (float) (LambertCoordinates.LAMBERT_XS + LambertCoordinates.LAMBERT_C * exp(-LambertCoordinates.LAMBERT_N * L) * sin(LambertCoordinates.LAMBERT_N * (lambda - LambertCoordinates.LAMBERT_LAMBDA_C)));
        float y =
                (float) (LambertCoordinates.LAMBERT_YS - LambertCoordinates.LAMBERT_C * exp(-LambertCoordinates.LAMBERT_N * L) * cos(LambertCoordinates.LAMBERT_N * (lambda - LambertCoordinates.LAMBERT_LAMBDA_C)));
        return new float[]{x, y};
    }

    private static double[] latLonFromLambert(double x, double y) {
        double R =
                sqrt((x - LambertCoordinates.LAMBERT_XS) * (x - LambertCoordinates.LAMBERT_XS) + (y - LambertCoordinates.LAMBERT_YS) * (y - LambertCoordinates.LAMBERT_YS));
        double gamma =
                atan((x - LambertCoordinates.LAMBERT_XS) / (LambertCoordinates.LAMBERT_YS - y));
        double lambda = LambertCoordinates.LAMBERT_LAMBDA_C + gamma / LambertCoordinates.LAMBERT_N;
        double L = (-1 / LambertCoordinates.LAMBERT_N) * log(abs(R / LambertCoordinates.LAMBERT_C));
        double phi = latFromLatIso(L);

        return new double[]{lambda, phi};
    }
}