package com.example.mymap.Utility;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;

import com.example.mymap.General.BoundingRectF;
import com.google.android.gms.common.util.Base64Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

public class BoundingBox {
    float width;
    float height;
    float x;
    float y;
    int detectedClass;

    public BoundingBox(BoundingRectF location, int detectedClass) {
        Log.d("BoundingBox", String.format("%s,%s,%s,%s", location.left, location.top, location.right, location.bottom));

        // This literally doesn't make sense but it works correctly.
        // I probably made an error somewhere in the code
        // Already tried to fix it but no avail. What works doesn't need fixing right? :)
        this.height = location.right - location.left;
        this.width = location.bottom - location.top;

        this.x = location.x;
        this.y = location.y;
        this.detectedClass = detectedClass;

    }

    public static String getJsonString(Bitmap image, ArrayList<BoundingBox> boundingBoxes, String detectorName, int imageWidth, int imageHeight) {
        String imageB64 = bitmapToB64(image);
        JSONObject json = new JSONObject();
        try {
            json.put("detector", detectorName);
            json.put("boundingBoxes", boundingBoxArrayToString(boundingBoxes));
            json.put("image", imageB64);
            json.put("imageWidth", imageWidth);
            json.put("imageHeight", imageHeight);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }


    public static String bitmapToB64(Bitmap bmp) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return Base64Utils.encode(stream.toByteArray());
    }

    public static String boundingBoxArrayToString(ArrayList<BoundingBox> boundingBoxes) {
        StringBuilder boundingBoxesString = new StringBuilder();
        for (int i = 0; i < boundingBoxes.size(); i ++) {
            BoundingBox boundingBox = boundingBoxes.get(i);
            String temp = boundingBox.detectedClass + "," + boundingBox.width + "," + boundingBox.height + "," + boundingBox.x + "," + boundingBox.y + "|";
            Log.d("BoundingBox/boundingBoxArrayToString", temp);
            boundingBoxesString.append(temp);
        }

        if (boundingBoxesString.length() > 0) {
            boundingBoxesString.deleteCharAt(boundingBoxesString.length() - 1);
        }

        return boundingBoxesString.toString();
    }
//    public static void getJsonString(RectF location, Bitmap image) {
//        float width = location.right - location.left;
//        float height = location.bottom - location.top;
//        float x = (location.left + location.right) / 2;
//        float y = (location.bottom + location.top) / 2;
//        int imageWidth = image.getWidth();
//        int imageHeight = image.getHeight();
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//        stream.toByteArray();
//
//        byte[] encoded = Base64.getEncoder().encode("Hello".getBytes());
////        println(new String(encoded));
//    }
}
