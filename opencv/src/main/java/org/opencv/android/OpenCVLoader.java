package org.opencv.android;

import android.content.Context;
import android.util.Log;

public class OpenCVLoader {
    private static final String TAG = "OpenCVLoader";
    
    public static final String OPENCV_VERSION = "4.8.0";
    private static boolean sInitialized = false;

    public static boolean initDebug() {
        if (sInitialized) {
            return true;
        }

        try {
            System.loadLibrary("opencv_java4");
            sInitialized = true;
            Log.d(TAG, "OpenCV library loaded successfully");
            return true;
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to load OpenCV library: " + e.getMessage());
            return false;
        }
    }

    public static boolean initAsync(String version, Context context, LoaderCallbackInterface callback) {
        if (sInitialized) {
            return true;
        }

        try {
            System.loadLibrary("opencv_java4");
            sInitialized = true;
            if (callback != null) {
                callback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            }
            return true;
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to load OpenCV library: " + e.getMessage());
            if (callback != null) {
                callback.onManagerConnected(LoaderCallbackInterface.INIT_FAILED);
            }
            return false;
        }
    }

    public interface LoaderCallbackInterface {
        int SUCCESS = 0;
        int INIT_FAILED = 1;
        void onManagerConnected(int status);
    }
}