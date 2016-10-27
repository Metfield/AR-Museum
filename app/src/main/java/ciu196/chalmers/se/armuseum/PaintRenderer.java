/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package ciu196.chalmers.se.armuseum;

import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.util.Log;
import android.widget.Toast;

import com.vuforia.Device;
import com.vuforia.Matrix44F;
import com.vuforia.Renderer;
import com.vuforia.RenderingPrimitives;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.Trackable;
import com.vuforia.TrackableResult;
import com.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.vuforia.Vec2F;
import com.vuforia.Vec3F;
import com.vuforia.Vec4F;
import com.vuforia.Vuforia;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import ciu196.chalmers.se.armuseum.SampleApplication.SampleApplicationSession;
import ciu196.chalmers.se.armuseum.SampleApplication.utils.CanvasMesh;
import ciu196.chalmers.se.armuseum.SampleApplication.utils.CubeObject;
import ciu196.chalmers.se.armuseum.SampleApplication.utils.CubeShaders;
import ciu196.chalmers.se.armuseum.SampleApplication.utils.LineShaders;
import ciu196.chalmers.se.armuseum.SampleApplication.utils.LoadingDialogHandler;
import ciu196.chalmers.se.armuseum.SampleApplication.utils.RayMesh;
import ciu196.chalmers.se.armuseum.SampleApplication.utils.SampleApplication3DModel;
import ciu196.chalmers.se.armuseum.SampleApplication.utils.SampleMath;
import ciu196.chalmers.se.armuseum.SampleApplication.utils.SampleUtils;
import ciu196.chalmers.se.armuseum.SampleApplication.utils.Texture;
import ciu196.chalmers.se.armuseum.SampleApplication.utils.TouchCoord;
import ciu196.chalmers.se.armuseum.SampleApplication.utils.TouchCoordQueue;
import ciu196.chalmers.se.armuseum.SampleApplication.utils.Vec4;


// The renderer class for the ImageTargets sample. 
public class PaintRenderer implements GLSurfaceView.Renderer, SampleAppRendererControl
{
    private static final String LOGTAG = "PaintRenderer";

    private final int CANVAS_TEXTURE = 0;

    private SampleApplicationSession vuforiaAppSession;
    private MainActivity mActivity;
    private SampleAppRenderer mSampleAppRenderer;

    private Vector<Texture> mTextures;

    private int shaderProgramID;
    private int vertexHandle;
    private int textureCoordHandle;
    private int mvpMatrixHandle;
    private int texSampler2DHandle;

    // Line Shader
    private int colorHandle;
    private int lineShaderProgramID;

    private CanvasMesh mCanvas;

    private float kBuildingScale = 12.0f;
    private SampleApplication3DModel mBuildingsModel;

    boolean mIsActive = false;
    boolean mModelsLoaded = false;

    // @Eman
    private Texture mCanvasTexture;
//    private RGBColor mCurrentBrushColor;

    // FUCK YOU ANDROID, YOU GIVE ME NO OTHER CHOICE!
    private int mLastEntryX, mLastEntryY;

    private float[] mProjectionInverseMatrix;
    private float[] mViewInverseMatrix;
    private float[] mModelViewMatrix;
    private float[] mRayTransformMatrix;

    // Ray member
    public RayMesh mDebugRay;

    public int VIEWPORT_WIDTH, VIEWPORT_HEIGHT;

    private TouchCoordQueue mTouchQueue;

    private static final float OBJECT_SCALE_FLOAT = 200.0f;

    // To know whether there is something we can draw on
    boolean mIsTextureActive = false;

