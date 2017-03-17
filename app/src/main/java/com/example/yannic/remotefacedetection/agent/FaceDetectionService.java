package com.example.yannic.remotefacedetection.agent;

import java.util.List;

import jadex.commons.future.IFuture;

/**
 * Created by Yannic on 23.02.2017.
 */
//@Security(Security.UNRESTRICTED)
public interface FaceDetectionService {


        String test();


        IFuture<List<Integer>> getFrame(int height, int width, byte[] data);

}
