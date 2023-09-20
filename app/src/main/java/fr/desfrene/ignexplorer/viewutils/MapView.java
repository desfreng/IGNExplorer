package fr.desfrene.ignexplorer.viewutils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import fr.desfrene.ignexplorer.ignutils.LambertCoordinates;
import fr.desfrene.ignexplorer.ignutils.MapNode;

final public class MapView extends View {
    private final static int NO_TILE_TEXT_HEIGHT = 400; // In Lambert Unit
    private final Paint textPainter = new Paint(Paint.ANTI_ALIAS_FLAG);

    private MapNode root = null;
    private ViewPort v = null;


    public MapView(Context context) {
        super(context);
        init();
    }

    public MapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        textPainter.setTextAlign(Paint.Align.CENTER);
        textPainter.setColor(Color.BLACK);
    }

    public void setRoot(MapNode root) {
        this.root = root;
        this.v = new ViewPort(root.getGeometry());
        this.postInvalidate();
    }

    public void setCenterCoordinates(final @NonNull LambertCoordinates c) {
        v.setCenter(c);
        this.postInvalidate();
    }

    public void setScale(float scale) {
        v.setScale(scale);
        this.postInvalidate();
    }

    private final Point lastDrawnPos = new Point();

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        lastDrawnPos.set(0, 0); // Refactor & Make this clean PLEASE

        if (root == null) {
            drawNoDataAtAll(canvas);
        } else {
            while (lastDrawnPos.y < getHeight()) {
                drawTile(canvas, root);
            }
        }

        super.onDraw(canvas);
    }

    private final RectF toDrawRect = new RectF();

    private void drawNoDataZone(Canvas canvas, int x, int y) {
//        float top = ;
//        float left = ;
//
//        toDrawRect.set(left, top, left + v.tilePixelWidth(), top + v.tilePixelHeight());

        drawRect(canvas, toDrawRect, true);

        final float textWidth = NO_TILE_TEXT_HEIGHT * v.scale();
        textPainter.setTextSize(textWidth);
        canvas.drawText("No Tile Data", toDrawRect.centerX(),
                toDrawRect.centerY() + textWidth / 2f, textPainter);
    }

    private void drawNoDataAtAll(Canvas canvas) {
        final float textWidth = 10;
        textPainter.setTextSize(textWidth);
        canvas.drawText("No Data", getWidth() / 2f, (getHeight() + textWidth) / 2f, textPainter);
    }

    //            topLeft.set(centerCoordinates.x - width / (2.f * scale),
//                centerCoordinates.y + height / (2.f * scale));
    private void drawTile(final @NonNull Canvas canvas, final @Nullable MapNode root) {
//        final PointF currentLambertCoordinates = new PointF();
//
//        topLeft.set(currentTileZone.x * tileWidth, currentTileZone.y * tileHeight,
//                currentLambertCoordinates);
//
//        MapTile tile = null;
//
//        if (root != null) {
//            tile = root.findTile(currentLambertCoordinates);
//        }
//
//        if (tile == null) {
//
//            float textSize = NO_TILE_TEXT_HEIGHT * scale;
//            textPainter.setTextSize(textSize);
//
//            drawnRect.set(currentScreenPos.x, currentScreenPos.y,
//                    currentScreenPos.x + tileWidth * scale,
//                    currentScreenPos.y + tileHeight * scale);
//
//            drawRect(canvas, drawnRect, true);
//
//
//            canvas.drawText("No Tile Found",
//                    currentScreenPos.x + scale * tileWidth / 2,
//                    currentScreenPos.y + scale * tileHeight / 2 + textSize / 2,
//                    textPainter);
//
//
//        } else {
////            Bitmap img = tile.getBitmap();
////            Rect tileRect = tile.getBitmapRect();
//            LambertCoordinates imgTopLeft = tile.getTopLeftCoordinates();
//
//            float lambertDX = currentLambertCoordinates.x - imgTopLeft.getLambertX();
//            float lambertDY = imgTopLeft.getLambertY() - currentLambertCoordinates.y;
//            Log.i(TAG, "LdX : " + lambertDX + " LdY : " + lambertDY);
//
//            float left = currentScreenPos.x - lambertDX * scale;
//            float top = currentScreenPos.y - lambertDY * scale;
//
//            drawnRect.set(left, top, left + tileWidth * scale, top + tileHeight * scale);
//            Log.e(TAG, "Rect : " + drawnRect);
////            canvas.drawBitmap(img, tileRect, drawnRect, imgPainter);
//            drawRect(canvas, drawnRect, true);
//        }
//
//        if (drawnRect.right > getWidth()) {
//            // New Line
//            currentScreenPos.set(0, drawnRect.bottom);
//            currentLambertCoordinates.set(topLeft.getLambertX(),
//                    currentLambertCoordinates.y - tileHeight);
//        } else {
//            // Same Line
//            currentScreenPos.set(drawnRect.right, drawnRect.top);
//            currentLambertCoordinates.set(topLeft.getLambertX() + tileWidth,
//                    currentLambertCoordinates.y);
//        }
    }


    public void drawRect(final @NonNull Canvas canvas, final @NonNull RectF rect, boolean crossed) {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        if (crossed) {
            paint.setStrokeWidth(1);
            paint.setColor(Color.BLACK);
            canvas.drawLine(rect.left + 1, rect.top + 1, rect.right - 1,
                    rect.bottom + 1, paint);
            canvas.drawLine(rect.right + 1, rect.top + 1, rect.right - 1,
                    rect.left + 1, paint);
        }

        int sWidth = 5;
        paint.setStrokeWidth(sWidth);

        paint.setColor(Color.RED);
        canvas.drawLine(rect.left + sWidth + 1, rect.top + sWidth + 1, rect.right - sWidth - 1,
                rect.top + sWidth + 1, paint);

        paint.setColor(Color.YELLOW);
        canvas.drawLine(rect.right - sWidth - 1, rect.top + sWidth + 1, rect.right - sWidth - 1,
                rect.bottom - sWidth - 1, paint);

        paint.setColor(Color.GREEN);
        canvas.drawLine(rect.right - sWidth - 1, rect.bottom - sWidth - 1, rect.left + sWidth + 1,
                rect.bottom - sWidth - 1, paint);

        paint.setColor(Color.BLUE);
        canvas.drawLine(rect.left + sWidth + 1, rect.bottom - sWidth - 1, rect.left + sWidth + 1,
                rect.top + sWidth + 1, paint);
    }
}
