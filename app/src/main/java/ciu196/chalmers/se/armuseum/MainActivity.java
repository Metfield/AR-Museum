package ciu196.chalmers.se.armuseum;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Debug;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rtugeek.android.colorseekbar.ColorSeekBar;
import com.vuforia.CameraDevice;
import com.vuforia.DataSet;
import com.vuforia.ObjectTracker;
import com.vuforia.STORAGE_TYPE;
import com.vuforia.State;
import com.vuforia.Trackable;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vuforia;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import ciu196.chalmers.se.armuseum.SampleApplication.SampleApplicationControl;
import ciu196.chalmers.se.armuseum.SampleApplication.SampleApplicationException;
import ciu196.chalmers.se.armuseum.SampleApplication.SampleApplicationSession;
import ciu196.chalmers.se.armuseum.SampleApplication.utils.LoadingDialogHandler;
import ciu196.chalmers.se.armuseum.SampleApplication.utils.SampleApplicationGLView;
import ciu196.chalmers.se.armuseum.SampleApplication.utils.Texture;
import ciu196.chalmers.se.armuseum.SampleApplication.utils.TouchCoord;
import ciu196.chalmers.se.armuseum.SampleApplication.utils.TouchCoordQueue;

public class MainActivity extends Activity implements SampleApplicationControl
{
    private static final String LOGTAG = "MainActivity";

    private boolean dropDatabaseOnStart = true;

    // Firebase instance variables
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    public static final String STROKE_PATH_CHILD = "stroke";

    SampleApplicationSession vuforiaAppSession;

    private DataSet mCurrentDataset;
    private int mCurrentDatasetSelectionIndex = 1;
    private int mStartDatasetsIndex = 0;
    private int mDatasetsNumber = 0;
    private ArrayList<String> mDatasetStrings = new ArrayList<String>();

    // Our OpenGL view:
    private SampleApplicationGLView mGlView;

    // Our renderer:
    private PaintRenderer mRenderer;

    // The textures we will use for rendering:
    private Vector<Texture> mTextures;

    private boolean mSwitchDatasetAsap = false;
    private boolean mFlash = false;
    private boolean mContAutofocus = false;
    private boolean mExtendedTracking = false;

    private View mFlashOptionView;

    private RelativeLayout mUILayout;

    LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);

    // Alert Dialog used to display SDK errors
    private AlertDialog mErrorDialog;

    boolean mIsDroidDevice = false;

    // Eman
    private TouchCoordQueue mTouchQueue;
    public TouchCoord tempTouchCoord;

    // Drawingpath
    private SerializablePath drawingPath;
    private RGBColor currentColor;
    private double currentBrushSize;

    private static final RGBColor DEFAULT_COLOR = new RGBColor(20, 20, 20);

    // ColorPicker
    private ColorSeekBar colorSeekBar;

    // Called when the activity first starts or the user navigates back to an
    // activity.
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(LOGTAG, "onCreate");
        super.onCreate(savedInstanceState);

        vuforiaAppSession = new SampleApplicationSession(this);

        startLoadingAnimation();
        mDatasetStrings.add("StonesAndChips.xml");
        mDatasetStrings.add("ARMuseumDataBase.xml");

        vuforiaAppSession.initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Load any sample specific textures:
        mTextures = new Vector<Texture>();
        loadTextures();

        mIsDroidDevice = android.os.Build.MODEL.toLowerCase().startsWith("droid");

        // Eman
        //mTouchQueue = new TouchCoordQueue();
        tempTouchCoord = new TouchCoord(0, 0);

        // Database
        setupFirebase();
        login();

        drawingPath = new SerializablePath();
        currentColor = DEFAULT_COLOR;
        currentBrushSize = 20;

        setupFirebase();
        login();
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    // We want to load specific textures from the APK, which we will later use
    // for rendering.

    private void loadTextures()
    {
        mTextures.add(Texture.loadTextureFromApk("canvas_texture.png", getAssets()));
    }


    // Called when the activity will start interacting with the user.
    @Override
    protected void onResume()
    {
        Log.d(LOGTAG, "onResume");
        super.onResume();

//        // This is needed for some Droid devices to force portrait
//        if (mIsDroidDevice)
//        {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        }

        try
        {
            vuforiaAppSession.resumeAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }

        // Resume the GL view:
        if (mGlView != null)
        {
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }

        hideStatusBar();
    }


    // Callback for configuration changes the activity handles itself
    @Override
    public void onConfigurationChanged(Configuration config)
    {
        Log.d(LOGTAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);

        vuforiaAppSession.onConfigurationChanged();
    }


    // Called when the system is about to start resuming a previous activity.
    @Override
    protected void onPause() {
        Log.d(LOGTAG, "onPause");
        super.onPause();

        if (mGlView != null)
        {
            mGlView.setVisibility(View.INVISIBLE);
            mGlView.onPause();
        }

        // Turn off the flash
        if (mFlashOptionView != null && mFlash)
        {
            // OnCheckedChangeListener is called upon changing the checked state
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            {
                ((Switch) mFlashOptionView).setChecked(false);
            } else
            {
                ((CheckBox) mFlashOptionView).setChecked(false);
            }
        }

        try
        {
            vuforiaAppSession.pauseAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }
    }


    // The final call you receive before your activity is destroyed.
    @Override
    protected void onDestroy()
    {
        Log.d(LOGTAG, "onDestroy");
        super.onDestroy();

        try
        {
            vuforiaAppSession.stopAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }

        // Unload texture:
        mTextures.clear();
        mTextures = null;

        System.gc();
    }


    // Initializes AR application components.
    private void initApplicationAR()
    {
        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();

        mGlView = new SampleApplicationGLView(this);
        mGlView.init(translucent, depthSize, stencilSize);

        mRenderer = new PaintRenderer(this, vuforiaAppSession);
        mRenderer.setTextures(mTextures);
        mGlView.setRenderer(mRenderer);
    }


    private void startLoadingAnimation()
    {
        mUILayout = (RelativeLayout) View.inflate(this, R.layout.camera_overlay,
                null);

        mUILayout.setVisibility(View.VISIBLE);
        mUILayout.setBackgroundColor(Color.BLACK);

        // Gets a reference to the loading dialog
        loadingDialogHandler.mLoadingDialogContainer = mUILayout
                .findViewById(R.id.loading_indicator);

        // Shows the loading indicator at start
        loadingDialogHandler
                .sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);

        // Adds the inflated layout to the view
        addContentView(mUILayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }


    // Methods to load and destroy tracking data.
    @Override
    public boolean doLoadTrackersData()
    {
        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) tManager
                .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
            return false;

        if (mCurrentDataset == null)
            mCurrentDataset = objectTracker.createDataSet();

        if (mCurrentDataset == null)
            return false;

        if (!mCurrentDataset.load(
                mDatasetStrings.get(mCurrentDatasetSelectionIndex),
                STORAGE_TYPE.STORAGE_APPRESOURCE))
            return false;

        if (!objectTracker.activateDataSet(mCurrentDataset))
            return false;

        int numTrackables = mCurrentDataset.getNumTrackables();
        for (int count = 0; count < numTrackables; count++)
        {
            Trackable trackable = mCurrentDataset.getTrackable(count);
            if(isExtendedTrackingActive())
            {
                trackable.startExtendedTracking();
            }

            String name = "Current Dataset : " + trackable.getName();
            trackable.setUserData(name);
        }
        return true;
    }


    @Override
    public boolean doUnloadTrackersData()
    {
        // Indicate if the trackers were unloaded correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) tManager
                .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
            return false;

        if (mCurrentDataset != null && mCurrentDataset.isActive())
        {
            if (objectTracker.getActiveDataSet().equals(mCurrentDataset)
                    && !objectTracker.deactivateDataSet(mCurrentDataset))
            {
                result = false;
            } else if (!objectTracker.destroyDataSet(mCurrentDataset))
            {
                result = false;
            }

            mCurrentDataset = null;
        }

        return result;
    }


    @Override
    public void onInitARDone(SampleApplicationException exception)
    {

        if (exception == null)
        {
            initApplicationAR();

            mRenderer.setActive(true);

            // Now add the GL surface view. It is important
            // that the OpenGL ES surface view gets added
            // BEFORE the camera is started and video
            // background is configured.
            addContentView(mGlView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            // Sets the UILayout to be drawn in front of the camera
            mUILayout.bringToFront();

            // Sets the layout background to transparent
            mUILayout.setBackgroundColor(Color.TRANSPARENT);

            try
            {
                vuforiaAppSession.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT);
            } catch (SampleApplicationException e)
            {
                Log.e(LOGTAG, e.getString());
            }

            boolean result = CameraDevice.getInstance().setFocusMode(
                    CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);

            if (result)
                mContAutofocus = true;
            else
                Log.e(LOGTAG, "Unable to enable continuous autofocus");

            addOverlayView(true);

        } else
        {
            Log.e(LOGTAG, exception.getString());
            showInitializationErrorMessage(exception.getString());
        }
    }


    // Shows initialization error messages as System dialogs
    public void showInitializationErrorMessage(String message)
    {
        final String errorMessage = message;
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                if (mErrorDialog != null)
                {
                    mErrorDialog.dismiss();
                }

                // Generates an Alert Dialog to show the error message
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        MainActivity.this);
                builder
                        .setMessage(errorMessage)
                        .setTitle(getString(R.string.INIT_ERROR))
                        .setCancelable(false)
                        .setIcon(0)
                        .setPositiveButton(getString(R.string.button_OK),
                                new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        finish();
                                    }
                                });

                mErrorDialog = builder.create();
                mErrorDialog.show();
            }
        });
    }


    @Override
    public void onVuforiaUpdate(State state)
    {
        if (mSwitchDatasetAsap)
        {
            mSwitchDatasetAsap = false;
            TrackerManager tm = TrackerManager.getInstance();
            ObjectTracker ot = (ObjectTracker) tm.getTracker(ObjectTracker
                    .getClassType());
            if (ot == null || mCurrentDataset == null
                    || ot.getActiveDataSet() == null)
            {
                Log.d(LOGTAG, "Failed to swap datasets");
                return;
            }

            doUnloadTrackersData();
            doLoadTrackersData();
        }
    }


    @Override
    public boolean doInitTrackers()
    {
        // Indicate if the trackers were initialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        Tracker tracker;

        // Trying to initialize the image tracker
        tracker = tManager.initTracker(ObjectTracker.getClassType());
        if (tracker == null)
        {
            Log.e(
                    LOGTAG,
                    "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else {
            Log.i(LOGTAG, "Tracker successfully initialized");
        }
        return result;
    }


    @Override
    public boolean doStartTrackers()
    {
        // Indicate if the trackers were started correctly
        boolean result = true;

        Tracker objectTracker = TrackerManager.getInstance().getTracker(
                ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.start();

        return result;
    }


    @Override
    public boolean doStopTrackers()
    {
        // Indicate if the trackers were stopped correctly
        boolean result = true;

        Tracker objectTracker = TrackerManager.getInstance().getTracker(
                ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.stop();

        return result;
    }

    @Override
    public boolean doDeinitTrackers()
    {
        // Indicate if the trackers were deinitialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(ObjectTracker.getClassType());

        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int index = event.getActionIndex();
        int action = event.getActionMasked();
        int pointerId = event.getPointerId(index);

        int xPos = (int)event.getX();
        int yPos = (int)event.getY();

        switch(action)
        {
            case MotionEvent.ACTION_DOWN:
                tempTouchCoord.set(xPos, yPos);
                mRenderer.addTouchToQueue(tempTouchCoord, currentColor, currentBrushSize);

                drawingPath.addPoint(new Point(xPos, yPos));

                break;
            case MotionEvent.ACTION_MOVE:
                tempTouchCoord.set(xPos, yPos);
                mRenderer.addTouchToQueue(tempTouchCoord);

                drawingPath.addPoint(new Point(xPos, yPos));

                break;
            case MotionEvent.ACTION_UP:
                Stroke stroke = new Stroke(drawingPath, currentColor, currentBrushSize);
                saveStroke(stroke);

                this.mTouchQueue.reset();
                mRenderer.clearTrail();
                drawingPath.reset();

                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }

        return true;
    }

    boolean isExtendedTrackingActive()
    {
        return mExtendedTracking;
    }

    public TouchCoordQueue getTouchCoordQueue()
    {
        return this.mTouchQueue;
    }

    private void saveStroke(Stroke stroke) {
        mFirebaseDatabaseReference.child(STROKE_PATH_CHILD).push().setValue(stroke);
    }

    public SerializablePath getDrawingPath()
    {
        return this.drawingPath;
    }

    private void setupFirebase() {
        // Authentication
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(LOGTAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(LOGTAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        // Database
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
    }

    private void startListeningToDrawingEventsFromDatabase() {
//        mFirebaseDatabaseReference.addListenerForSingleValueEvent(drawingDatabaseListener);
//        mFirebaseDatabaseReference.addValueEventListener(drawingDatabaseListener);

        Toast.makeText(MainActivity.this, "Starting to listen to db",
                Toast.LENGTH_SHORT).show();
        Log.v(LOGTAG, "Listening to db");

    }

    private void login() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(LOGTAG, "signInAnonymously:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(LOGTAG, "signInAnonymously", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });

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
                    RGBColor color = stroke.getColor();
                    double brushSize = stroke.getBrushSize();

                    for (Point point: stroke.getSerializablePath().getPoints()) {
                        tempTouchCoord.set(point.x, point.y);

                        mRenderer.addTouchToQueue(tempTouchCoord,color , brushSize);
//                        Log.v(LOGTAG, point.x + " " + point.y);
                    }
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError)
        {
            Log.w(LOGTAG, databaseError.toException());
        }
    };

    private void initColorPicker() {
        colorSeekBar = (ColorSeekBar) findViewById(R.id.colorSlider);

        colorSeekBar.setMaxValue(100);
        colorSeekBar.setColors(R.array.material_colors); // material_colors is defalut included in res/color,just use it.
        colorSeekBar.setColorBarValue(10); //0 - maxValue
        colorSeekBar.setAlphaBarValue(10); //0-255
        colorSeekBar.setShowAlphaBar(false);
        colorSeekBar.setBarHeight(10); //5dpi
        colorSeekBar.setThumbHeight(30); //30dpi
        colorSeekBar.setBarMargin(10); //set the margin between colorBar and alphaBar 10dpi

        colorSeekBar.setOnColorChangeListener(new ColorSeekBar.OnColorChangeListener() {
            @Override
            public void onColorChangeListener(int colorBarValue, int alphaBarValue, int color) {
                currentColor = intToRGB(color);
            }
        });

        currentColor = intToRGB(colorSeekBar.getColor());
    }

    private RGBColor intToRGB(int androidColorInt) {
        return new RGBColor(Color.red(androidColorInt), Color.green(androidColorInt), Color.blue(androidColorInt));
    }

    // Adds the Controls Overlay view to the GLView
    private void addOverlayView(boolean initLayout)
    {
        // Inflates the Overlay Layout to be displayed above the Camera View
        LayoutInflater inflater = LayoutInflater.from(this);
        mUILayout = (RelativeLayout) inflater.inflate(
                R.layout.controls_overlay, null, false);


        mUILayout.setVisibility(View.VISIBLE);

        // If this is the first time that the application runs then the
        // uiLayout background is set to BLACK color, will be set to
        // transparent once the SDK is initialized and camera ready to draw
//        if (initLayout)
//        {
//            mUILayout.setBackgroundColor(Color.BLACK);
//        }
        mUILayout.setBackgroundColor(Color.TRANSPARENT);

        // Adds the inflated layout to the view
        addContentView(mUILayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        initColorPicker();
        mUILayout.bringToFront();
    }



    protected void onDrawingSurfaceLoaded()
    {
        if (dropDatabaseOnStart) {
            dropDatabase();
        }
        startListeningToDrawingEventsFromDatabase();
    }

    private void dropDatabase() {
        mFirebaseDatabaseReference.child(STROKE_PATH_CHILD).removeValue();
    }

    private void hideStatusBar()  {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }
 }
