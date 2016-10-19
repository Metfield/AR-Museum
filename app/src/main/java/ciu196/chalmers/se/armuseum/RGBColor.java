package ciu196.chalmers.se.armuseum;

import java.io.Serializable;

/**
 * Created by johnpetersson on 2016-10-12.
 */
public class RGBColor implements Serializable
{
    private int r, g, b;

    public RGBColor(int r, int g, int b)
    {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public RGBColor(RGBColor newColor)
    {
        this.r = newColor.r;
        this.g = newColor.g;
        this.b = newColor.b;
    }

    // DB constructor
    public RGBColor() {}
    //DB getters
    public int getR() {return r;}
    public int getG() {return g;}
    public int getB() {return b;}

    @Override
    public String toString() {
        return "R: " + getR() + " G: " + getG() + " B: " + getB();
    }
}