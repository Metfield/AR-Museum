package ciu196.chalmers.se.armuseum;

import android.graphics.Point;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import ciu196.chalmers.se.armuseum.SampleApplication.utils.TouchCoord;


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

    private static PaintManager mInstance;

    public static PaintManager getInstance(PaintRenderer renderer)
    {
        if(mInstance == null)
        {
            mInstance = new PaintManager(renderer);
        }

        return mInstance;
    }

    private PaintManager(PaintRenderer renderer) {
        this.renderer = renderer;
        currentColor = new RGBColor(0, 0, 0);
        currentBrushSize = 20;

//        strokeBacklog = new LinkedList();
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public void startLine(Point point, RGBColor color, double brushSize) {
        startLine(point, color, brushSize, false);
    }

    // Private version to avoid database loops
    private void startLine(Point point, RGBColor color, double brushSize, boolean isDatabaseCall) {
        currentColor = color;
        currentBrushSize = brushSize;


        // Don't save in database if the call was triggered from database listener
        if (!isDatabaseCall) {
            // transform viewport coordinates to texture coordinates
            point = convertToCanvasCoordinates(point);

            // For db
            drawingPath = new SerializablePath();
            addTexturePointToDrawingPath(point);
        }
        TouchCoord tc = new TouchCoord(point.x, point.y, currentColor);
        renderer.addTouchToQueue(tc, currentBrushSize);
    }

    public void lineTo(Point point) {
        lineTo(point, false);
    }

    private void lineTo(Point point, boolean isDatabaseCall) {


        if (!isDatabaseCall) {
            // transform viewport coordinates to texture coordinates
            point = convertToCanvasCoordinates(point);

            // For db
            addTexturePointToDrawingPath(point);
        }
        TouchCoord tc = new TouchCoord(point.x, point.y, currentColor);
        renderer.addTouchToQueue(tc);
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
            drawStroke(stroke);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

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

    public void connectToDb() {
        // Get all the previous strokes from the db
//        mFirebaseDatabaseReference.addListenerForSingleValueEvent(drawingDatabaseListener);
        DatabaseReference strokeChild = mFirebaseDatabaseReference.child(STROKE_PATH_CHILD);
        strokeChild.addChildEventListener(strokeAddedListener);
    }

    public void setRenderer(PaintRenderer renderer) {
        this.renderer = renderer;
    }

    private void addTexturePointToDrawingPath(Point texturePoint) {
        //Point canvasPoint = convertToCanvasCoordinates(texturePoint);
        drawingPath.addPoint(texturePoint);
    }

    // TODO: Make this function convert screen space into drawing space
    private Point convertToCanvasCoordinates(Point point)
    {
        point.x = convertX2U(point.x);
        point.y = convertY2V(point.y);

        return point;
    }

    private int convertX2U(int value)
    {
        double scale = (double)(getTextureSize()) / (renderer.VIEWPORT_WIDTH );
        return (int)(value * scale);
    }

    private int convertY2V(int value)
    {
        double scale = (double)(getTextureSize()) / (renderer.VIEWPORT_HEIGHT);
        return (int)(value * scale);
    }

    private double getTextureSize()
    {
        return renderer.mCanvasTexture.mWidth - 1;
    }

    // Converts canvas space into screen space for sending to drawing queue
    // Should maybe be in renderer
    // TODO: implement
    private Point convertToScreenCoordinates(Point point) {
        return point;
    }

    public boolean isReady() {
        return getTextureSize() > 0 && renderer.VIEWPORT_HEIGHT > 0 && renderer.VIEWPORT_WIDTH > 0;
    }

}