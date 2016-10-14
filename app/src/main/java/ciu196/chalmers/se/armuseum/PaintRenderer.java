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
import android.opengl.Matrix;
import android.util.Log;
import android.widget.Toast;

import com.vuforia.Device;
import com.vuforia.Matrix44F;
import com.vuforia.Renderer;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.Trackable;
import com.vuforia.TrackableResult;
import com.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.vuforia.Vec2F;
import com.vuforia.Vec3F;
import com.vuforia.Vuforia;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import ciu196.chalmers.se.armuseum.SampleApplication.SampleApplicationSession;
import ciu196.chalmers.se.armuseum.SampleApplication.utils.CanvasMesh;
import ciu196.chalmers.se.armuseum.SampleApplication.utils.CubeObject;
import ciu196.chalmers.se.armuseum.SampleApplication.utils.CubeShaders;
import ciu196.chalmers.se.armuseum.SampleApplication.utils.LoadingDialogHandler;
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
    private static final String LOGTAG = "ImageTargetRenderer";

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

    private CanvasMesh mCanvas;

    private float kBuildingScale = 12.0f;
    private SampleApplication3DModel mBuildingsModel;

    boolean mIsActive = false;
    boolean mModelsLoaded = false;

    // @Eman
    private Texture mCanvasTexture;
//    private RGBColor mCurrentBrushColor;

    private float[] mProjectionInverseMatrix;
    private float[] mViewInverseMatrix;
    private float[] mModelViewMatrix;

    public int VIEWPORT_WIDTH, VIEWPORT_HEIGHT;

    private TouchCoordQueue mTouchQueue;

    private static final float OBJECT_SCALE_FLOAT = 200.0f;

    public PaintRenderer(MainActivity activity, SampleApplicationSession session)
    {
        mActivity = activity;
        vuforiaAppSession = session;
        // SampleAppRenderer used to encapsulate the use of RenderingPrimitives setting
        // the device mode AR/VR and stereo mode
        mSampleAppRenderer = new SampleAppRenderer(this, Device.MODE.MODE_AR, false);
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

        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID, "vertexPosition");
        textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID, "vertexTexCoord");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID, "modelViewProjectionMatrix");
        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID, "texSampler2D");

        if(!mModelsLoaded)
        {
            mCanvas = new CanvasMesh();

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

        mTouchQueue.VIEWPORT_WIDTH = VIEWPORT_WIDTH;
        mTouchQueue.VIEWPORT_HEIGHT = VIEWPORT_HEIGHT;

        mProjectionInverseMatrix = new float[16];
        mViewInverseMatrix = new float[16];
        mModelViewMatrix = new float[16];
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

        // Did we find any trackables this frame?
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++)
        {
            TrackableResult result = state.getTrackableResult(tIdx);
            Trackable trackable = result.getTrackable();
            printUserData(trackable);
            Matrix44F modelViewMatrix_Vuforia = Tool.convertPose2GLMatrix(result.getPose());
            float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();

            // deal with the modelview and projection matrices
            float[] modelViewProjection = new float[16];

            if (!mActivity.isExtendedTrackingActive())
            {
                Matrix.translateM(modelViewMatrix, 0, 0.0f, 0.0f, OBJECT_SCALE_FLOAT);
                Matrix.scaleM(modelViewMatrix, 0, OBJECT_SCALE_FLOAT, OBJECT_SCALE_FLOAT, OBJECT_SCALE_FLOAT);
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
            this.mModelViewMatrix = modelViewMatrix;

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
                mTouchQueue.TEXTURE_SIZE = mCanvasTexture.mWidth - 1;
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
    }

    public Texture getCanvasTexture()
    {
        // Do the whole texture getting here
        if(mTouchQueue.getSize() > 0)
        {
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
        this.mTouchQueue.setColor(color);
        this.mTouchQueue.setBrushSize(brushSize);

        addTouchToQueue(tc);
    }

    public void addTouchToQueue(TouchCoord tc)
    {
        transformCoordinates(tc.getX(), tc.getY());
        this.mTouchQueue.push(tc);
    }

    private float[] transformCoordinates(float x, float y)
    {
        // Transform touch coordinates to viewport space [-1, 1]
        Vec4 viewport_coords = new Vec4( (2.0f * x) / TouchCoordQueue.VIEWPORT_WIDTH - 1.0f,
                1.0f - (2.0f * y) / TouchCoordQueue.VIEWPORT_HEIGHT,
                -1.0f,
                1.0f );

        // Make sure values are clamped
        viewport_coords.x = Math.max(-1.0f, Math.min(1.0f, (float)viewport_coords.x));
        viewport_coords.y = Math.max(-1.0f, Math.min(1.0f, (float)viewport_coords.y));

        float[] view_coords = new float[4];
        float[] model_coords = new float[4];

        Matrix.multiplyMV(view_coords, 0, mProjectionInverseMatrix, 0, viewport_coords.getFloatArray(), 0);

        view_coords[2] = -1.0f;
        view_coords[3] = 0.0f;

        Matrix.multiplyMV(model_coords, 0, mViewInverseMatrix, 0, view_coords, 0);

        // Normalize
        float length = (float)Math.sqrt(model_coords[0]*model_coords[0] + model_coords[1]*model_coords[1] + model_coords[2]*model_coords[2] + model_coords[3]*model_coords[3]);
        model_coords[0] /= length;
        model_coords[1] /= length;
        model_coords[2] /= length;
        model_coords[3] /= length;

        Log.e("cock", "Transformed: " + model_coords[0]
                + " " + model_coords[1]
                + " " + model_coords[2]
                + " " + model_coords[3]);



        return null;
    }

//    public void setBrushColor(byte r, byte g, byte b)
//    {
//        this.mCurrentBrushColor = new RGBColor(r, g, b);
//        this.mCanvasTexture.setBrushColor(this.mCurrentBrushColor);
//    }


}


