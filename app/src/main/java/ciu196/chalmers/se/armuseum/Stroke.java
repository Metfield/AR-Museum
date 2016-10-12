package ciu196.chalmers.se.armuseum;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by johnpetersson on 2016-10-10.
 */
public class Stroke implements Serializable {
    private SerializablePath drawingPath;
    private RGBColor color;
    private Date timeStamp;

    public Stroke() {}
    public Stroke(SerializablePath drawPath, RGBColor color) {
        this.drawingPath = drawPath;
        this.color = color;
        timeStamp = new Date();
    }

    public SerializablePath getDrawingPath() {return drawingPath;}
    public RGBColor getColor() {return color;}
    public Date getTimeStamp() {return timeStamp;}

}