    public PaintRenderer(MainActivity activity, SampleApplicationSession session)
    {
        mActivity = activity;
        vuforiaAppSession = session;
        // SampleAppRenderer used to encapsulate the use of RenderingPrimitives setting
        // the device mode AR/VR and stereo mode
        mSampleAppRenderer = new SampleAppRenderer(this, Device.MODE.MODE_AR, false);

        mTouchQueue = TouchCoordQueue.getInstance();
    }
    
    
    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl)
    {
        if (!mIsActive)
            return;
        
        // Call our function to render content from SampleAppRenderer class
        mSampleAppRenderer.render();
    }
    

    public void setActive(boolean active)
    {
        mIsActive = active;
    }


    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");
        
        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();

        mSampleAppRenderer.onSurfaceCreated();
    }
    
    
    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");
        
        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);

        // RenderingPrimitives to be updated when some rendering change is done
        mSampleAppRenderer.onConfigurationChanged();

        initRendering();
    }
    
    // Function for initializing the renderer.
    private void initRendering()
    {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f : 1.0f);
        
        for (Texture t : mTextures)
        {
            GLES20.glGenTextures(1, t.mTextureID, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, t.mWidth, t.mHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, t.mData);
        }
        
        shaderProgramID = SampleUtils.createProgramFromShaderSrc(
                CubeShaders.CUBE_MESH_VERTEX_SHADER,
                CubeShaders.CUBE_MESH_FRAGMENT_SHADER);

        lineShaderProgramID = SampleUtils.createProgramFromShaderSrc(
                LineShaders.LINE_VERTEX_SHADER,
                LineShaders.LINE_FRAGMENT_SHADER);

        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID, "vertexPosition");
        textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID, "vertexTexCoord");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID, "modelViewProjectionMatrix");
        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID, "texSampler2D");

        colorHandle = GLES20.glGetUniformLocation(lineShaderProgramID, "color");

        if(!mModelsLoaded)
        {
            mCanvas = new CanvasMesh();
            mDebugRay = new RayMesh();

            try
            {
                mBuildingsModel = new SampleApplication3DModel();
                mBuildingsModel.loadModel(mActivity.getResources().getAssets(), "ImageTargets/Buildings.txt");
                mModelsLoaded = true;
            } catch (IOException e)
            {
                Log.e(LOGTAG, "Unable to load buildings");
            }

            // Hide the Loading Dialog
            mActivity.loadingDialogHandler.sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
        }

        Point tempPoint = new Point();
        mActivity.getWindowManager().getDefaultDisplay().getSize(tempPoint);

        // Eman
        VIEWPORT_WIDTH = tempPoint.x;
        VIEWPORT_HEIGHT = tempPoint.y;

        TouchCoordQueue.VIEWPORT_WIDTH = VIEWPORT_WIDTH;
        TouchCoordQueue.VIEWPORT_HEIGHT = VIEWPORT_HEIGHT;

        mProjectionInverseMatrix = new float[16];
        mViewInverseMatrix = new float[16];
        mModelViewMatrix = new float[16];
        mRayTransformMatrix = new float[16];

        mLastEntryX = mLastEntryY = -1;
    }
    
    public void updateConfiguration()
    {
        mSampleAppRenderer.onConfigurationChanged();
    }

    // The render function called from SampleAppRendering by using RenderingPrimitives views.
    // The state is owned by SampleAppRenderer which is controlling it's lifecycle.
    // State should not be cached outside this method.
    public void renderFrame(State state, float[] projectionMatrix)
    {
        // Renders video background replacing Renderer.DrawVideoBackground()
        mSampleAppRenderer.renderVideoBackground();

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // handle face culling, we need to detect if we are using reflection
        // to determine the direction of the culling
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
        if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON)
            GLES20.glFrontFace(GLES20.GL_CW); // Front camera
        else
            GLES20.glFrontFace(GLES20.GL_CCW); // Back camera

        mIsTextureActive = state.getNumTrackableResults() > 0;

        // Did we find any trackables this frame?
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++)
        {

            TrackableResult result = state.getTrackableResult(tIdx);
            Trackable trackable = result.getTrackable();
            //printUserData(trackable);
            Matrix44F modelViewMatrix_Vuforia = Tool.convertPose2GLMatrix(result.getPose());
            float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();

            mModelViewMatrix = modelViewMatrix;

            // deal with the modelview and projection matrices
            float[] modelViewProjection = new float[16];

            if (!mActivity.isExtendedTrackingActive())
            {
                Matrix.translateM(modelViewMatrix, 0, 0.0f, 0.0f, OBJECT_SCALE_FLOAT);
                Matrix.scaleM(modelViewMatrix, 0, OBJECT_SCALE_FLOAT, OBJECT_SCALE_FLOAT, OBJECT_SCALE_FLOAT);

                // Set same transformations for the ray
                /*Matrix.setIdentityM(mRayTransformMatrix, 0);
                Matrix.rotateM(mRayTransformMatrix, 0, 90.0f, 1.0f, 0, 0);
                Matrix.scaleM(mRayTransformMatrix, 0, kBuildingScale, kBuildingScale, kBuildingScale);*/
            }
            else
            {
                Matrix.rotateM(modelViewMatrix, 0, 90.0f, 1.0f, 0, 0);
                Matrix.scaleM(modelViewMatrix, 0, kBuildingScale, kBuildingScale, kBuildingScale);
            }

            Matrix.multiplyMM(modelViewProjection, 0, projectionMatrix, 0, modelViewMatrix, 0);

            // Eman: Get projection and view inverse
            Matrix.invertM(this.mProjectionInverseMatrix, 0, projectionMatrix, 0);
            Matrix.invertM(this.mViewInverseMatrix, 0, modelViewMatrix, 0);
//            mModelViewMatrix = modelViewMatrix;

            // activate the shader program and bind the vertex/normal/tex coords
            GLES20.glUseProgram(shaderProgramID);

            if (!mActivity.isExtendedTrackingActive())
            {
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, mCanvas.getVertices());
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mCanvas.getTexCoords());

                GLES20.glEnableVertexAttribArray(vertexHandle);
                GLES20.glEnableVertexAttribArray(textureCoordHandle);

                // activate texture 0, bind it, and pass to shader
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.getCanvasTexture().mTextureID[0]);
                GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, mCanvasTexture.mWidth, mCanvasTexture.mHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mCanvasTexture.mData);
                GLES20.glUniform1i(texSampler2DHandle, 0);

                // pass the model view matrix to the shader
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjection, 0);

                // finally draw the teapot
                GLES20.glDrawElements(GLES20.GL_TRIANGLES, mCanvas.getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT, mCanvas.getIndices());

                // disable the enabled arrays
                GLES20.glDisableVertexAttribArray(vertexHandle);
                GLES20.glDisableVertexAttribArray(textureCoordHandle);

                // Also set TouchCoordQueue texture size
                TouchCoordQueue.TEXTURE_SIZE = mCanvasTexture.mWidth - 1;

                // Eman
                // Now draw the debug RAY!!!
                GLES20.glUseProgram(lineShaderProgramID);

                GLES20.glLineWidth(50);

                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, mDebugRay.getVertices());
                GLES20.glEnableVertexAttribArray(vertexHandle);

                // pass the model view matrix to the shader
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjection, 0);

                // Set Ray color
                GLES20.glUniform3f(colorHandle, 10.0f, 255.0f, 40.0f);

                // finally draw the ray
                GLES20.glDrawElements(GLES20.GL_LINES, mDebugRay.getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT, mDebugRay.getIndices());

                // disable the enabled arrays
                GLES20.glDisableVertexAttribArray(vertexHandle);
            }
            else
            {
                Toast.makeText(PaintRenderer.this.mActivity, "Turn off Extended tracking!!", Toast.LENGTH_SHORT).show();
            }

            SampleUtils.checkGLError("Render Frame");
        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
    }

    private void printUserData(Trackable trackable)
    {
        String userData = (String) trackable.getUserData();
        Log.d(LOGTAG, "UserData:Retreived User Data	\"" + userData + "\"");
    }
    
    
    public void setTextures(Vector<Texture> textures)
    {
        mTextures = textures;
        mCanvasTexture = textures.get(CANVAS_TEXTURE);

//        mCurrentBrushColor = new RGBColor((byte)20, (byte)20, (byte)20);
//        mCanvasTexture.setBrushColor(mCurrentBrushColor);

        mActivity.onDrawingSurfaceLoaded();
    }

    public Texture getCanvasTexture()
    {
//        Log.v(LOGTAG, "Coord queue:  " + mTouchQueue.getSize());
        // Do the whole texture getting here
        if(mTouchQueue.getSize() > 0 && mIsTextureActive)
        {
            Log.v(LOGTAG, "Popping coords ");
            mCanvasTexture.updatePixels();

        }

       /* TouchCoord tc;
        Vec2F point;// = new Vec2F();
        Vec3F center = new Vec3F(0.0f, 0.0f, 0.0f);
        Vec3F normal = new Vec3F(0.0f, 0.0f, 1.0f);
        Matrix44F projectionInverse, modelView;

        projectionInverse = new Matrix44F();
        modelView = new Matrix44F();

        projectionInverse.setData(mProjectionInverseMatrix);
        projectionInverse.setData(mModelViewMatrix);

        while(mTouchQueue.getSize() > 0)
        {
            tc = mTouchQueue.pop();
            point = new Vec2F(tc.getX(), tc.getY());

            SampleMath.projectScreenPointToPlane(projectionInverse, modelView, VIEWPORT_WIDTH, VIEWPORT_HEIGHT, point, center, normal);
        }*/

        return mCanvasTexture;
    }

    public void addTouchToQueue(TouchCoord tc, RGBColor color, double brushSize)
    {
        mTouchQueue.setColor(color);
        mTouchQueue.setBrushSize(brushSize);

        mTouchQueue.push(tc);
        // DON'T ADD A LINE
//        addTouchToQueue(tc);
    }

    public void addTouchToQueue(TouchCoord tc)
    {
        transformCoordinates(tc.getX(), tc.getY());

        // Eman: Stupid fucking hack FUCK YOU JAVA
        // As long as there is a previous entry do this
        if(!isLastEntryNull())
        {
            // Set variables for line method
            int x1 = mLastEntryX;
            int y1 = mLastEntryY;
            int x2 = tc.getX();
            int y2 = tc.getY();

            createLineAndAddToQueue(x1, y1, x2, y2);

            mLastEntryX = tc.getX();
            mLastEntryY = tc.getY();
        }
        else
        {
            // If there is no entry add the first one
            mTouchQueue.push(tc);
            mLastEntryX = tc.getX();
            mLastEntryY = tc.getY();
        }
    }

    public void clearTrail()
    {
        mLastEntryX = -1;
        mLastEntryY = -1;
    }

    private boolean isLastEntryNull()
    {
        if(mLastEntryY != -1 || mLastEntryX != -1)
            return false;
        else
            return true;
    }

    private void createLineAndAddToQueue(int _x1, int _y1, int _x2, int _y2)
    {
        int x = _x1;
        int y = _y1;
        int x2 = _x2;
        int y2 = _y2;

        // Get difference
        int w = x2 - x ;
        int h = y2 - y ;

        int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0 ;

        // Configure algorithm according to octant
        if (w<0) dx1 = -1 ; else if (w>0) dx1 = 1 ;
        if (h<0) dy1 = -1 ; else if (h>0) dy1 = 1 ;
        if (w<0) dx2 = -1 ; else if (w>0) dx2 = 1 ;

        int longest = Math.abs(w) ;
        int shortest = Math.abs(h) ;

        if (!(longest>shortest))
        {
            longest = Math.abs(h) ;
            shortest = Math.abs(w) ;
            if (h<0) dy2 = -1 ; else if (h>0) dy2 = 1 ;
            dx2 = 0 ;
        }

        int numerator = longest >> 1 ;

        for (int i=0;i<=longest;i++)
        {
            mTouchQueue.push(new TouchCoord(x, y));
//            mActivity.getDrawingPath().addPoint(new Point(x, y));

            numerator += shortest ;
            if (!(numerator<longest))
            {
                numerator -= longest ;
                x += dx1 ;
                y += dy1 ;
            } else {
                x += dx2 ;
                y += dy2 ;
            }
        }
    }

    // STILL HIGHLY UNSTABLE METHOD!!!!
    private float[] transformCoordinates(float x, float y)
    {
        Log.i("touch", "TOUCH: " + x + " " + y);

        Matrix44F projInverse = new Matrix44F();
        projInverse.setData(mProjectionInverseMatrix);

        Matrix44F modelView = new Matrix44F();
        modelView.setData(mModelViewMatrix);


        Log.i("inter", "Test right plane");
        SampleMath.projectScreenPointToPlane(projInverse, modelView, VIEWPORT_WIDTH, VIEWPORT_HEIGHT, new Vec2F(x, y), new Vec3F(1, 0, 0), new Vec3F(-1, 0, 0));
/*
        Log.i("inter", "Test left plane");
        SampleMath.projectScreenPointToPlane(projInverse, modelView, VIEWPORT_WIDTH, VIEWPORT_HEIGHT, new Vec2F(x, y), new Vec3F(-1, 0, 0), new Vec3F(1, 0, 0));

        Log.i("inter", "Test upper plane");
        SampleMath.projectScreenPointToPlane(projInverse, modelView, VIEWPORT_WIDTH, VIEWPORT_HEIGHT, new Vec2F(x, y), new Vec3F(0, 1, 0), new Vec3F(0, -1, 0));

        Log.i("inter", "Test lower plane");
        SampleMath.projectScreenPointToPlane(projInverse, modelView, VIEWPORT_WIDTH, VIEWPORT_HEIGHT, new Vec2F(x, y), new Vec3F(0, -1, 0), new Vec3F(0, 1, 0));

        //Log.i("inter", "Test right plane");


*/



/*


        // Transform touch coordinates to viewport space [-1, 1]
        Vec4 viewport_coords = new Vec4( (2.0f * x) / TouchCoordQueue.VIEWPORT_WIDTH - 1.0f,
                1.0f - (2.0f * y) / TouchCoordQueue.VIEWPORT_HEIGHT,
                -1.0f,
                1.0f );

        // Make sure values are clamped
        viewport_coords.x = Math.max(-1.0f, Math.min(1.0f, (float)viewport_coords.x));
        viewport_coords.y = Math.max(-1.0f, Math.min(1.0f, (float)viewport_coords.y));

        //Log.e("blah!", "Viewport: " + viewport_coords.x + " " + viewport_coords.y  + " " + viewport_coords.z);

        float[] view_coords = new float[4];
        float[] model_coords = new float[4];
        float[] ray_origin = new float[4];

        // Go to eye (camera) coordinates
        Matrix.multiplyMV(view_coords, 0, mProjectionInverseMatrix, 0, viewport_coords.getFloatArray(), 0);

        // We're going in -z direction. W is 0 because this is not a point
        view_coords[2] = -1.0f;
        view_coords[3] = 0.0f;

        // Need to go to world coordinates
        Matrix.multiplyMV(model_coords, 0, mViewInverseMatrix, 0, view_coords, 0);

        ray_origin[0] = model_coords[0];
        ray_origin[1] = model_coords[1];
        ray_origin[2] = model_coords[2];

        // Normalize
        float length = (float)Math.sqrt(ray_origin[0]*ray_origin[0] + ray_origin[1]*ray_origin[1] + ray_origin[2]*ray_origin[2]);
        ray_origin[0] /= length;
        ray_origin[1] /= length;
        ray_origin[2] = (ray_origin[2] / length) /*+ mCanvas.getFrontFaceDepth()*/;



       /* mDebugRay.setOrigin(transformed[0], transformed[1], transformed[2] + 10);
        mDebugRay.setDestination(transformed[0], transformed[1], transformed[2] -10);*/

        /*Log.e("cock", "Transformed: " + transformed[0]
                + " " + transformed[1]
                + " " + transformed[2]);*/

        // Get vuforia's eye adjustment matrix
        /*float eyeAdjustmentGL[] = Tool.convert2GLMatrix(mSampleAppRenderer.getRenderingPrimitives().getEyeDisplayAdjustmentMatrix(0)).getData();
        float adjustedCoords[] = new float[4];

        Matrix.multiplyMV (adjustedCoords, 0, mModelViewMatrix, 0, ray_origin, 0);

        length = (float)Math.sqrt(adjustedCoords[0]*adjustedCoords[0] + adjustedCoords[1]*adjustedCoords[1] + adjustedCoords[2]*adjustedCoords[2]);
        adjustedCoords[0] /= length;
        adjustedCoords[1] /= length;
        adjustedCoords[2] /= length;

        Log.e("cock", "Adjusted: " + adjustedCoords[0]
                + " " + adjustedCoords[1]
                + " " + adjustedCoords[2]);*/

        /*mDebugRay.setOrigin(adjustedCoords[0], adjustedCoords[1], adjustedCoords[2] + 10);
        mDebugRay.setDestination(adjustedCoords[0], adjustedCoords[1], -10);*/

      /*  float[] ray_dir = new float[] {0.0f, 0.0f, -1.0f};
        float[] n = new float[] {0.0f, 0.0f, 1.0f};

        // Get t distance
        float upper_t = ray_origin[0] * n[0] + ray_origin[1] * n[1] + ray_origin[2] * n[2];
        float lower_t = ray_dir[0] * n[0] + ray_dir[1] * n[1] + ray_dir[2] * n[2];

        if(upper_t == 0 || lower_t == 0)
        {
            Log.e("cock", "MIIISS MATHAFACKA!!!!!");
            return new float[]{0,0};
        }

        float t = - (upper_t / lower_t);

        if(t < 0)
        {
            Log.e("cock", "MIIISS HIJUEPUTA!!!!!!!!!");
            return new float[]{0,0};
        }
        // Substitute t values in ray equation
        float[] canvas_coords = new float[3];

        canvas_coords[0] = ray_origin[0] + ray_dir[0] * t;
        canvas_coords[1] = ray_origin[1] + ray_dir[1] * t;
        canvas_coords[2] = ray_origin[2] + ray_dir[2] * t;

*/
        /*Log.e("cock", "CanvasCoords: " + canvas_coords[0]
                + " " + canvas_coords[1]
                + " " + canvas_coords[2]);*/

        return null;
    }

//    public void setBrushColor(byte r, byte g, byte b)
//    {
//        this.mCurrentBrushColor = new RGBColor(r, g, b);
//        this.mCanvasTexture.setBrushColor(this.mCurrentBrushColor);
//    }

}


