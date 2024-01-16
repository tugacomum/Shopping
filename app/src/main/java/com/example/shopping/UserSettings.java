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

    private static final int REQUEST_PERMISSION_CODE = 123;
    private CircleImageView circleImageView;
    private SharedPreferences sharedPreferences;
    private String imageUrl;
    private String image;
    private String _id;
    private EditText editTextName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        editTextName = findViewById(R.id.editTextUsernameee);
        EditText editTextEmail = findViewById(R.id.editTextEmaill);
        circleImageView = findViewById(R.id.imageViewLogoo);

        sharedPreferences = getSharedPreferences("userPreferences", MODE_PRIVATE);
        String userObjectJson = sharedPreferences.getString("userObject", "");

        try {
            JSONObject userObject = new JSONObject(userObjectJson);
            editTextName.setText(userObject.optString("name", "N/A"));
            editTextEmail.setText(userObject.optString("email", "N/A"));
            _id = userObject.optString("_id", "N/A");
            image = replaceToHttps(userObject.optString("imageUrl", "N/A"));
            Picasso.get().load(image).into(circleImageView);
        } catch (
            JSONException e) {
            e.printStackTrace();
        }

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionAndOpenGallery();
            }
        });

        findViewById(R.id.imageViewBackArrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.buttonChangee).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserProfile(_id, imageUrl);
            }
        });
    }

    private void updateUserProfile(String _id, String imageUrl) {
        String apiUrl = "https://shopping-f0qb.onrender.com/api/users/edituser";
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("_id", _id);
            requestBody.put("name", editTextName.getText().toString());
            requestBody.put("imageUrl", imageUrl);
            RequestQueue requestQueue = Volley.newRequestQueue(this);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.PATCH,
                    apiUrl,
                    requestBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Toast.makeText(UserSettings.this, "Perfil atualizado com sucesso", Toast.LENGTH_SHORT).show();
                        }
                    },
                    new Response.ErrorListener() {
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            Toast.makeText(UserSettings.this, "Erro a atualizar o perfil", Toast.LENGTH_SHORT).show();
                        }
                    });

            requestQueue.add(jsonObjectRequest);

            sharedPreferences = getSharedPreferences("userPreferences", MODE_PRIVATE);
            String userObjectJson = sharedPreferences.getString("userObject", "");

            try {
                JSONObject userObject = new JSONObject(userObjectJson);
                userObject.put("name", editTextName.getText().toString());
                userObject.put("imageUrl", imageUrl);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("userObject", userObject.toString());
                editor.apply();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String replaceToHttps(String image) {
        return image.replace("http://", "https://");
    }

    private void checkPermissionAndOpenGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_PERMISSION_CODE);
        } else {
            openGallery();
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryActivityResultLauncher.launch(galleryIntent);
    }

    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri selectedImageUri = data.getData();
                        if (selectedImageUri != null) {
                            try {
                                Picasso.get().load(selectedImageUri).into(circleImageView);
                                uploadImageToCloudinary(selectedImageUri);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });

    private void uploadImageToCloudinary(Uri imageUri) {
            initCloudinary();
            MediaManager.get().upload(imageUri)
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            imageUrl = resultData.get("url").toString();
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                        }
                    })
                    .dispatch();
    }

    private void initCloudinary() {
        Map config = new HashMap();
        config.put("cloud_name", "dnechwjbm");
        config.put("api_key", "586245818114969");
        config.put("api_secret", "k3N3bRy7-sdbMUcrsgiMgWxdULU");
        MediaManager.init(this, config);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Permission denied! Cannot open the gallery.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
