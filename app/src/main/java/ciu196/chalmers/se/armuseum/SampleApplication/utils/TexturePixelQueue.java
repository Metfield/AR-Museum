package ciu196.chalmers.se.armuseum.SampleApplication.utils;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by taich on 10/10/2016.
 */
public class TexturePixelQueue
{
    private static final String LOGTAG = "TexturePixelQueue";

    private static final double DEFAULT_BRUSH_SIZE = 20;

    private static TexturePixelQueue instance;

    public static TexturePixelQueue getInstance() {
        if (instance == null) {
            instance = new TexturePixelQueue();
        }
        return instance;
    }

    private TexturePixelQueue() {
        queue = new LinkedBlockingQueue<>();
    }

    // Use Vector maybe?
    private Queue<Pixel> queue;

    private double currentBrushSize;

    public void push(Pixel tc)
    {
        queue.add(tc);
    }

    public Pixel pop()
    {
        Pixel tc = queue.poll();
        return tc;
    }

    public int getSize()
    {
        return queue.size();
    }

    public double getBrushSize()
    {
        return currentBrushSize;
    }

    public void setBrushSize(double size)
    {
        currentBrushSize = size;
    }

    public void reset()
    {
        currentBrushSize = 0;
    }


}