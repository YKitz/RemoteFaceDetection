package com.example.yannic.remotefacedetection.agent;

import android.util.Log;


import com.example.yannic.remotefacedetection.MyEvent;

import java.util.ArrayList;
import java.util.List;

import jadex.bridge.IInternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.component.IRequiredServicesFeature;
import jadex.bridge.service.types.context.IContextService;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.kernelbase.ExternalAccess;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.AgentService;
import jadex.micro.annotation.Binding;
import jadex.micro.annotation.Description;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;

/**
 * Created by Yannic on 04.03.2017.
 */
@Description("Agent zur Auslagerung der FaceDetection")
@RequiredServices({@RequiredService(name="faceDetectionService", type = FaceDetectionService.class, binding = @Binding(scope = RequiredServiceInfo.SCOPE_GLOBAL)),
        @RequiredService(name="context", type=IContextService.class, binding=@Binding(scope= RequiredServiceInfo.SCOPE_PLATFORM))})
@ProvidedServices({@ProvidedService(name = "IAgentInterface", type = IAgentInterface.class)})
@Agent
@Service
public class RemoteFaceDetectionAgent implements IAgentInterface{




    private FaceDetectionService fds;

    @AgentService
    protected IContextService context;


    @AgentBody
    public IFuture<Void> executeBody(IInternalAccess agent)
    {
        Log.d("RemoteAgent", "Agent body execute");
        fds = (FaceDetectionService) agent.getComponentFeature(IRequiredServicesFeature.class).getRequiredService("faceDetectionService").get();
        addFaceDetectionService();

        showAndroidMessage("com.example.yannic.remotefacedetection.agent rdy");

        return new Future<Void>();
    }


    public IFuture<Void> agentKilled(ExternalAccess agent)
    {
        Log.d("RemoteAgent","This is Agent <<" + agent.getComponentIdentifier().getLocalName() + ">> saying goodbye!");
        return IFuture.DONE;
    }




    public void addFaceDetectionService() {
        String str = fds.test();
        Log.d("RemoteAgent", str);
    }


    @Override
    public IFuture<List<Integer>> getFaceArray(int height, int width, byte[] data){

        if(fds != null) {

            /*
            int length = (int) (mat.total() * mat.elemSize());
            byte buffer[] = new byte[length];
            mat.get(0, 0, buffer);

            */

            Log.d("RemoteAgent", "länge: " + data.length);

            IFuture<List<Integer>> fut = fds.getFrame(height, width, data);
            fut.addResultListener(new DefaultResultListener<List<Integer>>() {
                public void resultAvailable(List<Integer> result) {
                    Log.d("RemoteAgent", "Result ist da: " + result.toString());
                }
            });
            String str = fds.test();
            Log.d("RemoteAgent", str);
           // Log.d("RemoteAgent", fut.get());
            return fut;
        }else{
            Log.d("RemoteAgent", "fds null");
        }

        return new Future<List<Integer>>(new ArrayList<Integer>());

    }


    protected void showAndroidMessage(String txt)
    {
        MyEvent myEvent = new MyEvent();
        myEvent.setMessage(txt);
        context.dispatchEvent(myEvent);
    }
}
