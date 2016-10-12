package ciu196.chalmers.se.armuseum;

import android.graphics.Paint;
import android.graphics.Path;

import java.sql.Time;
import java.util.Date;

/**
 * Created by johnpetersson on 2016-10-10.
 */
public class DrawMotion {

    private SerializablePath drawPath;
//    private Paint drawPaint;
//    private Date timeStamp;

    public DrawMotion() {

    }

    public DrawMotion(SerializablePath drawPath) {
        this.drawPath = drawPath;
    }

    public DrawMotion(SerializablePath drawPath, Paint drawPaint) {
        this.drawPath = drawPath;
//        this.drawPaint = drawPaint;
//        this.timeStamp = new Date();
    }

    public SerializablePath getDrawPath() {
        return drawPath;
    }
}