package com.example.yannic.remotefacedetection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.View;

import java.util.List;

/**
 * Created by Yannic on 17.03.2017.
 */
public class Box extends View {
    private Paint paint = new Paint();
    private boolean draw;
   // public Canvas _canvas;

    int[] x;
    int[] y;
    int[] width;
    int[] height;
    Box(Context context, List<Integer> faces) {
        super(context);
        draw = false;
        int faceCount = (faces.size()-1)/4;
        x = new int[faceCount];
        y = new int[faceCount];
        width = new int[faceCount];
        height = new int[faceCount];

        if(faces.size()>1){
            draw=true;
        }


        int i = 0;
        for(int counter = 1; counter < faces.size(); counter+=4){

            x[i] = (int) (faces.get(counter)*1.34);
            y[i]  = (int) (faces.get(counter+1)*1.34);
            width[i]  = faces.get(counter+2);
            height[i]  = faces.get(counter+3);
            i++;

        }

    }


    public void clearCanvas(){
     //   _canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }

    @Override
    protected void onDraw(Canvas canvas) { // Override the onDraw() Method
        super.onDraw(canvas);

        //_canvas = canvas;
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(10);

        //center

        //draw guide box

        if(draw) {
            for(int c = 0; c < x.length; c++) {
                canvas.drawRect(x[c], y[c], x[c] + width[c], y[c] + height[c], paint);
            }
        }
        }
}
