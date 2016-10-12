package ciu196.chalmers.se.armuseum;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by johnpetersson on 2016-10-10.
 */
public class Stroke implements Serializable {
    private SerializablePath drawingPath;
    private RGBColor color;
    private double brushSize;
    private Date timeStamp;

    public Stroke() {}
    public Stroke(SerializablePath drawPath, RGBColor color, double brushSize) {
        this.drawingPath = drawPath;
        this.color = color;
        this.brushSize = brushSize;
        timeStamp = new Date();
    }

    public SerializablePath getDrawingPath() {return drawingPath;}
    public RGBColor getColor() {return color;}
    public double getBrushSize() {return brushSize;}
    public Date getTimeStamp() {return timeStamp;}

}
