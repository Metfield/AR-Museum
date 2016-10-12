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
    protected int x, y;
    protected int u, v;

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

        this.u =  TouchCoordQueue.convertX2U(x) - 1;
        this.v =  TouchCoordQueue.convertY2V(y) - 1;


        // Transform touch coordinates to viewport space [-1, 1]
        Vec4 viewport_coords = new Vec4( (2.0f * x) / TouchCoordQueue.VIEWPORT_WIDTH - 1.0f,
                1.0f - (2.0f * y) / TouchCoordQueue.VIEWPORT_HEIGHT,
                1.0f,
                1.0f );

        float[] view_coords = new float[4];

       // Matrix.multiplyMV(view_coords, 0, PaintRenderer.getProjectionInverseMatrix());

        Log.e("blah", "Transformed: " + viewport_coords.x
                + " " + viewport_coords.y
                + " " + viewport_coords.z
                + " " + viewport_coords.w);

//        PaintRenderer.mProjectionInverseMatrix
    }

    public int getU()
    {
        return this.u;
    }

    public int getV()
    {
        return this.v;
    }
}
