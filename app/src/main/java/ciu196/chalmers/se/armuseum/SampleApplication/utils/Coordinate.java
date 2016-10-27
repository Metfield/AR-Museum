package ciu196.chalmers.se.armuseum.SampleApplication.utils;

/**
 * Created by johnpetersson on 2016-10-27.
 */
public abstract class Coordinate {
    private int x, y;

    public Coordinate(int x, int y)
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

    public int getU() { return getX(); }

    public int getV() { return getY(); }

    @Override
    public String toString() {
        return "x: " + getX() + " y: " + getY(); //+ " u: " + getU() + " v: " + getV();
    }
}
