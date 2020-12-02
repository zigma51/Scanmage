package com.trailblazing.scanmage.activity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.trailblazing.scanmage.FileListFragment;
import com.trailblazing.scanmage.PermissionUtil;
import com.trailblazing.scanmage.R;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fabAdd, fabCamera, fabImport;
    Toolbar toolbar;

    private Animation rotateOpenAnim, rotateCloseAnim, fromBottomAnim, toBottomAnim;

    boolean clicked = false;
    private static final int PICK_IMAGE = 100;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new FileListFragment())
                .commit();

        fabAdd = findViewById(R.id.fab_add);
        fabCamera = findViewById(R.id.fab_camera);
        fabImport = findViewById(R.id.fab_import);

        rotateOpenAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_open_anim);
        rotateCloseAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_close_anim);
        fromBottomAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.from_bottom_anim);
        toBottomAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.to_bottom_anim);

        fabAdd.setOnClickListener(v -> onAddButtonClicked());

        fabImport.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), "android.permission.READ_EXTERNAL_STORAGE") != PermissionChecker.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, 0);
            } else {
                openGallery();
            }
        });

        fabCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), "android.permission.CAMERA") != PermissionChecker.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.CAMERA"}, 0);
            } else {
                fabCamera.startAnimation(toBottomAnim);
                fabImport.startAnimation(toBottomAnim);
                fabAdd.startAnimation(rotateCloseAnim);
                Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
                startActivity(intent);
            }
//                Toast.makeText(MainActivity.this, "Camera Button Clicked", Toast.LENGTH_LONG).show();
        });

        if (!PermissionUtil.areAllPermissionsGranted(MainActivity.this)) {
            PermissionUtil.requestAllPermissions(MainActivity.this);
        }
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            imageUri = data.getData();
            File imageFile = new File(getRealPathFromURI(imageUri));
            Uri uri = Uri.fromFile(imageFile);
            Intent intent = new Intent(MainActivity.this, EditImageActivity.class);
            Log.d("TAG", uri.toString());
            intent.putExtra("file_uri", uri);
            intent.putExtra("from_gallery", true);
            startActivity(intent);
            finish();
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        Cursor cursor = getContentResolver()
                .query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }

    private void onAddButtonClicked() {
        setVisibility(clicked);
        setAnimation(clicked);
        clicked = !clicked;
    }

    private void setVisibility(boolean clicked) {
        if (!clicked) {
            fabCamera.setVisibility(View.VISIBLE);
            fabImport.setVisibility(View.VISIBLE);
        } else {
            fabCamera.setVisibility(View.INVISIBLE);
            fabImport.setVisibility(View.INVISIBLE);
        }
    }

    private void setAnimation(boolean clicked) {
        if (!clicked) {
            fabCamera.startAnimation(fromBottomAnim);
            fabImport.startAnimation(fromBottomAnim);
            fabAdd.startAnimation(rotateOpenAnim);
        } else {
            fabCamera.startAnimation(toBottomAnim);
            fabImport.startAnimation(toBottomAnim);
            fabAdd.startAnimation(rotateCloseAnim);
        }
    }
}