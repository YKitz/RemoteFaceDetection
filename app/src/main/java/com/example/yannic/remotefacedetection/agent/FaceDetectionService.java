package com.example.yannic.remotefacedetection.agent;

import java.util.List;

import jadex.commons.future.IFuture;
import jadex.commons.future.ITuple2Future;

/**
 * Created by Yannic on 23.02.2017.
 */
//@Security(Security.UNRESTRICTED)
public interface FaceDetectionService {


        String test();


        ITuple2Future<List<Integer>, byte[]> getFrame(int id, byte[] data);

        ITuple2Future<byte[], Integer> recognizeFace(int id, byte[] inputFace);

}
