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

    private static final double DEFAULT_BRUSH_SIZE = 20;

    private static TouchCoordQueue instance;

    public static TouchCoordQueue getInstance() {
        if (instance == null) {
            instance = new TouchCoordQueue();
        }
        return instance;
    }

    private TouchCoordQueue() {
        queue = new LinkedBlockingQueue<>();
    }

    // Use Vector maybe?
    private Queue<TouchCoord> queue;

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