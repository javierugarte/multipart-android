package com.bikomobile.multipart;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Multipart {

    private final static String TWO_HYPHENS = "--";
    private final static String LINE_END = "\r\n";
    private final static String BOUNDARY = "apiclient-" + System.currentTimeMillis();
    private final static String MIME_TYPE = "multipart/form-data;boundary=" + BOUNDARY;

    private final Context mContext;
    private String mFileName;
    private byte[] mBytes;

    public Multipart(Context context) {
        this.mContext = context;
    }

    /**
     * Added a file to send request
     * This url can be parser to {@link PathUtil#getPath(Context, Uri)}
     * if the url does not belong to a file.
     *
     * @param fileName file name
     * @param uri      file uri
     */
    public void addFile(String fileName, Uri uri) {
        this.mFileName = fileName;

        File file = new File(uri.toString());

        if (file.isFile()) {
            mBytes = getBytesFromFile(file);
        } else {
            String videoUrl = PathUtil.getPath(mContext, uri);
            mBytes = getBytesFromUri(Uri.parse(videoUrl));
        }
    }

    /**
     * Launch a request from {@link com.android.volley.Request} to send a file
     *
     * @param url           url from destination
     * @param postParamName name from param
     * @param listener      listener interface for response
     * @param errorListener listener interface for errors
     */
    public void launchRequest(String url, String postParamName, Response.Listener<NetworkResponse> listener, Response.ErrorListener errorListener) {
        if (mBytes == null) {
            Log.e(getClass().getCanonicalName(), "Added a file");
            return;
        }

        MultipartRequest multipartRequest = getRequest(url, postParamName, listener, errorListener);

        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.add(multipartRequest);
    }

    /**
     * Return the request generated just in case you want launch yourself
     *
     * @param url           url from destination
     * @param postParamName name from param
     * @param listener      listener interface for response
     * @param errorListener listener interface for errors
     * @return the request generated
     */
    public MultipartRequest getRequest(String url, String postParamName, Response.Listener<NetworkResponse> listener, Response.ErrorListener errorListener) {
        return new MultipartRequest(url, null, MIME_TYPE, getMultipartBody(mBytes, postParamName, mFileName), listener, errorListener);
    }

    private byte[] getMultipartBody(byte[] bytes, String name, String fileName) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            buildMultipartBody(dataOutputStream, bytes, name, fileName);
            // send multipart form data necessary after file data
            dataOutputStream.writeBytes(TWO_HYPHENS + BOUNDARY + TWO_HYPHENS + LINE_END);
            // pass to multipart body
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void buildMultipartBody(DataOutputStream dataOutputStream, byte[] fileData, String name, String fileName) throws IOException {
        dataOutputStream.writeBytes(TWO_HYPHENS + BOUNDARY + LINE_END);
        dataOutputStream.writeBytes("Content-Disposition: form-data; " +
                "name=\"" + name + "\"; " +
                "filename=\"" + fileName + "\"" + LINE_END);

        dataOutputStream.writeBytes(LINE_END);

        ByteArrayInputStream fileInputStream = new ByteArrayInputStream(fileData);
        int bytesAvailable = fileInputStream.available();

        int maxBufferSize = 1024 * 1024;
        int bufferSize = Math.min(bytesAvailable, maxBufferSize);
        byte[] buffer = new byte[bufferSize];

        // read file and write it into form...
        int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

        while (bytesRead > 0) {
            dataOutputStream.write(buffer, 0, bufferSize);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        }

        dataOutputStream.writeBytes(LINE_END);
    }

    private static byte[] getBytesFromUri(Uri uri) {
        File file = new File(uri.toString());
        return getBytesFromFile(file);
    }

    private static byte[] getBytesFromFile(File file) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            int maxBufferSize = 1024 * 1024;
            int bufferSize = (int) Math.min(file.getTotalSpace(), maxBufferSize);
            byte[] buffer = new byte[bufferSize];

            // read file and write it into form...
            int bytesRead = 0;

            if (fileInputStream != null) {
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            while (bytesRead > 0) {
                dataOutputStream.write(buffer, 0, bufferSize);
                int bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return byteArrayOutputStream.toByteArray();
    }

}
