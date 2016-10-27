package ciu196.chalmers.se.armuseum.SampleApplication.utils;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.opengl.Matrix;
import android.os.Debug;
import android.util.Log;

import ciu196.chalmers.se.armuseum.PaintRenderer;
import ciu196.chalmers.se.armuseum.RGBColor;

/**
 * Created by taich on 10/10/2016.
 */
@SuppressLint("ParcelCreator")
public class Pixel extends Point
{
    private RGBColor color;

    public Pixel(Point point, RGBColor color) {
        super(point);
        this.color = color;
    }

    public Pixel(int x, int y, RGBColor color)
    {
        super(x, y);
        this.color = color;
    }

    public void set(int x, int y, RGBColor color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public RGBColor getColor() {
        return color;
    }

    @Override
    public String toString() {
        return "x: " + this.x + " y: " + this.y;
    }
}
