package com.trailblazing.scanmage.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.trailblazing.scanmage.R;
import com.trailblazing.scanmage.database.AppDatabase;
import com.trailblazing.scanmage.model.ScannedFile;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class EditImageActivity extends AppCompatActivity {
    ImageView CropButtonImageView;
    ImageView editImageView;
    EditText pdfFileNameEditText;
    Button savePdfBtn;
    Button closeEditing;
    Uri uri;
    File pdfFile;
    Uri fileUri;
    String fileName;
    String croppedFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_image);
        CropButtonImageView = findViewById(R.id.crop_btn);
        editImageView = findViewById(R.id.edit_image_view);
        pdfFileNameEditText = findViewById(R.id.file_name);
        savePdfBtn = findViewById(R.id.save_pdf);
        closeEditing = findViewById(R.id.close_editing);
        Intent intent = getIntent();

        closeEditing.setOnClickListener(v -> {
            Intent i = new Intent(EditImageActivity.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });

        fileUri = intent.getParcelableExtra("file_uri");
        assert fileUri != null;
        File file = new File(Objects.requireNonNull(fileUri.getPath()));
        fileName = file.getPath();
        Glide.with(EditImageActivity.this)
                .load(Uri.fromFile(file))
                .into(editImageView);
        CropButtonImageView.setOnClickListener(v -> startCrop(Uri.fromFile(file)));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE
                && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                uri = imageUri;
            } else {
                startCrop(imageUri);
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            Date date = new Date();
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US);
            if (resultCode == RESULT_OK) {
                Glide.with(EditImageActivity.this)
                        .load(result.getUri())
                        .into(editImageView);

                Glide.with(EditImageActivity.this).asBitmap().load(result.getUri()).into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        FileOutputStream outStream;
                        croppedFileName = String.format("%s/%s", getExternalFilesDir("images")
                                , String.format("%s_cropped_%s.jpg", fileName.substring(fileName.lastIndexOf("/"), fileName.lastIndexOf("."))
                                        , df.format(date)));
                        File croppedFile = new File(croppedFileName);
                        try {
                            outStream = new FileOutputStream(croppedFile);
                            resource.compress(Bitmap.CompressFormat.JPEG, 75, outStream);
                            outStream.flush();
                            outStream.close();
                            Toast.makeText(EditImageActivity.this, "Cropped Successfully!", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });

                savePdfBtn.setOnClickListener(v -> {
                    String pdfName = pdfFileNameEditText.getText().toString();
                    if (pdfName.isEmpty() || pdfName.length() <= 5) {
                        pdfFileNameEditText.setError("Enter valid name!");
                        pdfFileNameEditText.requestFocus();
                        return;
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
                    pdfName = String.format("%s/%s", getExternalFilesDir("documents"), String.format("%s_%s.pdf", pdfName, df.format(date)));
                    pdfFile = new File(pdfName);
                    Document document = new Document();
                    try {
                        PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
                        document.open();
                        Image image = Image.getInstance(croppedFileName);
                        image.setRotation(90);
                        document.setPageSize(new Rectangle(image.getWidth(), image.getHeight()));
                        document.newPage();
                        image.setAbsolutePosition(0, 0);
                        document.add(image);
                        document.close();
                        ScannedFile pdf = new ScannedFile();
                        pdf.date = sdf.format(date);
                        pdf.filePath = pdfName;
                        AppDatabase.getInstance(EditImageActivity.this).filesDao().insert(pdf);

                        Toast.makeText(this, "PDF saved Successfully!", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(EditImageActivity.this, MainActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    private void startCrop(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .start(this);
    }
}