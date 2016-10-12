/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package ciu196.chalmers.se.armuseum.SampleApplication.utils;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ciu196.chalmers.se.armuseum.MainActivity;
import ciu196.chalmers.se.armuseum.PaintRenderer;
import ciu196.chalmers.se.armuseum.RGBColor;


// Support class for the Vuforia samples applications.
// Exposes functionality for loading a texture from the APK.
public class Texture
{
    private static final String LOGTAG = "Vuforia_Texture";
    
    public int mWidth;          // The width of the texture.
    public int mHeight;         // The height of the texture.
    public int mChannels;       // The number of channels.
    public ByteBuffer mData;    // The pixel data.
    public int[] mTextureID = new int[1];
    public boolean mSuccess = false;

    public ByteBuffer mTempBuffer;
    public int mNumPixels;
    public byte[] mDataBytes;

    public int mBufferSize;
    private TouchCoordQueue mTouchQueue;

    private RGBColor mBrushColor;
    private double mBrushSize;

    /* Factory function to load a texture from the APK. */
    public static Texture loadTextureFromApk(String fileName,
        AssetManager assets)
    {
        InputStream inputStream = null;
        try
        {
            inputStream = assets.open(fileName, AssetManager.ACCESS_BUFFER);
            
            BufferedInputStream bufferedStream = new BufferedInputStream(
                inputStream);
            Bitmap bitMap = BitmapFactory.decodeStream(bufferedStream);
            
            int[] data = new int[bitMap.getWidth() * bitMap.getHeight()];
            bitMap.getPixels(data, 0, bitMap.getWidth(), 0, 0,
                bitMap.getWidth(), bitMap.getHeight());
            
            return loadTextureFromIntBuffer(data, bitMap.getWidth(),
                bitMap.getHeight());
        } catch (IOException e)
        {
            Log.e(LOGTAG, "Failed to log texture '" + fileName + "' from APK");
            Log.i(LOGTAG, e.getMessage());
            return null;
        }
    }
    
    
    public static Texture loadTextureFromIntBuffer(int[] data, int width, int height)
    {
        // Convert:
        int numPixels = width * height;
        byte[] dataBytes = new byte[numPixels * 4];
        
        for (int p = 0; p < numPixels; ++p)
        {
            int colour = data[p];
            dataBytes[p * 4] = (byte) (colour >>> 16); // R
            dataBytes[p * 4 + 1] = (byte) (colour >>> 8); // G
            dataBytes[p * 4 + 2] = (byte) colour; // B
            dataBytes[p * 4 + 3] = (byte) (colour >>> 24); // A
        }
        
        Texture texture = new Texture();
        texture.mWidth = width;
        texture.mHeight = height;
        texture.mChannels = 4;

        // Eman
        texture.mNumPixels = numPixels;
        texture.mDataBytes = dataBytes;
        
        texture.mData = ByteBuffer.allocateDirect(dataBytes.length).order(ByteOrder.nativeOrder());

        // Also initialize tempBuffer
        texture.mTempBuffer = ByteBuffer.allocateDirect(dataBytes.length).order(ByteOrder.nativeOrder());

        int rowSize = texture.mWidth * texture.mChannels;

        for (int r = 0; r < texture.mHeight; r++)
        {
            texture.mData.put(dataBytes, rowSize * (texture.mHeight - 1 - r), rowSize);
    }

        texture.mBufferSize = texture.mData.position();
        texture.mData.rewind();
        
        // Cleans variables
        dataBytes = null;
        data = null;
        
        texture.mSuccess = true;
        return texture;
    }

    public void updatePixels()
    {
       /* for (int p = 0; p < mNumPixels; ++p)
        {
            mDataBytes[p * 4] = (byte) (this.mBrushColor.r >>> 16); // R
            mDataBytes[p * 4 + 1] = (byte) (this.mBrushColor.g >>> 8); // G
            mDataBytes[p * 4 + 2] = (byte) this.mBrushColor.b; // B
            mDataBytes[p * 4 + 3] = (byte) (255 >>> 24); // A
        }

        int rowSize = mWidth * mChannels;

        for (int r = 0; r < mHeight; r++)
        {
            mTempBuffer.put(mDataBytes, rowSize * (mHeight - 1 - r), rowSize);
        }

        mData = mTempBuffer;
        mTempBuffer.rewind();*/

        TouchCoord tc;
        int u, v;
        int offset;
        int memPitch;

        setBrushColor(mTouchQueue.getColor());
        // TODO: Use brush size
        mBrushSize = mTouchQueue.getBrushSize();

        while(mTouchQueue.getSize() > 0)
        {
            tc = mTouchQueue.pop();

            if(tc == null)
            {
                return;
            }

            // Get u,v coordinates
            u = tc.getU();
            v = mHeight - tc.getV();

//            Log.e("blah","Raw v: " + v);

            // Clamp v value to [0, tex_1D_size-1]
            v = Math.max(0, Math.min(mHeight - 1, v));

//            Log.e("blah", "x: " + tc.getX() +" y: " + tc.getY() + "   U: " + u + " v: " + v);

            memPitch = mWidth * mChannels;
            offset = u + (v * memPitch);

            mData.put(offset, (byte)mBrushColor.getR());
            mData.put(offset + 1, (byte)mBrushColor.getG());
            mData.put(offset + 2, (byte)mBrushColor.getB());
            mData.put(offset + 3, (byte)255);
        }

/*        for(int i = 0; i < mBufferSize-3; i+=4)
        {
            mData.put(i, (byte)128);
            mData.put(i+1, (byte)255);
            mData.put(i+2, (byte)64);
            mData.put(i+3, (byte)255);
        }*/
    }

    private void setBrushColor(RGBColor newColor)
    {
        this.mBrushColor = newColor;
    }
}
