package ciu196.chalmers.se.armuseum.SampleApplication.utils;

import java.nio.Buffer;

/**
 * Created by Emmanuel on 10/5/2016.
 */
public class CanvasMesh extends MeshObject
{
    private static double depth = -1.0f;

    private static final double vertices[] =
    {
        -1.0f, -1.00, depth,
         1.0f, -1.0f, depth,
         1.0f,  1.0f, depth,
        -1.0f,  1.0,  depth
    };

    private static final double texcoords[] =
    {
        0, 0,
        1, 0,
        1, 1,
        0, 1
    };

    private static final double normals[] =
    {
        0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1
    };

    private static final short indices[] =
    {
        0, 1, 2,
        0, 2, 3
    };

    private Buffer mVertBuff;
    private Buffer mTexCoordBuff;
    private Buffer mIndBuff;
    private Buffer mNormBuff;

    public CanvasMesh()
    {
        mVertBuff = fillBuffer(vertices);
        mTexCoordBuff = fillBuffer(texcoords);
        mIndBuff = fillBuffer(indices);
        mNormBuff = fillBuffer(normals);
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
