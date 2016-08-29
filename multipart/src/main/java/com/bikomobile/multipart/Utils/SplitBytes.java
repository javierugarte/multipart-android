package com.bikomobile.multipart.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2016 Bitban Technologies, S.L.
 * All right reserved.
 */
public class SplitBytes {

    public static List<Bytes> getBytesForPart(byte[] bytes, int sizePart) {

        int count = bytes.length / sizePart;
        if ((bytes.length % sizePart) != 0) {
            count++;
        }

        ArrayList<Bytes> arrayBytes = null;

        for (int post = 1; post <= count; post++) {

            if (arrayBytes == null) {
                arrayBytes = new ArrayList<>(count);
            }

            int size = Math.min(bytes.length, sizePart);

            byte[] bytesSplit = new byte[size];
            System.arraycopy(bytes, 0, bytesSplit, 0, size);

            byte[] aux = new byte[bytes.length - size];
            System.arraycopy(bytes, size, aux, 0, bytes.length - size);

            bytes = aux;

            arrayBytes.add(new Bytes(bytesSplit));
        }

        return arrayBytes;
    }

    public static class Bytes {
        private byte[] bytes;

        public Bytes(byte[] bytes) {
            this.bytes = bytes;
        }

        public byte[] getBytes() {
            return bytes;
        }
    }
}
