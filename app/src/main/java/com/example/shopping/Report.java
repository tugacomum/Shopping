package com.example.shopping;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Report extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private static final int MAX_IMAGES = 4;

    private LinearLayout imageContainer;
    private List<ImageView> selectedImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        findViewById(R.id.imageViewBackArrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.addimage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedImages.size() < MAX_IMAGES) {
                    openGallery();
                }
            }
        });

        imageContainer = findViewById(R.id.imageContainer);
        selectedImages = new ArrayList<>();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                int targetSize = getResources().getDimensionPixelSize(R.dimen.image_size);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, false);

                ImageView imageView = new ImageView(this);
                imageView.setLayoutParams(new LinearLayout.LayoutParams(targetSize, targetSize));
                imageView.setImageBitmap(resizedBitmap);

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) imageView.getLayoutParams();
                if (imageContainer.getChildCount() >= 1) {
                    layoutParams.setMargins(45, 0, 0, 0);
                } else {
                    layoutParams.setMargins(0, 0, 0, 0);
                }

                imageContainer.addView(imageView);

                imageView.setBackgroundResource(R.drawable.images_rounded);
                selectedImages.add(imageView);

                if (selectedImages.size() == MAX_IMAGES) {
                    findViewById(R.id.addimage).setVisibility(View.GONE);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}