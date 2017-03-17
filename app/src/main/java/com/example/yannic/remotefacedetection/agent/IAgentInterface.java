package com.example.yannic.remotefacedetection.agent;


import java.util.List;

import jadex.commons.future.IFuture;

/**
 * Created by Yannic on 23.02.2017.
 */

public interface IAgentInterface {

    IFuture<List<Integer>> getFaceArray(int height, int width, byte[] data);

}
