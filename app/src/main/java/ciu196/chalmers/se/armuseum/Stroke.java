package ciu196.chalmers.se.armuseum;

import java.io.Serializable;

/**
 * Created by johnpetersson on 2016-10-10.
 */
public class Stroke implements Serializable {
    private SerializablePath serializablePath;
    private RGBColor color;
    private int brushSize;

    public Stroke() {}
    public Stroke(SerializablePath serializablePath, RGBColor color, int brushSize) {
        this.serializablePath = serializablePath;
        this.color = color;
        this.brushSize = brushSize;
    }

    public SerializablePath getSerializablePath() {return serializablePath;}
    public RGBColor getColor() {return color;}
    public int getBrushSize() {return brushSize;}
}
