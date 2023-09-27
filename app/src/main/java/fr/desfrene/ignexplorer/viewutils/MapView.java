package fr.desfrene.ignexplorer.viewutils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import fr.desfrene.ignexplorer.ignutils.LambertCoordinates;
import fr.desfrene.ignexplorer.ignutils.MapNode;
import fr.desfrene.ignexplorer.ignutils.MapTile;

final public class MapView extends View {
    private final static int NO_TILE_TEXT_HEIGHT = 400; // In Lambert Unit
    private final Paint textPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tilePainter = new Paint(Paint.ANTI_ALIAS_FLAG);

    private MapNode root = null;
    @NonNull
    private final ViewPort v = new ViewPort();


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

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        v.setViewGeometry(w, h);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void setRoot(@NonNull final MapNode root) {
        this.root = root;
        this.v.setRoot(root.getGeometry());
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

    private final PointF lambertTopLeft = new PointF();
    private final Rect rect = new Rect();

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (root == null) {
            drawNoDataAtAll(canvas);
        } else {
            if (!v.validState()) {
                throw new IllegalStateException("ViewPort uninitialized !");
            }

            lambertTopLeft.set(v.screenLambertLeft(), v.screenLambertTop());
            rect.set(0, 0, 0, 0);

            while (rect.bottom < getHeight() || rect.right < getWidth()) {
                v.rectOf(lambertTopLeft, rect);
                MapTile tile = root.findTile(lambertTopLeft);

                if (tile == null) {
                    drawNoDataZone(canvas, rect);
                } else {
                    drawTile(canvas, tile, rect);
                }

                if (rect.right >= getWidth()) {
                    lambertTopLeft.set(v.screenLambertLeft(),
                            lambertTopLeft.y - v.getTileLambertHeight());
                } else {
                    lambertTopLeft.offset(v.getTileLambertWidth(), 0);
                }
            }
        }

        super.onDraw(canvas);
    }

    private void drawNoDataZone(Canvas canvas, @NonNull final Rect toDrawRect) {
        drawRect(canvas, toDrawRect);

        final float textWidth = NO_TILE_TEXT_HEIGHT * v.getScale();
        textPainter.setTextSize(textWidth);
        canvas.drawText("No Tile Data", toDrawRect.centerX(),
                toDrawRect.centerY() + textWidth / 2f, textPainter);
    }

    private void drawNoDataAtAll(Canvas canvas) {
        final float textWidth = 10;
        textPainter.setTextSize(textWidth);
        canvas.drawText("No Data", getWidth() / 2f, (getHeight() + textWidth) / 2f, textPainter);
    }

    private void drawTile(final @NonNull Canvas canvas,
                          final @NonNull MapTile tile,
                          final @NonNull Rect toDrawRect) {
        Bitmap img = getBitmap(tile);
        if (img == null) {
            drawError(canvas, toDrawRect);
        } else {
            Rect tileRect = v.getBitmapRect();
            canvas.drawBitmap(img, tileRect, toDrawRect, tilePainter);
        }
    }

    private void drawError(Canvas canvas, Rect toDrawRect) {
        drawRect(canvas, toDrawRect);

        final float textWidth = NO_TILE_TEXT_HEIGHT * v.getScale();
        textPainter.setTextSize(textWidth);
        canvas.drawText("Read Error", toDrawRect.centerX(),
                toDrawRect.centerY() + textWidth / 2f, textPainter);
    }

    private Bitmap getBitmap(MapTile tile) {
        return BitmapFactory.decodeFile(tile.getPath());
    }

    final Paint paintRect = new Paint(Paint.ANTI_ALIAS_FLAG);
    final int sWidth = 0;

    public void drawRect(final @NonNull Canvas canvas, final @NonNull Rect rect) {
        paintRect.setStrokeWidth(sWidth * 2);

        paintRect.setColor(Color.RED);
        canvas.drawLine(rect.left + sWidth, rect.top + sWidth, rect.right - sWidth,
                rect.top + sWidth, paintRect);

        paintRect.setColor(Color.MAGENTA);
        canvas.drawLine(rect.right - sWidth, rect.top + sWidth, rect.right - sWidth,
                rect.bottom - sWidth, paintRect);

        paintRect.setColor(Color.GREEN);
        canvas.drawLine(rect.right - sWidth, rect.bottom - sWidth, rect.left + sWidth,
                rect.bottom - sWidth, paintRect);

        paintRect.setColor(Color.BLUE);
        canvas.drawLine(rect.left + sWidth, rect.bottom - sWidth, rect.left + sWidth,
                rect.top + sWidth, paintRect);
    }
}
