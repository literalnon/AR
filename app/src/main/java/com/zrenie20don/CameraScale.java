package com.zrenie20don;

import android.graphics.Rect;
import android.util.Log;

/**
 * Created by Кирилл on 06.03.2018.
 */

public class CameraScale  {
    private final float ZOOM_MIN = 1.0f;
    private final int X_MIN = 0;
    private final int Y_MIN = 0;
    private int displayWidth;
    private int displayHeight;

    private Rect current_rect;
    private int xCenter;
    private int yCenter;
    private int xWidth;
    private int yHeight;
    private int xMax;
    private int yMax;
    private float zoomMax;
    private float zoomCurrent;

    public CameraScale(float zoomMax, int xMax, int yMax, int displayHeight, int displayWidth) {
        this.xMax = xMax;
        this.yMax = yMax;
        this.zoomMax = zoomMax;

        current_rect = new Rect(X_MIN,Y_MIN, xMax, yMax); //(0,0,xMax,yMax) as the starting rectangle
        zoomCurrent = ZOOM_MIN;
        xWidth = current_rect.width();
        yHeight = current_rect.height();
        xCenter = current_rect.centerX();
        yCenter = current_rect.centerY();

        this.displayHeight = displayHeight;
        this.displayWidth = displayWidth;
    }

    public void pan(float distanceX, float distanceY){
        //calculate the shift in the we want to take on the camera sensor with respect to the distance moved on the screen
        int xShift = Math.round((distanceX/displayWidth)*xWidth); //scales down to a percentage of the current view width->converts to a pixel shift
        int yShift = Math.round((distanceY/displayHeight)*yHeight); //scales down to a percentage of the current view height->converts to a pixel shift

        //check if the shift will push us pass our maximums, this should account for both negative and positive values of xShift and yShift correctly
        if ( !((xCenter + Math.round(xWidth/2.0) + xShift < xMax) && (xCenter - Math.round(xWidth/2.0) + xShift > 0))) { //if not within xBounds, set xShift to 0
            xShift = 0;
        }
        if ( !((yCenter + Math.round(yHeight/2) + yShift < yMax) && (yCenter - Math.round(yHeight/2.0) + yShift > 0))) { //if not within yBounds, set yShift to 0
            yShift = 0;
        }

        Log.d("Scaler", "pan: xShift" + xShift + " yShift " + yShift);
        current_rect.offset(xShift,yShift);
        Log.d("Scaler", "pan: current_rect" + current_rect.toString());
        xCenter = current_rect.centerX(); //update center
        yCenter = current_rect.centerY(); //update center
    }

    public void zoom(float scale_change){
        if ( (zoomCurrent*scale_change < zoomMax) && (zoomCurrent*scale_change > ZOOM_MIN) ){ //if we are within zoom bounds
            zoomCurrent *= scale_change; //update the zoom factor
            int newWidthHalf = (int)Math.floor(xMax/zoomCurrent/2.0);
            int newHeightHalf = (int)Math.floor(yMax/zoomCurrent/2.0);
            int xTempCenter = xCenter;
            int yTempCenter = yCenter;

            //if at edge we need to shift and scale
            if (xCenter + newWidthHalf > xMax) { //if at right edge
                xTempCenter = xMax - newWidthHalf; //shift center to the left
            } else if (xCenter - newWidthHalf < 0) { //if at left edge
                xTempCenter = newWidthHalf; //shift center to the right
            }
            if (yCenter + newHeightHalf > yMax) { //if at bottom
                yTempCenter = yMax - newHeightHalf; //shift center up
            } else if (yCenter - newHeightHalf < 0) { //if at top
                yTempCenter = newHeightHalf; //shift center down
            }
            Log.d("Scaler", "zoom: " + zoomCurrent);
            Log.d("TAG", "current center(x,y) " + xTempCenter + " " + yTempCenter + "current halfwidths(x,y) " + newWidthHalf + " " + newHeightHalf);
            current_rect.set(xTempCenter - newWidthHalf, yTempCenter - newHeightHalf,xTempCenter + newWidthHalf, yTempCenter + newHeightHalf);
            Log.d("Scaler", "zoom: current_rect" + current_rect.toString());
            xWidth = current_rect.width();
            yHeight = current_rect.height();
            xCenter = current_rect.centerX(); //update center
            yCenter = current_rect.centerY(); //update center
        } //if not in digital zoom bounds, do nothing
    }

    public Rect getCurrentView() {
        return current_rect;
    }
}