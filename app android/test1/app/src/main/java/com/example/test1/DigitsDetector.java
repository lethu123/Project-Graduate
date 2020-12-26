package com.example.test1;

import android.animation.FloatArrayEvaluator;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.SystemClock;
import android.service.controls.actions.FloatAction;
import android.util.Log;
import android.widget.ImageView;

import org.tensorflow.lite.Interpreter;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class DigitsDetector {
    private final String TAG = this.getClass().getSimpleName();

    // The tensorflow lite file
    private Interpreter tflite;

    // Input byte buffer
    private ByteBuffer inputBuffer = null;

    // Output array [batch_size, 10]
    private float[][] mnistOutput = null;
    private ArrayList<Float> list = new ArrayList<Float>();

    // Name of the file in the assets folder
    private static final String MODEL_PATH = "lenet5.tflite";

    // Specify the output size
    private static final int NUMBER_LENGTH = 10;

    // Specify the input size
    private static final int DIM_BATCH_SIZE = 1;
    private static final int DIM_IMG_SIZE_X = 28;
    private static final int DIM_IMG_SIZE_Y = 28;
    private static final int DIM_PIXEL_SIZE = 1;

    // Number of bytes to hold a float (32 bits / float) / (8 bits / byte) = 4 bytes / float
    private static final int BYTE_SIZE_OF_FLOAT = 4;


    public DigitsDetector(Activity activity) {
        try {
            tflite = new Interpreter(loadModelFile(activity));
            inputBuffer =
                    ByteBuffer.allocateDirect(
                            BYTE_SIZE_OF_FLOAT * DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE);
            inputBuffer.order(ByteOrder.nativeOrder());
            mnistOutput = new float[DIM_BATCH_SIZE][NUMBER_LENGTH];
            Log.d(TAG, "Created a Tensorflow Lite MNIST Classifier.");
//            writeToFile(activity.getApplicationContext(), list);
        } catch (IOException e) {
            Log.e(TAG, "IOException loading the tflite file");
        }
    }

    /**
     * Run the TFLite model
     */
    protected void runInference() {
        tflite.run(inputBuffer, mnistOutput);
    }

    /**
     * Classifies the number with the mnist model.
     *
     * @param bitmap
     * @return the identified number
     */
//    public int classify(Bitmap bitmap) {
//        if (tflite == null) {
//            Log.e(TAG, "Image classifier has not been initialized; Skipped.");
//        }
//        preprocess(bitmap);
//        runInference();
//        return postprocess();
//    }

    public String classify(Bitmap bitmap) {
        if (tflite == null) {
            Log.e(TAG, "Image classifier has not been initialized; Skipped.");
        }
        preprocess(bitmap);
        runInference();
        return postprocess();
    }

    /**
     * Go through the output and find the number that was identified.
     *
     * @return the number that was identified (returns -1 if one wasn't found)
     */

    private String postprocess() {
        if(getMaxDetect(mnistOutput[0]) != "") {
            return getMaxDetect(mnistOutput[0]);
        }
        return "";
    }

    /**
     * Load the model file from the assets folder
     */
    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_PATH);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    /**
     * Converts it into the Byte Buffer to feed into the model
     *
     * @param bitmap
     */
    private void preprocess(Bitmap bitmap) {
        if (bitmap == null || inputBuffer == null) {
            return;
        }
        // Reset the image data
        inputBuffer.rewind();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        long startTime = SystemClock.uptimeMillis();
        // The bitmap shape should be 28 x 28
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int pixel = 0;
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                final int val = pixels[pixel++];
                Float r = ((val>> 16) & 0xff)/1.0f;
                Float g = ((val>> 8) & 0xff) / 1.0f;
                Float b = (val & 0xff) / 1.0f;
                Float normalizedPixelValue = (r + g + b) / 3.0f / 255.0f;
                System.out.println("image matrix " + i +  j + " " + normalizedPixelValue);
                list.add(normalizedPixelValue);
                inputBuffer.putFloat(normalizedPixelValue);
            }
        }
        long endTime = SystemClock.uptimeMillis();
        Log.d(TAG, "Time cost to put values into ByteBuffer: " + Long.toString(endTime - startTime));
        writeToFile(list);
    }


     private String getMaxDetect(float arr[]) {
        float max = arr[0];
        int maxIndex = 0;
         for(int i = 0; i< arr.length ; i++) {
             if(arr[i] > max) {
                 max = arr[i];
                 maxIndex = i;
             }
         }
        return  String.format("Số dự đoán =  %d\nĐộ chính xác = %f",maxIndex, max ) ;
     }

    public void writeToFile(  ArrayList<Float> arr)
    {
        // Get the directory for the user's public pictures directory.

        final File path = new
                File(Environment.getExternalStorageDirectory() + "/Documents");

        // Make sure the path directory exists.
        if(!path.exists())
        {
            // Make it, if it doesn't exit
            path.mkdirs();
        }

        final File file = new File(path, "abc.txt");

        try
        {
//            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            DataOutputStream dos = new DataOutputStream(fOut);

            for (Float str : arr) {
                System.out.println("ahihi" + str);
                dos.writeFloat(str);
            }
            dos.close();

            fOut.flush();
            fOut.close();
            System.out.println("Done!");
        }
        catch (IOException e)
        {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

}
