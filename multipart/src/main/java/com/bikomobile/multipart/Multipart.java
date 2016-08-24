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

public class Multipart {

    private final static String TWO_HYPHENS = "--";
    private final static String LINE_END = "\r\n";
    private final static String BOUNDARY = "apiclient-" + System.currentTimeMillis();
    private final static String MIME_TYPE = "multipart/form-data;boundary=" + BOUNDARY;

    private final Context mContext;
    private List<EntryMultipart> mMultipartParams = new ArrayList<>();

    public Multipart(Context context) {
        this.mContext = context;
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

        EntryMultipart entryMultipart = new EntryMultipart(contentType, postParam, fileName, bytes);
        this.mMultipartParams.add(entryMultipart);
    }

    /**
     * Added a post param to send request
     *
     * @param key       post param key
     * @param value     post param name
     */
    public void addParam(String key, String value) {
        this.mMultipartParams.add(new EntryMultipart(key, value.getBytes()));
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
    }

    /**
     * Launch a request from {@link com.android.volley.Request} to send a file
     *
     * @param url           url from destination
     * @param listener      listener interface for response
     * @param errorListener listener interface for errors
     */
    public void launchRequest(String url, Response.Listener<NetworkResponse> listener, Response.ErrorListener errorListener) {
        if (mMultipartParams.isEmpty()) {
            Log.e(getClass().getCanonicalName(), "Added a file first");
            return;
        }

        MultipartRequest multipartRequest = getRequest(url, listener, errorListener);

        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.add(multipartRequest);
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
        if (mMultipartParams.isEmpty()) {
            Log.e(getClass().getCanonicalName(), "Added a file first");
            return null;
        }

        return new MultipartRequest(url, null, MIME_TYPE, getMultipartBody(mMultipartParams), listener, errorListener);
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

        byte[] bytes = entryMultipart.getData();
        dataOutputStream.write(bytes);

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
