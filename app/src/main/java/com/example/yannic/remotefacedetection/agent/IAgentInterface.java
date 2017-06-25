package com.example.yannic.remotefacedetection.agent;


import java.util.List;

import jadex.commons.future.IFuture;
import jadex.commons.future.ITuple2Future;

/**
 * Created by Yannic on 23.02.2017.
 */

public interface IAgentInterface {

    ITuple2Future<List<Integer>, byte[]> getFaceArray(int id, byte[] data);

    ITuple2Future<byte[], Integer> recognizeFace(int id, byte[] input);

}
