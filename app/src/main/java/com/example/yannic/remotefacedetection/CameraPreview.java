package com.example.yannic.remotefacedetection;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
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
        private boolean faceDetectionRunning;
        private SurfaceHolder mHolder;
        private Camera mCamera;
        private JadexService.MyServiceInterface myService;
        private List<Integer> _faces;
        private Context _context;

        private boolean _faceFound;
        private boolean detectionLocal;



        public CameraPreview(Context context, Camera camera) {
            super(context);
            mCamera = camera;
            Log.d("Preview", "Preview created");
            _faces = new ArrayList<Integer>();
            _context = context;

            _faceFound = false;
            faceDetectionRunning = false;
            detectionLocal = false;

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


        Log.d("FaceDetection", ""+ _faceFound + " " + frameCounter + "Threshold; " + myService.getThreshold());


        //für lokale gesichtserkennung
        if (myService != null && myService.agentRunning() && frameCounter >= myService.getThreshold() && detectionLocal && _faceFound) {

            Log.d("FaceDetection", "send id: " + frameID);
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();
            YuvImage image = new YuvImage(bytes, parameters.getPreviewFormat(),
                    size.width, size.height, null);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compressToJpeg(
                    new Rect(Math.round(((float) image.getWidth())/2000*(1000+_faces.get(0))),
                            Math.round(((float)image.getHeight())/2000*(1000+_faces.get(1))),
                            Math.round(((float)image.getWidth())/2000*(1000+_faces.get(2))),
                            Math.round(((float)image.getHeight())/2000*(1000+_faces.get(3)))),
                    50,
                    baos);


            //Log.d("Rect", " " + Math.round(((float) image.getWidth())/2000*(1000+_faces.get(0))) + " " + ((int)((float)image.getHeight())/2000*(1000+_faces.get(1)))+ " " +((float)image.getWidth())/2000*(1000+_faces.get(2))+ " " + ((float)image.getHeight())/2000*(1000+_faces.get(3)));
            byte[] b = baos.toByteArray();

            Log.d("FaceDetection", "bytes: " + b.length);



            myService.recognizeFace(b);
            frameID++;

            frameCounter = 0;
            _faceFound = false;
        }

        //für remote Gesichtserkennung
        if (myService != null && myService.agentRunning() && frameCounter >= myService.getThreshold() && !detectionLocal) {

            Log.d("RemoteAgent", "id: " + frameID);
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();
            YuvImage image = new YuvImage(bytes, parameters.getPreviewFormat(),
                    size.width, size.height, null);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compressToJpeg(
                    new Rect(0, 0, image.getWidth(), image.getHeight()), 5,
                    baos);


            byte[] b = baos.toByteArray();


            Log.d("PreviewFrameSend", "bytes: " + b.length + "width: " + image.getWidth() + "heigth: " + image.getHeight());
            myService.detectFaces(frameID, b);
            frameID++;

            frameCounter = 0;
        }
        frameCounter++;

    }


        public int startFaceDetection() {
            if (faceDetectionRunning) {
                return 0;
            }
            // check if face detection is supported or not
            // using Camera.Parameters


            MyFaceDetectionListener fDListener = new MyFaceDetectionListener();
            mCamera.setFaceDetectionListener(fDListener);
            mCamera.startFaceDetection();
            faceDetectionRunning = true;
            return 1;
        }

        public int stopFaceDetection() {
            if (faceDetectionRunning) {
                mCamera.stopFaceDetection();
                faceDetectionRunning = false;
                return 1;
            }
            return 0;
        }





        public void setDetectionLocationLocal(){
            detectionLocal = true;
            startFaceDetection();
        }


    public void setFacesList(List<Integer> faces){
        _faces = faces;
    }

    private class MyFaceDetectionListener implements Camera.FaceDetectionListener {
        List<Integer> faceRects;

        private MyFaceDetectionListener(){
            super();
            faceRects = new ArrayList<Integer>();
            faceRects.add(0);
        }


    @Override
    public void onFaceDetection(Camera.Face[] faces, Camera camera) {

        Log.d("FaceDetection", "Gesichter gefunden: " + faces.length);




        for (int i=0; i<faces.length; i++) {
            //nt left = faces[i].rect.left;
            //int right = faces[i].rect.right;
            //int top = faces[i].rect.top;
            //int bottom = faces[i].rect.bottom;
            faceRects.add(faces[i].rect.left);

            faceRects.add(faces[i].rect.top);
            faceRects.add(faces[i].rect.right);
            faceRects.add(faces[i].rect.bottom);

            Intent toIntent = new Intent("faceDetected");
            toIntent.putIntegerArrayListExtra("Data", (ArrayList<Integer>) faceRects);
            LocalBroadcastManager.getInstance(_context).sendBroadcast(toIntent);

            Log.d("FaceDetection", "coord: left" + faceRects.get(i) + " top" + faceRects.get(i+1) + " right" + faceRects.get(i+2) + " bottom" +faceRects.get(i+3) );


        }

        if(faces.length>0){
            _faceFound = true;


            _faces = faceRects;
        }



    }
}
}






