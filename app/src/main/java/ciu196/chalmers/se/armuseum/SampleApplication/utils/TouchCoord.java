package ciu196.chalmers.se.armuseum.SampleApplication.utils;

import android.opengl.Matrix;
import android.os.Debug;
import android.util.Log;

import ciu196.chalmers.se.armuseum.PaintRenderer;
import ciu196.chalmers.se.armuseum.RGBColor;

/**
 * Created by taich on 10/10/2016.
 */
public class TouchCoord
{
    private int x, y;
    private RGBColor color;

    public TouchCoord(int x, int y, RGBColor color)
    {
        set(x, y);
        this.color = color;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    // Set X and Y
    public void set(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public void set(int x, int y, RGBColor color) {
        this.set(x, y);
        this.color = color;
    }

    public RGBColor getColor() {
        return color;
    }

    @Override
    public String toString() {
        return "x: " + getX() + " y: " + getY();
    }
}
