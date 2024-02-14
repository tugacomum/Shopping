package com.example.shopping;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserSettings extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private String image;
    private CircleImageView circleImageView;
    private TextView email;
    private TextView editTextName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        sharedPreferences = getSharedPreferences("userPreferences", MODE_PRIVATE);
        String userObjectJson = sharedPreferences.getString("userObject", "");
        editTextName = findViewById(R.id.userNameTextView);
        circleImageView = findViewById(R.id.imageViewLogooo);
        email = findViewById(R.id.emailText);

        try {
            JSONObject userObject = new JSONObject(userObjectJson);
            editTextName.setText(userObject.optString("name", "N/A"));
            email.setText(userObject.optString("email", "N/A"));
            image = replaceToHttps(userObject.optString("imageUrl", "N/A"));
            Picasso.get().load(image).into(circleImageView);
        } catch (
                JSONException e) {
            e.printStackTrace();
        }

        findViewById(R.id.editProfileButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserSettings.this, EditUserSettings.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.imageViewBackArrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.ticketsLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserSettings.this, UserTickets.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.logOutLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("userObject");
                editor.apply();
                Intent intent = new Intent(UserSettings.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
    private String replaceToHttps(String image) {
        return image.replace("http://", "https://");
    }
    public void onProfileImageClick(View view) {
        Intent intent = new Intent(this, FullScreenImageActivity.class);
        intent.putExtra("image_url", image);
        startActivity(intent);
    }
}
