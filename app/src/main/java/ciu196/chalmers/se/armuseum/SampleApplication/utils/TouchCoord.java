package ciu196.chalmers.se.armuseum.SampleApplication.utils;

import android.opengl.Matrix;
import android.os.Debug;
import android.util.Log;

import ciu196.chalmers.se.armuseum.PaintRenderer;

/**
 * Created by taich on 10/10/2016.
 */
public class TouchCoord
{
    private int x, y;
//    private int u, v;

    public TouchCoord(int x, int y)
    {
        set(x, y);
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

    public int getU() { return TouchCoordQueue.convertX2U(x) - 1; }

    public int getV() { return TouchCoordQueue.convertY2V(y) - 1; }

    @Override
    public String toString() {
        return "x: " + getX() + " y: " + getY() + " u: " + getU() + " v: " + getV();
    }
}
