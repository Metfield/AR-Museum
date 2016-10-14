package ciu196.chalmers.se.armuseum.SampleApplication.utils;

/**
 * Created by taich on 10/12/2016.
 */
public class Vec4
{
    public float x, y, z, w;

    public Vec4(float x, float y, float z, float w)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vec4() {}

    public float[] getFloatArray()
    {
        return new float[] {this.x, this.y, this.z, this.w};
    }
}
