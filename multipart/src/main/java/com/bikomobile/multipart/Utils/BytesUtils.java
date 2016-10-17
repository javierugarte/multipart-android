package com.bikomobile.multipart.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Copyright 2016 Bitban Technologies, S.L.
 * All right reserved.
 */
public class BytesUtils {

    public static byte[] getBytesFromImageUri(Context context, Uri imageUri) {
        return getBytesFromUri(context, imageUri, false);
    }

    public static byte[] getBytesFromVideoUri(Context context, Uri videoUri) {
        return getBytesFromUri(context, videoUri, true);
    }

    private static byte[] getBytesFromUri(Context context, Uri uri, boolean isVideo) {

        byte[] bytes = null;

        File file = new File(uri.toString());

        if (!file.isFile()) {
            // get the absolute url
            String absoluteUrl = PathUtil.getPath(context, uri);

            if (absoluteUrl != null) {
                uri = Uri.parse(absoluteUrl);
                file = new File(absoluteUrl);
            }
        }

        bytes = getBytesFromFile(file);

        if (bytes == null || bytes.length == 0) {
            // if the file is a GoogleDrive document
            if (PathUtil.isDriveDocument(uri)) {
                if (isVideo) {
                    bytes = getBytesFromDriveVideoUri(context, uri);
                } else {
                    bytes = getBytesFromDriveImageUri(context, uri);
                }
            }
        }

        return bytes;
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

    private static byte[] getBytesFromDriveImageUri(Context context, Uri uri) {
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

    private static byte [] getBytesFromDriveVideoUri(Context context, Uri uri) {
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
}
