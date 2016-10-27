package ciu196.chalmers.se.armuseum.SampleApplication;

import android.graphics.Point;

import ciu196.chalmers.se.armuseum.RGBColor;
import ciu196.chalmers.se.armuseum.SampleApplication.utils.Pixel;

/**
 * Created by taich on 10/27/2016.
 */

public class Coordinate extends Pixel
{
    private int brushSize;

    public Coordinate(int x, int y, int brushSize, RGBColor color)
    {
        super(x, y, color);
        this.brushSize = brushSize;
    }

    public Coordinate(Point point, int brushSize, RGBColor color)
    {
        super(point, color);
        this.brushSize = brushSize;
    }

    public int getBrushSize()
    {
        return brushSize;
    }
}
