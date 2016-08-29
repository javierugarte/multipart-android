package com.bikomobile.multipart;

public class EntryMultipart {

    private String contentType;
    private String filename;
    private String postName;
    private byte[] data;

    public EntryMultipart(String contentType, String postName, String filename, byte[] data) {
        this.contentType = contentType;
        this.filename = filename;
        this.postName = postName;
        this.data = data;
    }

    public EntryMultipart(String postName, byte[] data) {
        this.contentType = null;
        this.filename = null;
        this.postName = postName;
        this.data = data;
    }

    public EntryMultipart(String contentType, String postName, byte[] data) {
        this.contentType = contentType;
        this.filename = null;
        this.postName = postName;
        this.data = data;
    }

    public String getContentType() {
        return contentType;
    }

    public String getFilename() {
        return filename;
    }

    public String getPostName() {
        return postName;
    }

    public byte[] getData() {
        return data;
    }

    public boolean isTextPlain() {
        return contentType.equalsIgnoreCase("text/plain");
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
