package com.trailblazing.scanmage.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.trailblazing.scanmage.PermissionUtil;
import com.trailblazing.scanmage.R;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fabAdd, fabCamera, fabImport;

    private Animation rotateOpenAnim, rotateCloseAnim, fromBottomAnim, toBottomAnim;

    boolean clicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fabAdd = findViewById(R.id.fab_add);
        fabCamera = findViewById(R.id.fab_camera);
        fabImport = findViewById(R.id.fab_import);

        rotateOpenAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_open_anim);
        rotateCloseAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_close_anim);
        fromBottomAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.from_bottom_anim);
        toBottomAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.to_bottom_anim);

        fabAdd.setOnClickListener(v -> onAddButtonClicked());

        fabCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), "android.permission.CAMERA") != PermissionChecker.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.CAMERA"}, 0);
            } else {
                Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
                startActivity(intent);
            }
//                Toast.makeText(MainActivity.this, "Camera Button Clicked", Toast.LENGTH_LONG).show();
        });

        fabImport.setOnClickListener(v -> Toast.makeText(MainActivity.this, "Import Button Clicked", Toast.LENGTH_LONG).show());

        if (!PermissionUtil.areAllPermissionsGranted(MainActivity.this)) {
            PermissionUtil.requestAllPermissions(MainActivity.this);
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