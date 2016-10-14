package ciu196.chalmers.se.armuseum.SampleApplication.utils;

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
    private static final RGBColor DEFAULT_COLOR = new RGBColor(0, 0, 0);
//    private static final double DEFAULT_BRUSH_SIZE = 20;


    // Use Vector maybe?
    private static Queue<TouchCoord> queue = new LinkedBlockingQueue<>();
    public static int VIEWPORT_WIDTH, VIEWPORT_HEIGHT;

    public static int TEXTURE_SIZE;

    private static RGBColor currentColor = DEFAULT_COLOR;
    private static double currentBrushSize;

    public static void push(TouchCoord tc)
    {
        queue.add(tc);
    }

    public static TouchCoord pop()
    {
        return queue.poll();
    }

    public static int getSize()
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

    public static RGBColor getColor()
    {
        return currentColor;
    }

    public static void setColor(RGBColor color)
    {
        currentColor = color;
    }

    public static double getBrushSize()
    {
        return currentBrushSize;
    }

    public static void setBrushSize(double size)
    {
        currentBrushSize = size;
    }

    public static void reset()
    {
        currentBrushSize = 0;
        currentColor = DEFAULT_COLOR;
        queue.clear();
    }
}
