package ciu196.chalmers.se.armuseum;

import android.graphics.Point;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
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

    private TouchCoordQueue mTouchQueue = TouchCoordQueue.getInstance();

    public PaintManager(PaintRenderer renderer) {
        this.renderer = renderer;
        currentColor = new RGBColor(0, 0, 0);
        currentBrushSize = 20;

        strokeBacklog = new LinkedList();
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

    private void lineTo(Point point, boolean isDatabaseCall) {
//        Log.v(LOGTAG, "Drawing line:  " + point);

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
            startLine(start, color, brushSize, true);

            for (Point point: stroke.getSerializablePath().getPoints()) {
                lineTo(point, true);
            }
            finishLine(true);
        }
    }

    private void saveStrokeToDb(Stroke stroke) {
        mFirebaseDatabaseReference.child(STROKE_PATH_CHILD).push().setValue(stroke);
    }

    ChildEventListener strokeAddedListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//            if (dataSnapshot.child(STROKE_PATH_CHILD).exists()) {

                Stroke stroke = dataSnapshot.getValue(Stroke.class);
//            Log.v(LOGTAG, "Stroke added: " + stroke);

                    // Renderer is not yet initialized, add strokes to backlog to be rendered later
//                    if (renderer == null || renderer.getCanvasTexture() == null || !renderer.mIsTextureActive) {
//                        Log.v(LOGTAG, "Stroke added to backlog: " + stroke);
//                        strokeBacklog.add(stroke);
//
//                    } else {
//                        drawStrokesInBacklog();
                        Log.v(LOGTAG, "Stroke sent for drawing: " + stroke);
                        drawStroke(stroke);
//                    }
//            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//            Log.v(LOGTAG, "child changed");
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

//    ValueEventListener drawingDatabaseListener = new ValueEventListener()
//    {
//        @Override
//        public void onDataChange(DataSnapshot dataSnapshot)
//        {
//            Log.v(LOGTAG, "Datachanged: " + dataSnapshot);
//            // Database listener firing for every point added
//            if (dataSnapshot.child(STROKE_PATH_CHILD).exists())
//            {
//                Iterable<DataSnapshot> savedDrawPaths = dataSnapshot.child(STROKE_PATH_CHILD).getChildren();
//
//                Iterator<DataSnapshot> iterator = savedDrawPaths.iterator();
//                while (iterator.hasNext())
//                {
//                    Stroke stroke = iterator.next().getValue(Stroke.class);
//
//                    // Renderer is not yet initialized, add strokes to backlog to be rendered later
//                    if (renderer == null || renderer.getCanvasTexture() == null) {
//                        strokeBacklog.add(stroke);
//                    } else {
//                        drawStrokesInBacklog();
//                        drawStroke(stroke);
//                    }
//                }
//            }
//        }
//
//        @Override
//        public void onCancelled(DatabaseError databaseError)
//        {
//            Log.w(LOGTAG, databaseError.toException());
//        }
//    };

    public void connectToDb() {
        // Get all the previous strokes from the db
//        mFirebaseDatabaseReference.addListenerForSingleValueEvent(drawingDatabaseListener);
//        mFirebaseDatabaseReference.addValueEventListener(drawingDatabaseListener);
//        mFirebaseDatabaseReference.addChildEventListener(strokeAddedListener);
        DatabaseReference strokeChild = mFirebaseDatabaseReference.child(STROKE_PATH_CHILD);
        strokeChild.addChildEventListener(strokeAddedListener);
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