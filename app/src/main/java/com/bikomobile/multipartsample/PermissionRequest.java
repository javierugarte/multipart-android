package com.bikomobile.multipartsample;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;


public class PermissionRequest {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static PermissionCallback stPermissionCallback;

    // Interface
    public interface PermissionCallback {
        void permissionGranted();
        void permissionDenied();
    }

    /**
     * Returns true if the application has access to given permissions.
     */
    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request the permission given.
     * @param activity The target activity.
     * @param permission The requested permission.
     * @param permissionCallback callback
     */
    public static void askForPermission(Activity activity, String permission, PermissionCallback permissionCallback) {
        stPermissionCallback = permissionCallback;

        if (permissionCallback == null) {
            return;
        }

        if (hasPermission(activity, permission)) {
            permissionCallback.permissionGranted();
            return;
        }

        ActivityCompat.requestPermissions(
                activity,
                new String[]{permission},
                PERMISSION_REQUEST_CODE);
    }

    public static void onRequestPermissionsResult(Activity activity, int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST_CODE && permissions.length > 0) {

            if (hasPermission(activity, permissions[0])) {
                stPermissionCallback.permissionGranted();
            } else {
                stPermissionCallback.permissionDenied();
            }
        }
    }

}