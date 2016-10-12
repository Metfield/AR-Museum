package ciu196.chalmers.se.armuseum.SampleApplication.utils;

/**
 * Created by taich on 10/10/2016.
 */
public class TouchCoord
{
    protected int x, y;
    protected int u, v;

    public TouchCoord(int x, int y)
    {
        this.x = x;
        this.y = y;

        this.u =  TouchCoordQueue.convertX2U(x) - 1;
        this.v =  TouchCoordQueue.convertY2V(y) - 1;
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
