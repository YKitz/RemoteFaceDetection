package com.example.yannic.remotefacedetection;

import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;


import com.example.yannic.remotefacedetection.agent.IAgentInterface;
import com.example.yannic.remotefacedetection.agent.RemoteFaceDetectionAgent;

import java.util.ArrayList;
import java.util.List;

import jadex.android.EventReceiver;
import jadex.android.service.JadexPlatformService;
import jadex.base.PlatformConfiguration;
import jadex.base.RootComponentConfiguration;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IExternalAccess;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;


/**
 * Created by Yannic on 23.02.2017.
 */

public class JadexService extends JadexPlatformService {

    public interface MyPlatformListener
    {
        void onPlatformStarting();
        void onPlatformStarted();

    }

    public class MyServiceInterface extends Binder
    {

        public boolean agentRunning(){ return JadexService.this.running ;}

        public void startFaceDetection()    { JadexService.this.startFaceDetection(); }

        public void detectFaces(int height, int width, byte[] data)    {JadexService.this.detectFaces( height,width, data);    }
        }



    public MyPlatformListener listener;

    IAgentInterface agent;

    private Handler handler;

    private boolean running;
    public JadexService()
    {
        super();
        setPlatformAutostart(false);
        PlatformConfiguration config = getPlatformConfiguration();
        RootComponentConfiguration rootConfig = config.getRootConfig();
        rootConfig.setKernels(RootComponentConfiguration.KERNEL.micro);
        rootConfig.setAwareness(true);
        rootConfig.setNetworkName("OpenCVTestNetwork");
        rootConfig.setNetworkPass("testpw");




    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        running = false;
        //Händler für die kommunikation mit dem UI Thread
        this.handler = new Handler();

        Log.d("Service","Service running");

        registerEventReceiver(new EventReceiver<MyEvent>(MyEvent.class)
        {

            public void receiveEvent(final MyEvent event)
            {

                running=true;
                handler.post(new Runnable()
                {

                    public void run()
                    {
                        Toast.makeText(JadexService.this, event.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }


    @Override
    public IBinder onBind(Intent intent)
    {
        return new MyServiceInterface();
    }

    @Override
    protected void onPlatformStarting()
    {
        super.onPlatformStarting();
        if (listener != null)
        {
            listener.onPlatformStarting();
        }
    }

    @Override
    protected void onPlatformStarted(IExternalAccess platform)
    {
        super.onPlatformStarted(platform);
        if (listener != null)
        {
            listener.onPlatformStarted();
        }


       // startSendAgent();
        //startComponent("LocalFaceDetectionAgent", FaceDetectionAgent.class);
        startComponent("RemoteFaceDetectionAgent", RemoteFaceDetectionAgent.class).addResultListener(new DefaultResultListener<IComponentIdentifier>() {
            @Override
            public void resultAvailable(IComponentIdentifier result) {
                agent = getsService(IAgentInterface.class);

            }
        });

    }

    public void startFaceDetection(){
        startPlatform();
    }


    public void detectFaces(int height, int width, byte[] data){



         IFuture<List<Integer>> fut = agent.getFaceArray(height, width, data);

        fut.addResultListener(new DefaultResultListener<List<Integer>>() {
            public void resultAvailable(List<Integer> result) {


                    Intent toIntent = new Intent("faceDetected");
                    toIntent.putIntegerArrayListExtra("Data", (ArrayList<Integer>) result);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(toIntent);



                Log.d("JadexService", "broadcasting Rect: " + result.toString());
            }
        });
       }


    }








