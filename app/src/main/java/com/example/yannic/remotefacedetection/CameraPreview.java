package com.example.yannic.remotefacedetection;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.R.attr.data;

/**
 * Created by Yannic on 13.03.2017.
 */



    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback{
        private SurfaceHolder mHolder;
        private Camera mCamera;
        private JadexService.MyServiceInterface myService;
        private List _faces;


        public CameraPreview(Context context, Camera camera) {
            super(context);
            mCamera = camera;
            Log.d("Preview", "Preview created");
            _faces = new ArrayList<Integer>();

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void setService(JadexService.MyServiceInterface service){
        myService = service;
    }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, now tell the camera where to draw the preview.
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                Log.d("CameraPreview", "Error setting camera preview: " + e.getMessage());
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // empty. Take care of releasing the Camera preview in your activity.
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null){
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e){
                // ignore: tried to stop a non-existent preview
            }

            // set preview size and make any resize, rotate or
            // reformatting changes here

            // start preview with new settings
            try {
                mCamera.setPreviewCallback(this);
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

            } catch (Exception e){
                Log.d("CameraPreview", "Error starting camera preview: " + e.getMessage());
            }
        }




        int frameCounter= 0;
        int frameID = 0;

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {






        if(myService != null && myService.agentRunning() && frameCounter >= 35) {

            Log.d("RemoteAgent", "id: " + frameID);
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size size = parameters.getPreviewSize();
                YuvImage image = new YuvImage(bytes, parameters.getPreviewFormat(),
                        size.width, size.height, null);
                /*
                //saving for testing
                File file = new File(Environment.getExternalStorageDirectory(), "out.jpg");
                FileOutputStream filecon = new FileOutputStream(file);
                */
                ByteArrayOutputStream baos=new ByteArrayOutputStream();
                image.compressToJpeg(
                        new Rect(0, 0, image.getWidth(), image.getHeight()), 20,
                        baos);
                byte[] b = baos.toByteArray();
                Log.d("PreviewFrameSend", "bytes: " + b.length +"width: " + image.getWidth() + "heigth: "+ image.getHeight());
                myService.detectFaces(frameID, b);
                frameID++;

            frameCounter = 0;
        }
  frameCounter++;




    }


    public void setFacesList(List<Integer> faces){
        _faces = faces;
    }

}

