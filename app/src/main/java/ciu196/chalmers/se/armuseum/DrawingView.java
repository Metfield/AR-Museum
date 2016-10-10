package ciu196.chalmers.se.armuseum;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.vuforia.Matrix44F;
import com.vuforia.Vec2F;
import com.vuforia.Vec3F;

import java.util.jar.Attributes;

import ciu196.chalmers.se.armuseum.SampleApplication.utils.SampleMath;

/**
 * Created by johnpetersson on 2016-10-05.
 */
public class DrawingView extends View {

    private Path drawPath;
    private Paint drawPaint, canvasPaint;
    private int paintColor = 0xFF550000;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;



    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(20);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);
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
                drawPath.reset();
            default:
                return false;
        }
        // Invalidate the view to get it to redraw (onDraw())
        invalidate();
        return true;
    }

//    private Vec3F getPointIn3DPlane(Vec2F point) {
//        Matrix44F inverseProjMatrix =
//        Matrix44F modelViewMatrix
//        float screenWidth
//        float screenHeight
//        Vec2F point
//        Vec3F planeCenter
//        Vec3F planeNormal
//
//        return SampleMath.getPointToPlaneIntersection(inverseProjMatrix, modelViewMatrix, screenWidth, screenHeight, point, planeCenter, planeNormal);
//    }

}
