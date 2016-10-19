package ciu196.chalmers.se.armuseum.SampleApplication.utils;

import java.nio.Buffer;

/**
 * Created by Emmanuel on 10/5/2016.
 */
public class CanvasMesh extends MeshObject
{
    private static double back = -1.0f;
    private static double front = back + 0.2f;

    private static final double vertices[]  = {
            -1.00f, -1.00f, front, // front
            1.00f, -1.00f, front,
            1.00f, 1.00f, front,
            -1.00f, 1.00f, front,

            -1.00f, -1.00f, back, // back
            1.00f, -1.00f, back,
            1.00f, 1.00f, back,
            -1.00f, 1.00f, back,

            -1.00f, -1.00f, back, // left
            -1.00f, -1.00f, front,
            -1.00f, 1.00f, front,
            -1.00f, 1.00f, back,

            1.00f, -1.00f, back, // right
            1.00f, -1.00f, front,
            1.00f, 1.00f, front,
            1.00f, 1.00f, back,

            -1.00f, 1.00f, front, // top
            1.00f, 1.00f, front,
            1.00f, 1.00f, back,
            -1.00f, 1.00f, back,

            -1.00f, -1.00f, front, // bottom
            1.00f, -1.00f, front,
            1.00f, -1.00f, back,
            -1.00f, -1.00f, back };


    private static final double texcoords[] = {
            0, 0, 1, 0, 1, 1, 0, 1,

            1, 0, 0, 0, 0, 1, 1, 1,

            0, 0, 1, 0, 1, 1, 0, 1,

            1, 0, 0, 0, 0, 1, 1, 1,

            0, 0, 1, 0, 1, 1, 0, 1,

            1, 0, 0, 0, 0, 1, 1, 1 };


    private static final double normals[]   = {
            0, 0, 1,  0, 0, 1,  0, 0, 1,  0, 0, 1,

            0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1,

            -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0,

            1, 0, 0,  1, 0, 0,  1, 0, 0,  1, 0, 0,

            0, 1, 0,  0, 1, 0,  0, 1, 0,  0, 1, 0,

            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,
    };

    private static final short  indices[]   = {
            0, 1, 2, 0, 2, 3, // front
            4, 6, 5, 4, 7, 6, // back
            8, 9, 10, 8, 10, 11, // left
            12, 14, 13, 12, 15, 14, // right
            16, 17, 18, 16, 18, 19, // top
            20, 22, 21, 20, 23, 22  // bottom
    };

    private Buffer mVertBuff;
    private Buffer mTexCoordBuff;
    private Buffer mNormBuff;
    private Buffer mIndBuff;


    public CanvasMesh()
    {
        mVertBuff = fillBuffer(vertices);
        mTexCoordBuff = fillBuffer(texcoords);
        mNormBuff = fillBuffer(normals);
        mIndBuff = fillBuffer(indices);
    }


    @Override
    public Buffer getBuffer(BUFFER_TYPE bufferType)
    {
        Buffer result = null;
        switch (bufferType)
        {
            case BUFFER_TYPE_VERTEX:
                result = mVertBuff;
                break;
            case BUFFER_TYPE_TEXTURE_COORD:
                result = mTexCoordBuff;
                break;
            case BUFFER_TYPE_INDICES:
                result = mIndBuff;
                break;
            case BUFFER_TYPE_NORMALS:
                result = mNormBuff;
            default:
                break;
        }
        return result;
    }

    public float getFrontFaceDepth()
    {
        return (float)this.front;
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
