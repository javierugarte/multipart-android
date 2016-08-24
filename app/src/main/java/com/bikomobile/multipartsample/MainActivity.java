package com.bikomobile.multipartsample;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bikomobile.multipart.Multipart;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String UPLOAD_URL = "http://devel.onepointzero.org/mpform/post.php";

    private Uri mVideoUri = null;
    private Uri mImageUri = null;

    private static final int SELECT_PHOTO_REQUEST_CODE = 100;
    private static final int SELECT_VIDEO_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText editText = (EditText) findViewById(R.id.et_name);
        View btnSelectImage = findViewById(R.id.btn_select_image);
        if (btnSelectImage != null) {
            btnSelectImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchSelectedImage();
                }
            });
        }

        View btnSelectVideo = findViewById(R.id.btn_select_video);
        if (btnSelectVideo != null) {
            btnSelectVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchSelectedVideo();
                }
            });
        }

        View btnUploadImage = findViewById(R.id.btn_upload);
        if (btnUploadImage != null) {
            btnUploadImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String name = editText.getText().toString().trim();

                    if (name.isEmpty()) {
                        name = "default.png";
                    }

                    if (mImageUri != null) {
                        uploadPhoto(name, mImageUri);
                    } else {
                        uploadVideo(name, mVideoUri);
                    }
                }
            });
        }
    }

    private void launchSelectedImage() {
        PermissionRequest.askForPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE,
                new PermissionRequest.PermissionCallback() {
            @Override
            public void permissionGranted() {
                Intent photoPickerIntent = new Intent();
                photoPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
                photoPickerIntent.setType("image/*");
                startActivityForResult(Intent.createChooser(photoPickerIntent, "Select Picture"), SELECT_PHOTO_REQUEST_CODE);
            }

            @Override
            public void permissionDenied() {
                Toast.makeText(getApplicationContext(), "You should grant permissions first", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void launchSelectedVideo() {
        Intent photoPickerIntent = new Intent();
        photoPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("video/*");
        startActivityForResult(Intent.createChooser(photoPickerIntent, "Select Video"), SELECT_VIDEO_REQUEST_CODE);
    }


    private void uploadPhoto(String name, Uri imageUri) {

        final Context context = getApplicationContext();

        Multipart multipart = new Multipart(context);

        multipart.addParam("title", name);
        multipart.addFile("image/jpeg", "myFile", name, imageUri);

        multipart.launchRequest(UPLOAD_URL, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                Toast.makeText(context, "Success", Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Error", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void uploadVideo(String name, Uri videoUri) {
        final Context context = getApplicationContext();

        final ProgressDialog loading = ProgressDialog.show(this,
                "Uploading...", "Please wait...", false, false);


        Multipart multipart = new Multipart(context);

        HashMap<String, String> params = new HashMap<>();
        params.put("title", name);
        
        multipart.addParams(params);
        multipart.addFile("video/mp4", "myfile", name, videoUri);

        multipart.launchRequest(UPLOAD_URL, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                Toast.makeText(context, "Success", Toast.LENGTH_LONG).show();
                loading.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Error", Toast.LENGTH_LONG).show();
                loading.dismiss();
            }
        });

    }

    private void showImages(List<Uri> images) {
        mVideoUri = null;

        VideoView videoView = (VideoView) findViewById(R.id.vid_selected);
        if (videoView != null) {
            videoView.setVisibility(View.GONE);
        }

        if (images != null && images.size() == 1) {

            Uri image = images.get(0);

            ImageView imageView = (ImageView) findViewById(R.id.img_selected);
            if (imageView != null) {
                mImageUri = image;
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(getBitmap(image));
            }
        }
    }

    private void showVideo(Uri videoUri) {
        this.mImageUri = null;
        ImageView imageView = (ImageView) findViewById(R.id.img_selected);
        if (imageView != null) {
            imageView.setVisibility(View.GONE);
        }

        this.mVideoUri = videoUri;
        if (videoUri != null) {
            VideoView videoView = (VideoView) findViewById(R.id.vid_selected);
            if (videoView != null) {
                videoView.setVisibility(View.VISIBLE);
                videoView.setVideoURI(videoUri);
                videoView.requestFocus();
                videoView.start();
            }
        }
    }

    /**
     * Convert the uri image to Bitmap.
     * This method requires permission READ_EXTERNAL_STORAGE
     *
     * @param uri uri from image
     * @return bitmap with the image
     */
    private Bitmap getBitmap(Uri uri) {
        InputStream imageStream = null;

        try {
            imageStream = getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return BitmapFactory.decodeStream(imageStream);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionRequest.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {
            case SELECT_PHOTO_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    ArrayList<Uri> images = new ArrayList<>();
                    if (intent.getData() != null) { // Single image
                        images.add(intent.getData());
                    } else { // Multiple images
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            for (int i = 0; i < intent.getClipData().getItemCount(); i++) {
                                images.add(intent.getClipData().getItemAt(i).getUri());
                            }
                        }
                    }

                    showImages(images);
                }
                break;
            case SELECT_VIDEO_REQUEST_CODE:
                Uri selectedVideoPath = intent.getData();
                showVideo(selectedVideoPath);
                break;
        }
    }
}