package com.example.mymap;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.mymap.customview.OverlayView;
import com.example.mymap.env.BorderedText;
import com.example.mymap.env.ImageUtils;
import com.example.mymap.tflite.DetectorFactory;
import com.example.mymap.tflite.YoloV5Classifier;
import com.example.mymap.tracking.MultiBoxTracker;

import java.io.IOException;

public class LandmarkDetectorActivity extends DetectorActivity {
    String forcedModelString;
    static public boolean active;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        forcedModelString = getIntent().getStringExtra("modelString");

        try {
            this.detector = DetectorFactory.getDetector(getAssets(), forcedModelString, context);
            this.detector.useGpu();
            detector.setNumThreads(9);
            Log.d("LandmarkDetectorActivity/onCreate", "model: " + detector);
        } catch (IOException e) {
            e.printStackTrace();
        }

        modelView.setAlpha(0.75f);
        modelView.setBackgroundColor(Color.GRAY);
        modelView.setEnabled(false);
    }


    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }

//    @Override
//    protected void updateActiveModel() {
//        // Get UI information before delegating to background
//        Log.d("LandmarkDetectorActivity/updateActiveModel", "using custom model settings for landmark detection");
//        final int numThreads = 9;
//
//        handler.post(() -> {
//            currentNumThreads = numThreads;
//
//            // Disable classifier while updating
//            if (detector != null) {
//                detector.close();
//                detector = null;
//            }
//
//            // Lookup names of parameters.
//            String modelString = forcedModelString;
//            String device = "GPU";
//
//            LOGGER.i("Changing model to " + modelString + " device " + device);
//
//            // Try to load model.
//
//            try {
//                detector = DetectorFactory.getDetector(getAssets(), modelString, context);
//                // Customize the interpreter to the type of device we want to use.
//                if (detector == null) {
//                    return;
//                }
//            }
//            catch(IOException e) {
//                e.printStackTrace();
//                LOGGER.e(e, "Exception in updateActiveModel()");
//                Toast toast =
//                        Toast.makeText(
//                                getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
//                toast.show();
//                finish();
//            }
//
//
//            if (device.equals("CPU")) {
//                detector.useCPU();
//            } else if (device.equals("GPU")) {
//                detector.useGpu();
//            } else if (device.equals("NNAPI")) {
//                detector.useNNAPI();
//            }
//            detector.setNumThreads(numThreads);
//
//            int cropSize = detector.getInputSize();
//            croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888);
//
//            frameToCropTransform =
//                    ImageUtils.getTransformationMatrix(
//                            previewWidth, previewHeight,
//                            cropSize, cropSize,
//                            sensorOrientation, MAINTAIN_ASPECT);
//
//            cropToFrameTransform = new Matrix();
//            frameToCropTransform.invert(cropToFrameTransform);
//        });
//    }

    @Override
    public void onPreviewSizeChosen(Size size, int rotation) {
        final float textSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        tracker = new MultiBoxTracker(this);

        int cropSize = detector.getInputSize();

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        sensorOrientation = rotation - getScreenOrientation();
        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888);

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        cropSize, cropSize,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                new OverlayView.DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        tracker.draw(canvas);
                        if (isDebug()) {
                            tracker.drawDebug(canvas);
                        }
                    }
                });

        tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);

    }
}
