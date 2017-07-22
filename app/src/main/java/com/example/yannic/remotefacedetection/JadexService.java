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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jadex.android.EventReceiver;
import jadex.android.service.JadexPlatformService;
import jadex.base.PlatformConfiguration;
import jadex.base.RootComponentConfiguration;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IExternalAccess;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.DefaultTuple2ResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.ITuple2Future;


/**
 * Created by Yannic on 23.02.2017.
 */

public class JadexService extends JadexPlatformService {

    public interface MyPlatformListener {
        void onPlatformStarting();

        void onPlatformStarted();

    }

    public class MyServiceInterface extends Binder {

        public boolean agentRunning() {
            return JadexService.this.running;
        }

        public void startFaceDetection() {
            JadexService.this.startFaceDetection();
        }

        public void detectFaces(int id, byte[] data) {
            JadexService.this.detectFaces(id, data);
        }

        public int getThreshold(){ return JadexService.this.getThreshold(); }

        public void recognizeFace(int id, byte[] input){JadexService.this.recognizeFace(id, input);}

    }


    public MyPlatformListener listener;

    IAgentInterface agent;

    private Handler handler;

    private boolean running;

    private int sendThreshold;

   // private Map<Integer , Long> ids;

    private List<Integer> ids;

    public JadexService() {
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
    public void onCreate() {
        super.onCreate();
        running = false;
        //Händler für die kommunikation mit dem UI Thread
        this.handler = new Handler();

        sendThreshold = 30;

        //ids = new HashMap<Integer, Long>();
        ids = new LinkedList<Integer>();

        Log.d("Service", "Service running");

        registerEventReceiver(new EventReceiver<MyEvent>(MyEvent.class) {

            public void receiveEvent(final MyEvent event) {

                running = true;
                handler.post(new Runnable() {

                    public void run() {
                        Toast.makeText(JadexService.this, event.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }


    @Override
    public IBinder onBind(Intent intent) {
        return new MyServiceInterface();
    }

    @Override
    protected void onPlatformStarting() {
        super.onPlatformStarting();
        if (listener != null) {
            listener.onPlatformStarting();
        }
    }

    @Override
    protected void onPlatformStarted(IExternalAccess platform) {
        super.onPlatformStarted(platform);
        if (listener != null) {
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

    public void startFaceDetection() {
        startPlatform();
    }

    public int getThreshold(){return sendThreshold;}



    public void recognizeFace(int id, byte[] input){



        //problem bei vielen faces
        //passe sende häufigkeit an
        if(ids.size() > 0){
            sendThreshold += ids.size()*3;
            Log.d("Remote", "Threshold: " + sendThreshold);
        }
        else{
            sendThreshold = sendThreshold/2;
            Log.d("Remote", "Threshold: " + sendThreshold);
        }

        ids.add(id);


        ITuple2Future<byte[], Integer> fut = agent.recognizeFace(id, input);
        fut.addResultListener(new DefaultTuple2ResultListener<byte[], Integer>() {
            @Override
            public void exceptionOccurred(Exception exception) {

            }

            @Override
            public void firstResultAvailable(byte[] b) {
                Intent toIntent = new Intent("faceRecognized");
                toIntent.putExtra("img",  b);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(toIntent);
            }

            @Override
            public void secondResultAvailable(Integer id){
                if (ids.contains(id)){
                    ids.remove(id);
                }
            }


        });
    }


    public void detectFaces(int id, byte[] data) {


        //passe sende häufigkeit an
        if(ids.size() > 0){
            sendThreshold += ids.size()*5;
            Log.d("Remote", "Threshold: " + sendThreshold);
        }
        else{
            sendThreshold = sendThreshold/2;
            Log.d("Remote", "Threshold: " + sendThreshold);
        }

        ids.add(id);

        //ids.put(id, System.currentTimeMillis());

        Log.d("JadexService", "Map größe: "+ ids.size());

        ITuple2Future<List<Integer>, byte[]> fut = agent.getFaceArray(id, data);

        fut.addResultListener(new DefaultTuple2ResultListener<List<Integer>, byte[]>() {
            @Override
            public void exceptionOccurred(Exception exception) {

            }

            @Override
            public void firstResultAvailable(List<Integer> result) {

                    Intent toIntent = new Intent("faceDetected");
                    toIntent.putIntegerArrayListExtra("Data", (ArrayList<Integer>) result);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(toIntent);

                    if (ids.contains(result.get(0))){
                        ids.remove(result.get(0));
                    }

                  /*  if(ids.containsKey(result.get(0))){

                        if((System.currentTimeMillis() - ids.get(result.get(0)) ) > 1500){
                            sendThreshold = sendThreshold +10;
                            Log.d("Remote", "Threshold: " + sendThreshold);
                        }
                        else{
                            sendThreshold = sendThreshold/2;
                            Log.d("Remote", "Threshold: " + sendThreshold);
                        }
                        ids.remove(result.get(0));
                    }
                */
                    //Log.d("JadexService", "broadcasting Rect: " + result.toString());

            }

            @Override
            public void secondResultAvailable(byte[] result) {
                if(result.length>0) {
                    Intent intent = new Intent("faceRecognized");
                    intent.putExtra("img", result);

                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }
            }


        });


    }
}








