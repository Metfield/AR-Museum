package ciu196.chalmers.se.armuseum;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by johnpetersson on 2016-10-05.
 */
public class DrawingView extends View {

    public static final String DRAW_MOTION_CHILD = "drawmotion";

    private SerializablePath drawPath;
    private Paint drawPaint, canvasPaint;
    private int paintColor = 0xFF550000;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;

    private float brushsize;
    private float lastBrushSize; // For when you switch to eraser

    private boolean isErasing = false;

    private DatabaseReference mFirebaseDatabaseReference;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
//        brushsize = getResources().getInteger(R.integer.standard_size);
        brushsize = 20;
        lastBrushSize = brushsize;

        drawPath = new SerializablePath();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(brushsize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                drawCanvas.drawPath(drawPath, drawPaint);
                saveDrawMotion(drawPath, drawPaint);

                drawPath.reset();
            default:
                return false;
        }
        // Invalidate the view to get it to redraw (onDraw())
        invalidate();
        return true;
    }

    private void saveDrawMotion(SerializablePath drawPath, Paint drawPaint) {
        DrawMotion drawMotion = new DrawMotion(drawPath, drawPaint);
        mFirebaseDatabaseReference.child(DRAW_MOTION_CHILD).push().setValue(drawMotion);
    }

    private void setBrushsize(float size) {
        brushsize = size;
        lastBrushSize = brushsize;
        drawPaint.setStrokeWidth(size);

    }

    private float getBrushsize() {
        return brushsize;
    }

    private float getLastBrushSize() {
        return lastBrushSize;
    }

    public void setErasing(boolean isErasing) {
        this.isErasing = isErasing;

        if (isErasing) {
            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        } else {
            drawPaint.setXfermode(null);
        }

    }
}
