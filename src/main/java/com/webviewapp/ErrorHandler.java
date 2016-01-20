package com.webviewapp;

import android.util.Log;

/**
 * Created by s.shvyrev on 20.01.16.
 */
public class ErrorHandler {
    private static final String TAG = ErrorHandler.class.getSimpleName();

    public static void onHttpError(int statusCode, String response) {
        Log.e(TAG, ".onHttpError status:" + statusCode + " response: " + response);
    }

    public static void onJsonError(StackTraceElement[] stackTrace) {
        Log.e(TAG, ".onJsonError:" + stackTrace.toString());
    }

    public static void onLoadingError(StackTraceElement[] stackTrace) {
        Log.e(TAG, ".onLoadingError:" + stackTrace.toString());
    }
}
