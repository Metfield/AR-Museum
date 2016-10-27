package ciu196.chalmers.se.armuseum.SampleApplication.utils;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import ciu196.chalmers.se.armuseum.RGBColor;

/**
 * Created by johnpetersson on 2016-10-27.
 */
public class CoordinateQueue {
    private static final String LOGTAG = "CoordinateQueue";

    private static final RGBColor DEFAULT_COLOR = new RGBColor(0, 0, 0);
    //    private static final double DEFAULT_BRUSH_SIZE = 20;

    private static CoordinateQueue instance;

    public static CoordinateQueue getInstance() {
        if (instance == null) {
            instance = new CoordinateQueue();
        }
        return instance;
    }

    CoordinateQueue() {
        queue = new LinkedBlockingQueue<>();
        currentColor = DEFAULT_COLOR;
    }

    // Use Vector maybe?
    private Queue<TouchCoord> queue;

    public static int VIEWPORT_WIDTH, VIEWPORT_HEIGHT;
    public static int TEXTURE_SIZE;

    private RGBColor currentColor ;
    private double currentBrushSize;

    public void push(TouchCoord coordinate)
    {
        queue.add(coordinate);
    }

    public TouchCoord pop()
    {
        TouchCoord coordinate = queue.poll();
        return coordinate;
    }

    public int getSize()
    {
        return queue.size();
    }
//
//    public static int convertX2U(int value)
//    {
//        double scale = (double)(TEXTURE_SIZE) / (VIEWPORT_WIDTH );
//        return (int)(value * scale);
//    }
//
//    public static int convertY2V(int value)
//    {
//        double scale = (double)(TEXTURE_SIZE) / (VIEWPORT_HEIGHT );
//        return (int)(value * scale);
//    }

//    public RGBColor getColor()
//    {
//        return currentColor;
//    }
//
//    public void setColor(RGBColor color)
//    {
//        currentColor = color;
//    }

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
