package ciu196.chalmers.se.armuseum;

import java.io.Serializable;

/**
 * Created by johnpetersson on 2016-10-12.
 */
public class RGBColor implements Serializable
{
    public byte r, g, b;

    RGBColor(byte r, byte g, byte b)
    {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    RGBColor(RGBColor newColor)
    {
        this.r = newColor.r;
        this.g = newColor.g;
        this.b = newColor.b;
    }
}