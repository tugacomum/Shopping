package com.example.shopping;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditUserSettings extends AppCompatActivity {
    private static final int REQUEST_PERMISSION_CODE = 123;
    private CircleImageView circleImageView;
    private TextView editTextEmaill;
    private TextView editTextNamee;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private SharedPreferences sharedPreferences;
    private String imageUrl;
    private static boolean isCloudinaryInitialized = false;
    private String image;
    private String _id;
    private String mCurrentPhotoPath;
    private EditText editTextName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_settings);

        editTextName = findViewById(R.id.editText1);
        circleImageView = findViewById(R.id.imageViewLogoo);
        editTextNamee = findViewById(R.id.userNameTextView);
        editTextEmaill = findViewById(R.id.userEmailText);
        sharedPreferences = getSharedPreferences("userPreferences", MODE_PRIVATE);
        String userObjectJson = sharedPreferences.getString("userObject", "");

        try {
            JSONObject userObject = new JSONObject(userObjectJson);
            editTextName.setText(userObject.optString("name", "N/A"));
            editTextNamee.setText(userObject.optString("name", "N/A"));
            editTextEmaill.setText(userObject.optString("email", "N/A"));
            _id = userObject.optString("_id", "N/A");
            image = replaceToHttps(userObject.optString("imageUrl", "N/A"));
            Picasso.get().load(image).into(circleImageView);
        } catch (
                JSONException e) {
            e.printStackTrace();
        }

        findViewById(R.id.imageViewBackArrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.editProfileButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserProfile(_id, imageUrl);
            }
        });

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetDialog();
            }
        });
    }
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.shopping.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
    private void showBottomSheetDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_options);

        LinearLayout openCameraTextView = bottomSheetDialog.findViewById(R.id.openCameraTextView);
        LinearLayout openGalleryTextView = bottomSheetDialog.findViewById(R.id.openGaleryTextView);

        openCameraTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                openCamera();
            }
        });

        openGalleryTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                checkPermissionAndOpenGallery();
            }
        });

        bottomSheetDialog.show();
    }
    private void updateUserProfile(String _id, String imageUrl) {
        String apiUrl = "https://shopping-f0qb.onrender.com/api/users/edituser";
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("_id", _id);
            if (editTextName.getText().toString().isEmpty()) {
                Toast.makeText(EditUserSettings.this, "Nome não pode estar vazio", Toast.LENGTH_SHORT).show();
                return;
            }
            if (imageUrl != null && !imageUrl.isEmpty()) {
                requestBody.put("imageUrl", imageUrl);
            }
            requestBody.put("name", editTextName.getText().toString());
            RequestQueue requestQueue = Volley.newRequestQueue(this);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.PATCH,
                    apiUrl,
                    requestBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Toast.makeText(EditUserSettings.this, "Perfil atualizado com sucesso", Toast.LENGTH_SHORT).show();
                        }
                    },
                    new Response.ErrorListener() {
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            Toast.makeText(EditUserSettings.this, "Erro a atualizar o perfil", Toast.LENGTH_SHORT).show();
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
    private void checkPermissionAndOpenGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_PERMISSION_CODE);
        } else {
            openGallery();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            File file = new File(mCurrentPhotoPath);
            Uri imageUri = FileProvider.getUriForFile(this, "com.example.shopping.fileprovider", file);
            uploadImageToCloudinary(imageUri);
            imageUrl = imageUri.toString();
            Picasso.get().load(imageUrl).into(circleImageView);
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
    private String replaceToHttps(String image) {
        return image.replace("http://", "https://");
    }
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
        if (!isCloudinaryInitialized) {
            Map config = new HashMap();
            config.put("cloud_name", "dnechwjbm");
            config.put("api_key", "586245818114969");
            config.put("api_secret", "k3N3bRy7-sdbMUcrsgiMgWxdULU");
            try {
                MediaManager.init(this, config);
                isCloudinaryInitialized = true; // Esta linha agora funcionará corretamente
            } catch (IllegalStateException e) {
                // O MediaManager já está inicializado, então podemos ignorar essa exceção
            }
        }
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