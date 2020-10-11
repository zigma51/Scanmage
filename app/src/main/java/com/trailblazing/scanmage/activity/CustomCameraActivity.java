package com.trailblazing.scanmage.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.*;
import android.hardware.Camera.*;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.FrameLayout;

import com.trailblazing.scanmage.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.widget.Toast;

//public class CustomCameraActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_custom_camera);
//    }
//}

public class CustomCameraActivity extends AppCompatActivity {
    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private boolean flashmode = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_camera);
        mCamera = getCameraInstance();
        mCameraPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mCameraPreview);

        Button captureButton = (Button) findViewById(R.id.button_capture);
        Button flashButton = (Button) findViewById(R.id.button_flash);

        // Continuous Auto focus
        Camera.Parameters param = mCamera.getParameters();
        List<String> focusModes = param.getSupportedFocusModes();
        if (focusModes.contains(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            param.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        mCamera.setParameters(param);

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.takePicture(null, null, mPicture);
            }
        });
        flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Camera.Parameters param = mCamera.getParameters();
                try {

                    param.setFlashMode(!flashmode ? Parameters.FLASH_MODE_TORCH
                            : Parameters.FLASH_MODE_OFF);
                    mCamera.setParameters(param);
                    flashmode = !flashmode;
                } catch (Exception e) {
                    // TODO: handle exception
                }

            }
        });
    }

    /**
     * Helper method to access the camera returns null if it cannot get the
     * camera or does not exist
     *
     * @return
     */
    private Camera getCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open();

        } catch (Exception e) {
            // cannot get camera or does not exist
        }
        return camera;
    }


    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Toast.makeText(CustomCameraActivity.this, "File not found", Toast.LENGTH_LONG).show();

            } catch (IOException e) {
                Toast.makeText(CustomCameraActivity.this, "IO not found", Toast.LENGTH_LONG).show();
            }
        }
    };

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Scanmage");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("Scanmage", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }
}

