package ciu196.chalmers.se.armuseum.SampleApplication.utils;

import java.nio.Buffer;

/**
 * Created by taich on 10/19/2016.
 */
public class RayMesh extends MeshObject
{
    private static double front = -1;
    private static double back = -5;

    private static double vertices[]  = {
            0.5f, 0.5f, front, // origin
            0.5f, 0.5f, front,
            0.5f, 0.5f, front,

            0.5f, 0.5f, back,
            0.5f, 0.5f, back, // destination
            0.5f, 0.5f, back,
    };

    private static final short  indices[]   = {
            0, 1, 2, // origin
            3, 4, 5 // destination
    };

    private Buffer mVertBuff;
    private Buffer mIndBuff;

    public RayMesh()
    {
        mVertBuff = fillBuffer(vertices);
        mIndBuff = fillBuffer(indices);
    }

    public void setOrigin(double x, double y, double z)
    {
        vertices[0] = x;
        vertices[1] = y;
        vertices[2] = z;

        mVertBuff = fillBuffer(vertices);
    }

    public void setDestination(double x, double y, double z)
    {
        vertices[3] = x;
        vertices[4] = y;
        vertices[5] = z;

        mVertBuff = fillBuffer(vertices);
    }

    @Override
    public Buffer getBuffer(MeshObject.BUFFER_TYPE bufferType)
    {
        Buffer result = null;
        switch (bufferType)
        {
            case BUFFER_TYPE_VERTEX:
                result = mVertBuff;
                break;
            case BUFFER_TYPE_INDICES:
                result = mIndBuff;
                break;
            default:
                break;
        }
        return result;
    }

    @Override
    public int getNumObjectVertex()
    {
        return vertices.length / 3;
    }

    @Override
    public int getNumObjectIndex()
    {
        return indices.length;
    }
}

