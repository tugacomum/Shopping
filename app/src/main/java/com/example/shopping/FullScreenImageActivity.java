package com.example.shopping;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.squareup.picasso.Picasso;

public class FullScreenImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);
        Intent intent = getIntent();
        String imageUrl = intent.getStringExtra("image_url");
        ImageView fullScreenImageView = findViewById(R.id.fullScreenImageView);
        Picasso.get().load(imageUrl).into(fullScreenImageView);
    }
}
