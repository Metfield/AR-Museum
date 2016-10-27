package ciu196.chalmers.se.armuseum.SampleApplication.utils;

import android.opengl.Matrix;
import android.os.Debug;
import android.util.Log;

import ciu196.chalmers.se.armuseum.PaintRenderer;

/**
 * Created by taich on 10/10/2016.
 */
public class TouchCoord extends Coordinate
{
//    private int u, v;

    public TouchCoord(int x, int y)
    {
        super (x, y);
    }

    @Override
    public int getU() { return TouchCoordQueue.convertX2U(getX()) - 1; }

    @Override
    public int getV() { return TouchCoordQueue.convertY2V(getY()) - 1; }

    @Override
    public String toString() {
        return "x: " + getX() + " y: " + getY() + " u: " + getU() + " v: " + getV();
    }
}
