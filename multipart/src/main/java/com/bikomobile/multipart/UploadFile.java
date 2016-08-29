package com.bikomobile.multipart;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Copyright 2016 Bitban Technologies, S.L.
 * All right reserved.
 */
public class UploadFile {


    private final static String TWO_HYPHENS = "--";
    private final static String LINE_END = "\r\n";
    private final static String BOUNDARY = "apiclient-" + System.currentTimeMillis();
    private final static String MIME_TYPE = "multipart/form-data;boundary=" + BOUNDARY;

    private final Context mContext;
    private int mMaxSize = 1024 * 1024 * 50; // 50 MB
    private List<EntryMultipart> mMultipartParams = new ArrayList<>();
    private List<EntryMultipart> mMultipartToSend = new ArrayList<>();
    private EntryMultipart mFileEntryMultipart;

    public UploadFile(Context context) {
        this.mContext = context;
    }

    /**
     * Set max size from file
     * @param bytes in bytes
     */
    public void setMaxSizeFromFile(int bytes) {
        this.mMaxSize = bytes;
    }

    /**
     * Added a file to send request
     * This url can be parser to {@link PathUtil#getPath(Context, Uri)}
     * if the url does not belong to a file.
     *
     * @param contentType   content type (image/jpeg, video/mp4...)
     * @param postParam     post param name
     * @param fileName      file name
     * @param uri           file uri
     */
    public void addFile(String contentType, String postParam, String fileName, Uri uri) {

        byte [] bytes = null;

        File file = new File(uri.toString());

        if (file.isFile()) {
            bytes = getBytesFromFile(file);
        } else {
            String videoUrl = PathUtil.getPath(mContext, uri);
            if (videoUrl != null) {
                bytes = getBytesFromUri(Uri.parse(videoUrl));
            } else {
                // if the image is a GoogleDrive document generate bitmap and convert to bytes
                if (PathUtil.isDriveDocument(uri)) {
                    if (contentType.equalsIgnoreCase("video/mp4")) {
                        bytes = getBytesFromVideoUri(mContext, uri);
                    } else {
                        bytes = getBytesFromImageUri(mContext, uri);
                    }
                }
            }
        }

        if (bytes == null) {
            return;
        }

        mFileEntryMultipart = new EntryMultipart(contentType, postParam, fileName, bytes);
    }

    /**
     * Added a post param to send request
     *
     * @param key       post param key
     * @param value     post param name
     */
    public void addParam(String key, String value) {
        this.mMultipartParams.add(new EntryMultipart(key, value.getBytes()));
        this.mMultipartToSend = new ArrayList<>(mMultipartParams);
    }

    /**
     * Added a map of params to send request
     *
     * @param params the map of params
     */
    public void addParams(Map<String, String> params) {
        for (String key : params.keySet()) {
            EntryMultipart entryMultipart =
                    new EntryMultipart("text/plain", key, params.get(key).getBytes());

            this.mMultipartParams.add(entryMultipart);
        }
        this.mMultipartToSend = new ArrayList<>(mMultipartParams);
    }

    /**
     * Launch a request from {@link com.android.volley.Request} to send a file
     *
     * @param url           url from destination
     * @param listener      listener interface for response
     * @param errorListener listener interface for errors
     */
    public void launchRequest(String url, Response.Listener<NetworkResponse> listener, Response.ErrorListener errorListener) {
        launchRequest(url, null, listener, errorListener);
    }

    /**
     * Launch a request from {@link com.android.volley.Request} to send a file
     *
     * @param url           url from destination
     * @param headers       headers
     * @param listener      listener interface for response
     * @param errorListener listener interface for errors
     */
    public void launchRequest(String url, Map<String, String> headers, Response.Listener<NetworkResponse> listener, Response.ErrorListener errorListener) {
        if (mMultipartParams.isEmpty()) {
            Log.e(getClass().getCanonicalName(), "Added a file first");
            return;
        }

        List<MultipartRequest> requests = getRequests(url, headers, listener, errorListener);

        for (MultipartRequest request : requests) {
                RequestQueue requestQueue = Volley.newRequestQueue(mContext);
                requestQueue.add(request);
        }
    }

    public List<MultipartRequest> getRequests(String url, Response.Listener<NetworkResponse> listener, Response.ErrorListener errorListener) {
        return getRequests(url, null, listener, errorListener);
    }

