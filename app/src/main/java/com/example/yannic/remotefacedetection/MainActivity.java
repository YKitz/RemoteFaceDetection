package com.example.yannic.remotefacedetection;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    private Camera cam;
    private CameraPreview preview;
    Box _box;
    private Intent serviceIntent;
    public JadexService.MyServiceInterface myService;
    RelativeLayout.LayoutParams layoutParams;



    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cam = getCameraInstance();

        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
        //_box = new Box(getApplicationContext(), 2550, 1430, 200, 200);
        //addContentView(_box, layoutParams);




        preview = new CameraPreview(this, cam);




        FrameLayout layout = (FrameLayout) findViewById(R.id.camera_preview);
        layout.addView(preview);



        this.serviceIntent = new Intent(this, JadexService.class);

        startService(serviceIntent);

    }

    @Override
    public void onResume(){
        super.onResume();
        bindService(serviceIntent, this, 0);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        unbindService(this);
    }



    public void startFaceDetection(View v){
        myService.startFaceDetection();
    }


    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d("MAIN", "serviceConnected");
        this.myService = (JadexService.MyServiceInterface) service;
        preview.setService(myService);
        BroadcastReceiver jadexServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ArrayList<Integer> data = intent.getIntegerArrayListExtra("Data");


                _box = new Box(getApplicationContext(), data);
                addContentView(_box, layoutParams);
                //preview.setFacesList(data);
               //_box.clearCanvas();


                /*
                for(int counter = 0; counter < data.size(); counter+=4){

                    _box = new Box(getApplicationContext(), (int) (data.get(counter)*1.34), (int) (data.get(counter+1)*1.34),data.get(counter+2), data.get(counter+3));
                    addContentView(_box, layoutParams);

                }
                */


                Toast.makeText(getApplicationContext(),""+data.toString(), Toast.LENGTH_SHORT).show();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(jadexServiceReceiver,new IntentFilter("faceDetected"));
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        this.myService = null;
    }
}
