package com.trailblazing.scanmage.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceOrientedMeteringPointFactory;
import androidx.camera.extensions.HdrImageCaptureExtender;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.trailblazing.scanmage.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};

    PreviewView mPreviewView;
    Button captureImage;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mPreviewView = findViewById(R.id.cameraView);
        captureImage = findViewById(R.id.captureBtn);
        imageView = findViewById(R.id.imagePreview);

        if (allPermissionsGranted()) {
            startCamera(); //start camera if permission has been granted by user
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void startCamera() {

        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {

                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);

                } catch (ExecutionException | InterruptedException e) {
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();

        ImageCapture.Builder builder = new ImageCapture.Builder();

        //Vendor-Extensions (The CameraX extensions dependency in build.gradle)
        HdrImageCaptureExtender hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder);

        // Query if extension is available (optional).
        if (hdrImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
            // Enable the extension if available.
            hdrImageCaptureExtender.enableExtension(cameraSelector);
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        final ImageCapture imageCapture = builder
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .setTargetResolution(new Size(1200, 1600))
                .build();

        preview.setSurfaceProvider(mPreviewView.createSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis, imageCapture);
        setUpTapToFocus(camera.getCameraControl(), height, width);

        captureImage.setOnClickListener(v -> {
            Date date = new Date();
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US);
            String fileName = String.format("%s/%s", getExternalFilesDir("images"), String.format("image_%s.jpg", df.format(date)));
            File file = new File(fileName);

            ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
            imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (file.exists()) {
//                                Toast.makeText(CameraActivity.this, "Inside If", Toast.LENGTH_SHORT).show();
                                Bitmap bitmap = BitmapFactory.decodeFile(fileName);
                                imageView.setImageBitmap(bitmap);
                                imageView.setRotation(90);
                                imageView.setScaleType(ImageView.ScaleType.FIT_XY);

                                imageView.setOnClickListener(v1 -> {
                                    Intent intent = new Intent(CameraActivity.this, EditImageActivity.class);
                                    intent.putExtra("filename", fileName);
                                    startActivity(intent);
                                    finish();
                                });
                            }
                            Toast.makeText(CameraActivity.this, "Image Saved successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onError(@NonNull ImageCaptureException error) {
                    error.printStackTrace();
                }
            });
        });
    }

    private void setUpTapToFocus(CameraControl cameraControl, float height, float width) {
        mPreviewView.setOnTouchListener((v, event) -> {
            if (event.getAction() != MotionEvent.ACTION_UP) {
                return false;
            }
            MeteringPointFactory factory = new SurfaceOrientedMeteringPointFactory(width, height);
            MeteringPoint point = factory.createPoint(event.getX(), event.getY());
            FocusMeteringAction action = new FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF).build();
            cameraControl.startFocusAndMetering(action);
            return true;
        });
    }

    public String getBatchDirectoryName() {

        String app_folder_path = "";
        for (File path : getExternalMediaDirs()) {
            if (path.equals(new File("com.trailblazing.scanmage"))) {
                return (path.toString());
            }
        }
        File dir = new File("com.trailblazing.scanmage/images");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return app_folder_path;
    }

    private boolean allPermissionsGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }
}