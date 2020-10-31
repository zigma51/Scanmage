package com.trailblazing.scanmage.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

public class EditImageActivity extends AppCompatActivity {
    ImageView imageView;
    ImageView editImageView;
    EditText pdfFileNameEditText;
    Button savePdfBtn;
    Button closeEditing;
    Uri uri;
    File pdfFile;
    String fileName;
    String croppedFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_image);
        imageView = findViewById(R.id.crop_btn);
        editImageView = findViewById(R.id.edit_image_view);
        pdfFileNameEditText = findViewById(R.id.file_name);
        savePdfBtn = findViewById(R.id.save_pdf);
        closeEditing = findViewById(R.id.close_editing);

        closeEditing.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        fileName = intent.getStringExtra("filename");
        assert fileName != null;
        File file = new File(fileName);
        editImageView.setImageURI(Uri.fromFile(file));
        imageView.setOnClickListener(v -> startCrop(Uri.fromFile(file)));

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
            if (resultCode == RESULT_OK) {
                editImageView.setImageURI(result.getUri());
                croppedFileName = fileName.substring(0, fileName.length() - 4) + "cropped.jpg";
                File croppedFile = new File(croppedFileName);
                FileOutputStream outStream;
                BitmapDrawable draw = (BitmapDrawable) editImageView.getDrawable();
                Bitmap bitmap = draw.getBitmap();
                try {
                    outStream = new FileOutputStream(croppedFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                    outStream.flush();
                    outStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Toast.makeText(this, "Cropped Successfully!", Toast.LENGTH_SHORT).show();
                savePdfBtn.setOnClickListener(v -> {
                    String pdfName = pdfFileNameEditText.getText().toString();
                    if (pdfName.isEmpty() || pdfName.length() <= 5) {
                        pdfFileNameEditText.setError("Enter valid name!");
                        pdfFileNameEditText.requestFocus();
                        return;
                    }
                    Date date = new Date();
                    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
                    pdfName = String.format("%s/%s", getExternalFilesDir("documents"), String.format("%s_%s.pdf", pdfName, df.format(date)));
                    pdfFile = new File(pdfName);
                    Document document = new Document();
                    try {
                        PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
                        document.open();
                        Image image = Image.getInstance(croppedFileName);
//                        float scalar = ((document.getPageSize().getWidth() - document.leftMargin()
//                                - document.rightMargin() - 0) / image.getWidth()) * 100; // 0 means you have no indentation. If you have any, change it.
//                        image.scalePercent(scalar);
//                        image.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_TOP);
                        document.setPageSize(new Rectangle(image.getScaledWidth(), image.getScaledHeight()));
                        document.newPage();
                        image.setAbsolutePosition(0, 0);
                        document.add(image);
                        document.close();
                        ScannedFile pdf = new ScannedFile();
                        pdf.date = sdf.format(date);
                        pdf.filePath = pdfName;
                        AppDatabase.getInstance(EditImageActivity.this).filesDao().insert(pdf);

                        Toast.makeText(this, "PDF saved Successfully!", Toast.LENGTH_SHORT).show();
                        finish();
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