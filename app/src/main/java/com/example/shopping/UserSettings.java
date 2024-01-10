package com.example.shopping;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.Manifest;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserSettings extends AppCompatActivity {
    private static final String TAG = "Upload image Activity";
    private static int IMAGE_REQ = 1;
    private Uri imagePath;
    private String imageUrl;
    private CircleImageView imageView;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        imageView = findViewById(R.id.imageViewLogoo);
        button = findViewById(R.id.buttonChange);

        initConfig();

        SharedPreferences sharedPreferences = getSharedPreferences("userPreferences", MODE_PRIVATE);
        String userObjectJson = sharedPreferences.getString("userObject", "");
        if (!userObjectJson.isEmpty()) {
            try {
                JSONObject userObject = new JSONObject(userObjectJson);
                EditText editTextUsername = findViewById(R.id.editTextUsernamee);
                editTextUsername.setText(userObject.optString("name", ""));
                EditText editTextEmail = findViewById(R.id.editTextEmail);
                editTextEmail.setText(userObject.optString("email", ""));
                CircleImageView imageView = findViewById(R.id.imageViewLogoo);
                String image = userObject.optString("imageUrl", "");
                if (!image.isEmpty()) {
                    Picasso.get().load(image).into(imageView);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        imageView.setOnClickListener(v -> {
            requestPermission();
        });

        button.setOnClickListener(v -> {
            MediaManager.get().upload(imagePath).callback(new UploadCallback() {
                @Override
                public void onStart(String requestId) {
                    Log.d(TAG, "onStart: " + "started");
                }

                @Override
                public void onProgress(String requestId, long bytes, long totalBytes) {
                    Log.d(TAG, "onStart: " + "uploading");
                }

                @Override
                public void onSuccess(String requestId, Map resultData) {
                    Log.d(TAG, "onStart: " + "success");
                    String url = resultData.get("url").toString();
                    imageUrl = url;
                }

                @Override
                public void onError(String requestId, ErrorInfo error) {
                    Log.d(TAG, "onStart: " + error);
                }

                @Override
                public void onReschedule(String requestId, ErrorInfo error) {
                    Log.d(TAG, "onStart: " + error);
                }
            }).dispatch();
        });

        findViewById(R.id.imageViewBackArrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
    private void initConfig() {
        Map config = new HashMap();
        config.put("cloud_name", "dnechwjbm");
        config.put("api_key", "586245818114969");
        config.put("api_secret", "k3N3bRy7-sdbMUcrsgiMgWxdULU");
        MediaManager.init(this, config);
    }
    private void requestPermission() {
        Log.d("Permission", "Requesting permission...");
        if(ContextCompat.checkSelfPermission(UserSettings.this, Manifest.permission.READ_MEDIA_IMAGES)
                == PackageManager.PERMISSION_GRANTED)
        {
            selectImage();
        }else
        {
            ActivityCompat.requestPermissions(UserSettings.this, new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES
            }, IMAGE_REQ);
        }
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        someActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        imagePath = data.getData();
                        Picasso.get().load(imagePath).into(imageView);
                    }
                }
            }
    );

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