    /**
     * Return the requests generated just in case you want launch yourself
     *
     * @param url           url from destination
     * @param headers
     *@param listener      listener interface for response
     * @param errorListener listener interface for errors   @return the request generated
     */
    public List<MultipartRequest> getRequests(String url, Map<String, String> headers, Response.Listener<NetworkResponse> listener, Response.ErrorListener errorListener) {
        List<MultipartRequest> requests = new ArrayList<>();

        int count = mFileEntryMultipart.getData().length / mMaxSize;
        if ((mFileEntryMultipart.getData().length % mMaxSize) != 0) {
            count++;
        }


        for (int post = 1; post <= count; post++) {

            mMultipartToSend = new ArrayList<>(mMultipartParams);

            int size = Math.min(mFileEntryMultipart.getData().length, mMaxSize);

            byte[] bytesFromSend = new byte[size];
            System.arraycopy(mFileEntryMultipart.getData(), 0, bytesFromSend, 0, size);

            byte[] aux = new byte[mFileEntryMultipart.getData().length - size];
            System.arraycopy(mFileEntryMultipart.getData(), size, aux, 0, mFileEntryMultipart.getData().length - size);

            mFileEntryMultipart.setData(aux);

            mMultipartToSend.add(new EntryMultipart("chunk", (post+"").getBytes()));
            mMultipartToSend.add(new EntryMultipart("chunks", (count+"").getBytes()));
            mMultipartToSend.add(new EntryMultipart(mFileEntryMultipart.getContentType(),
                    mFileEntryMultipart.getPostName(), mFileEntryMultipart.getFilename(),
                    bytesFromSend));

            MultipartRequest request = getRequest(url, headers, listener, errorListener);
            requests.add(request);

        }

        return requests;
    }

    /**
     * Return the request generated just in case you want launch yourself
     *
     * @param url           url from destination
     * @param listener      listener interface for response
     * @param errorListener listener interface for errors
     * @return the request generated
     */
    public MultipartRequest getRequest(String url, Response.Listener<NetworkResponse> listener, Response.ErrorListener errorListener) {
        return getRequest(url, null, listener, errorListener);
    }

    /**
     * Return the request generated just in case you want launch yourself
     *
     * @param url           url from destination
     * @param headers
     *@param listener      listener interface for response
     * @param errorListener listener interface for errors   @return the request generated
     */
    public MultipartRequest getRequest(String url, Map<String, String> headers, Response.Listener<NetworkResponse> listener, Response.ErrorListener errorListener) {
        if (mMultipartParams.isEmpty()) {
            Log.e(getClass().getCanonicalName(), "Added a file first");
            return null;
        }

        byte[] multipartBody = getMultipartBody(mMultipartToSend);

        return new MultipartRequest(url, headers, MIME_TYPE, multipartBody, listener, errorListener);
    }

    private byte[] getMultipartBody(List<EntryMultipart> multipartParams) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        for (EntryMultipart entryMultipart : multipartParams) {

            try {
                buildEntryMultiPart(dataOutputStream, entryMultipart);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        // send multipart form data necessary after file data
        try {
            dataOutputStream.writeBytes(TWO_HYPHENS + BOUNDARY + TWO_HYPHENS + LINE_END);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // pass to multipart body
        return byteArrayOutputStream.toByteArray();

    }

    private void buildEntryMultiPart(DataOutputStream dataOutputStream,
                                     EntryMultipart entryMultipart) throws IOException {

        dataOutputStream.writeBytes(TWO_HYPHENS + BOUNDARY + LINE_END);

        dataOutputStream.writeBytes("Content-Disposition: form-data;");
        dataOutputStream.writeBytes(" name=\"" + entryMultipart.getPostName() + "\"; ");

        if (entryMultipart.getFilename() != null) {
            dataOutputStream.writeBytes(" filename=\"" + entryMultipart.getFilename() + "\"");
            dataOutputStream.writeBytes(LINE_END);
            dataOutputStream.writeBytes("Content-Type: " + entryMultipart.getContentType());
        }

        dataOutputStream.writeBytes(LINE_END);
        dataOutputStream.writeBytes(LINE_END);

        dataOutputStream.write(entryMultipart.getData());

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

    public static byte [] getBytesFromVideoUri(Context context, Uri uri) {
        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (inputStream == null) {
            return null;
        }

        int maxBufferSize = 1024 * 1024;
        int available = 0;
        try {
            available = inputStream.available();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (available == 0) {
            available = maxBufferSize;
        }

        int bufferSize = Math.min(available, maxBufferSize);

        byte[] data = new byte[bufferSize];
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;

        try {
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            buffer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return buffer.toByteArray();
    }


    private static byte[] getBytesFromImageUri(Context context, Uri uri) {
        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Bitmap bmp = BitmapFactory.decodeStream(inputStream);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);

        return stream.toByteArray();
    }

}
