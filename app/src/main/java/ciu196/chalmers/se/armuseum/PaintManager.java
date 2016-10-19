package ciu196.chalmers.se.armuseum;

import android.graphics.Point;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import ciu196.chalmers.se.armuseum.SampleApplication.utils.TouchCoord;
import ciu196.chalmers.se.armuseum.SampleApplication.utils.TouchCoordQueue;

/**
 * Created by johnpetersson on 2016-10-19.
 */
public class PaintManager {

    public static final String LOGTAG = "PaintManager";
    public static final String STROKE_PATH_CHILD = "stroke";

    private PaintRenderer renderer;

    private RGBColor currentColor;
    private double currentBrushSize;

    private SerializablePath drawingPath;

    private DatabaseReference mFirebaseDatabaseReference;

    private Queue<Stroke> strokeBacklog;

    public PaintManager(PaintRenderer renderer) {
        this.renderer = renderer;
        currentColor = new RGBColor(0, 0, 0);
        currentBrushSize = 20;

        strokeBacklog = new LinkedList<Stroke>();
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public void startLine(Point point, RGBColor color, double brushSize) {
        startLine(point, color, brushSize, false);
    }

    // Private version to avoid database loops
    private void startLine(Point point, RGBColor color, double brushSize, boolean isDatabaseCall) {
        currentColor = color;
        currentBrushSize = brushSize;

        TouchCoord touchCoord = new TouchCoord(point.x, point.y);
        renderer.addTouchToQueue(touchCoord, currentColor, currentBrushSize);

        // Don't save in database if the call was triggered from database listener
        if (!isDatabaseCall) {
            // For db
            drawingPath = new SerializablePath();
            drawingPath.addPoint(point);
        }
    }

    public void lineTo(Point point) {
        lineTo(point, false);
    }

    public void lineTo(Point point, boolean isDatabaseCall) {
        renderer.addTouchToQueue(new TouchCoord(point.x, point.y));

        if (!isDatabaseCall) {
            // For db
            drawingPath.addPoint(point);

        }
    }

    public void finishLine() {
        finishLine(false);
    }


    private void finishLine(boolean isDatabaseCall) {
        TouchCoordQueue.reset();
        renderer.clearTrail();

        if (!isDatabaseCall) {
            Stroke stroke = new Stroke(drawingPath, currentColor, currentBrushSize);
            saveStrokeToDb(stroke);
            drawingPath.reset();
        }
    }

    // Used to draw full strokes from the db
    private void drawStroke(Stroke stroke) {
        RGBColor color = stroke.getColor();
        double brushSize = stroke.getBrushSize();

        SerializablePath path = stroke.getSerializablePath();

        if (path != null) {

            Point start = stroke.getSerializablePath().getStartingPoint();
//        TouchCoord tempTouchCoord = new TouchCoord(start.x, start.y);

            startLine(start, color, brushSize, true);


//        renderer.addTouchToQueue(tempTouchCoord, color, brushSize);

            for (Point point: stroke.getSerializablePath().getPoints()) {
                lineTo(point, true);
//            tempTouchCoord.set(point.x, point.y);
//
//            renderer.addTouchToQueue(tempTouchCoord);
//                        Log.v(LOGTAG, point.x + " " + point.y);
            }
            finishLine(true);
        }
    }


    private void saveStrokeToDb(Stroke stroke) {
        mFirebaseDatabaseReference.child(STROKE_PATH_CHILD).push().setValue(stroke);
    }

    ValueEventListener drawingDatabaseListener = new ValueEventListener()
    {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot)
        {
            // Database listener firing for every point added
            if (dataSnapshot.child(STROKE_PATH_CHILD).exists())
            {
//                Log.v(LOGTAG, "Event from db");
                Iterable<DataSnapshot> savedDrawPaths = dataSnapshot.child(STROKE_PATH_CHILD).getChildren();

                Iterator<DataSnapshot> iterator = savedDrawPaths.iterator();
                while (iterator.hasNext())
                {
                    Stroke stroke = iterator.next().getValue(Stroke.class);

                    // Renderer is not yet initialized, add strokes to backlog to be rendered later
                    if (renderer == null) {
                        strokeBacklog.add(stroke);
                    } else {
                        drawStrokesInBacklog();
                        drawStroke(stroke);
                    }


//                    RGBColor color = stroke.getColor();
//                    double brushSize = stroke.getBrushSize();

//                    List<Point> points = stroke.getSerializablePath().getPoints();
//                    TouchCoord tempTouchCoord = new TouchCoord(points.get(0).x, points.get(0).y);
//                    renderer.addTouchToQueue(tempTouchCoord, color, brushSize);
//
//                    for (Point point: points) {
//                        tempTouchCoord.set(point.x, point.y);
//
//                        renderer.addTouchToQueue(tempTouchCoord);
////                        Log.v(LOGTAG, point.x + " " + point.y);
//                    }
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError)
        {
            Log.w(LOGTAG, databaseError.toException());
        }
    };

    public void connectToDb() {
//        mFirebaseDatabaseReference.addListenerForSingleValueEvent(drawingDatabaseListener);
        mFirebaseDatabaseReference.addValueEventListener(drawingDatabaseListener);
    }

    public void setRenderer(PaintRenderer renderer) {
        this.renderer = renderer;
    }

    private void drawStrokesInBacklog() {
        while(!strokeBacklog.isEmpty()) {
            drawStroke(strokeBacklog.poll());
        }
    }

}
