package com.trailblazing.scanmage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button cam = findViewById(R.id.btnCamScreen);
        cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(intent);

            }
        });
        if (!PermissionUtil.areAllPermissionsGranted(MainActivity.this)) {
            PermissionUtil.requestAllPermissions(MainActivity.this);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtil.PERMISSION_REQUEST_CODE) {
            if (!PermissionUtil.areAllPermissionsGranted(MainActivity.this)) {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                PermissionUtil.showPermissionsRationale(MainActivity.this);
            }
        }
    }
}