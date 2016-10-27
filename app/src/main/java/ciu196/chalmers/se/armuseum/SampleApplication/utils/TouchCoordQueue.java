package ciu196.chalmers.se.armuseum.SampleApplication.utils;

import android.util.Log;

import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import ciu196.chalmers.se.armuseum.RGBColor;

/**
 * Created by taich on 10/10/2016.
 */
public class TouchCoordQueue
{
    private static final String LOGTAG = "TouchCoordQueue";

    private static final RGBColor DEFAULT_COLOR = new RGBColor(0, 0, 0);
    //    private static final double DEFAULT_BRUSH_SIZE = 20;

    private static TouchCoordQueue instance;

    public static TouchCoordQueue getInstance() {
        if (instance == null) {
            instance = new TouchCoordQueue();
        }
        return instance;
    }

    private TouchCoordQueue() {
        queue = new LinkedBlockingQueue<>();
        currentColor = DEFAULT_COLOR;
    }

    // Use Vector maybe?
    private Queue<TouchCoord> queue;

    public static int VIEWPORT_WIDTH, VIEWPORT_HEIGHT;
    public static int TEXTURE_SIZE;

    private RGBColor currentColor ;
    private double currentBrushSize;

    public void push(TouchCoord tc)
    {
        queue.add(tc);
    }

    public TouchCoord pop()
    {
        TouchCoord tc = queue.poll();
        return tc;
    }

    public int getSize()
    {
        return queue.size();
    }

    public static int convertX2U(int value)
    {
        double scale = (double)(TEXTURE_SIZE) / (VIEWPORT_WIDTH );
        return (int)(value * scale);
    }

    public static int convertY2V(int value)
    {
        double scale = (double)(TEXTURE_SIZE) / (VIEWPORT_HEIGHT );
        return (int)(value * scale);
    }

    public RGBColor getColor()
    {
        return currentColor;
    }

    public void setColor(RGBColor color)
    {
        currentColor = color;
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
        currentColor = DEFAULT_COLOR;
    }

    public boolean isReady() {
        return TEXTURE_SIZE > 0 && VIEWPORT_HEIGHT > 0 && VIEWPORT_WIDTH > 0;
    }
}
