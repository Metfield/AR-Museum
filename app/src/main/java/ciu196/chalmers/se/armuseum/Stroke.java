package ciu196.chalmers.se.armuseum;

import java.io.Serializable;

/**
 * Created by johnpetersson on 2016-10-10.
 */
public class Stroke implements Serializable {
    private SerializablePath serializablePath;
    private RGBColor color;
    private double brushSize;
//    private Date timeStamp;

    public Stroke() {}
    public Stroke(SerializablePath serializablePath, RGBColor color, double brushSize) {
        this.serializablePath = serializablePath;
        this.color = color;
        this.brushSize = brushSize;
//        timeStamp = new Date();
    }

    public SerializablePath getSerializablePath() {return serializablePath;}
    public RGBColor getColor() {return color;}
    public double getBrushSize() {return brushSize;}
//    public Date getTimeStamp() {return timeStamp;}

}
