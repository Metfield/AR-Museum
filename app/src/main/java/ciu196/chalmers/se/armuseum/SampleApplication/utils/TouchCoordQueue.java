package ciu196.chalmers.se.armuseum.SampleApplication.utils;

import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by taich on 10/10/2016.
 */
public class TouchCoordQueue
{
    // Use Vector maybe?
    private static Queue<TouchCoord> queue = new LinkedBlockingQueue<>();
    public static int VIEWPORT_WIDTH, VIEWPORT_HEIGHT;

    public static int TEXTURE_SIZE;

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
}
