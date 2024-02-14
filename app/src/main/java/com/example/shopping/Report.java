package com.example.shopping;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.preprocess.BitmapEncoder;
import com.cloudinary.android.preprocess.ImagePreprocessChain;
import com.cloudinary.android.preprocess.Rotate;
import com.cloudinary.android.preprocess.VideoPreprocessChain;
import com.squareup.picasso.Picasso;

public class Report extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private static final int MAX_IMAGES = 4;

    private LinearLayout imageContainer;
    private List<ImageView> selectedImages;
    private static boolean isCloudinaryInitialized = false;
    private EditText editTextMessage;

    private JSONArray imageUrlsArray = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        selectedImages = new ArrayList<>();
        imageContainer = findViewById(R.id.imageContainer);

        updateAddPhotoTextVisibility();

        final TextView textViewCharacterCount = findViewById(R.id.textViewCharacterCount);

        String cartId = getIntent().getStringExtra("cartId");
        String productId = getIntent().getStringExtra("productId");

        editTextMessage = findViewById(R.id.editTextMessage);
        editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                int remainingCharacters = 200 - editable.length();
                textViewCharacterCount.setText(remainingCharacters + " / 200");
            }
        });

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

        Button buttonReport = findViewById(R.id.buttonReportt);
        buttonReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendReportToServer(cartId, productId);
                findViewById(R.id.addimage).setVisibility(View.GONE);
            }
        });

        getUserTicket(cartId, productId);
    }

    private void getUserTicket(String cartId, String productId) {
        SharedPreferences sharedPreferences = getSharedPreferences("userPreferences", MODE_PRIVATE);
        String userObjectJson = sharedPreferences.getString("userObject", "");

        try {
            JSONObject userObject = new JSONObject(userObjectJson);
            String userId = userObject.optString("_id", "");

            if (!userId.isEmpty()) {
                JSONObject requestBody = new JSONObject();
                requestBody.put("userId", userId);
                requestBody.put("cartId", cartId);
                requestBody.put("productId", productId);

                RequestQueue requestQueue = Volley.newRequestQueue(this);
                String apiUrl = "https://shopping-f0qb.onrender.com/api/tickets/getuserticket";

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        Request.Method.POST,
                        apiUrl,
                        requestBody,
                        response -> {
                            try {
                                if (response.has("description")) {
                                    editTextMessage.setText(response.getString("description"));
                                }
                                if (response.has("imageUrls") && response.getJSONArray("imageUrls").length() > 0) {
                                    JSONArray imageUrlsArray = response.getJSONArray("imageUrls");
                                    loadAndDisplayImages(imageUrlsArray);
                                    findViewById(R.id.addimage).setVisibility(View.GONE);
                                    findViewById(R.id.textViewNoImage).setVisibility(View.GONE);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        },
                        error -> {
                            Log.e("getUserTicket", "Error: " + error.toString());
                        });

                requestQueue.add(jsonObjectRequest);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadAndDisplayImages(JSONArray imageUrlsArray) {
        try {
            for (int i = 0; i < imageUrlsArray.length(); i++) {
                JSONObject imageUrlObject = imageUrlsArray.getJSONObject(i);
                String imageUrl = imageUrlObject.optString("image", "");

                loadAndDisplayImage(imageUrl);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadAndDisplayImage(String imageUrl) {
        String httpsImageUrl = updateToHttps(imageUrl);

        ImageView imageView = new ImageView(this);
        int imageSize = getResources().getDimensionPixelSize(R.dimen.image_size);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(imageSize, imageSize);
        layoutParams.setMargins(0, 0, getResources().getDimensionPixelSize(R.dimen.image_margin), 0);

        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Picasso.get().load(httpsImageUrl).into(imageView);

        imageContainer.addView(imageView);
    }


    private String updateToHttps(String imageUrl) {
        return imageUrl.replace("http://", "https://");
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    private void initCloudinary() {
        if (!isCloudinaryInitialized) {
            Map config = new HashMap();
            config.put("cloud_name", "dnechwjbm");
            config.put("api_key", "586245818114969");
            config.put("api_secret", "k3N3bRy7-sdbMUcrsgiMgWxdULU");
            MediaManager.init(this, config);
            isCloudinaryInitialized = true;
        }
    }


    private void uploadImageToCloudinary(Uri imageUri) {
        initCloudinary();
        MediaManager.get().upload(imageUri).option("folder", "app_images")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = resultData.get("url").toString();
                        Log.d("Upload Success", imageUrl);
                        try {
                            JSONObject imageUrlObject = new JSONObject();
                            imageUrlObject.put("image", imageUrl);
                            imageUrlsArray.put(imageUrlObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e("Upload Error", error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch();
    }


    public interface CloudinaryUploadCallback {
        void onSuccess(JSONArray imageUrls);
        void onError(ErrorInfo error);
    }

    private Uri getImageUri(ImageView imageView) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();
        File imageFile = saveImageLocally(bitmap, "image");
        if (imageFile != null) {
            return FileProvider.getUriForFile(Report.this, getApplicationContext().getPackageName() + ".provider", imageFile);
        }
        return null;
    }


    private File saveImageLocally(Bitmap bitmap, String imageName) {
        File imagesFolder = new File(getFilesDir(), "images");
        if (!imagesFolder.exists()) {
            imagesFolder.mkdirs();
        }
        File imageFile = new File(imagesFolder, imageName + ".png");
        try (FileOutputStream out = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            return imageFile;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void sendReportToServer(String cartId, String productId) {
        SharedPreferences sharedPreferences = getSharedPreferences("userPreferences", MODE_PRIVATE);
        String userObjectJson = sharedPreferences.getString("userObject", "");

        JSONObject requestBody = new JSONObject();
        try {
            JSONObject userObject = new JSONObject(userObjectJson);
            String userId = userObject.optString("_id", "");
            requestBody.put("userId", userId);
            requestBody.put("description", editTextMessage.getText().toString());
            requestBody.put("cartId", cartId);
            requestBody.put("productId", productId);
            requestBody.put("imageUrls", imageUrlsArray);

            sendReportToServerWithImages(requestBody);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void sendReportToServerWithImages(JSONObject requestBody) {
        String apiUrl = "https://shopping-f0qb.onrender.com/api/tickets/createticket";
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                apiUrl,
                requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        startActivity(new Intent(Report.this, HomeActivity.class));
                        Toast.makeText(Report.this, "Obrigado por reportar o problema.", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        startActivity(new Intent(Report.this, HomeActivity.class));
                        Toast.makeText(Report.this, "Obrigado por reportar o problema.", Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            if (imageUri != null) {
                uploadImageToCloudinary(imageUri); // Faz o upload da imagem para o Cloudinary

                // Cria e configura a ImageView
                ImageView imageView = new ImageView(this);
                int imageSize = getResources().getDimensionPixelSize(R.dimen.image_size); // Supondo que você tenha essa dimensão definida
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(imageSize, imageSize);
                int margin = getResources().getDimensionPixelSize(R.dimen.image_margin); // Supondo que você tenha essa margem definida
                layoutParams.setMargins(0, 0, margin, 0); // Define a margem direita
                imageView.setLayoutParams(layoutParams);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                // Usa Picasso para carregar a imagem selecionada na ImageView
                Picasso.get().load(imageUri).into(imageView);

                // Adiciona a ImageView ao container na UI
                imageContainer.addView(imageView);
                selectedImages.add(imageView); // Adiciona a ImageView à lista de imagens selecionadas

                // Verifica se o limite de imagens foi alcançado
                if (selectedImages.size() >= MAX_IMAGES) {
                    findViewById(R.id.addimage).setVisibility(View.GONE); // Esconde o botão para adicionar mais imagens
                }

                // Atualiza a visibilidade do texto de orientação para adicionar imagens
                updateAddPhotoTextVisibility();
            }
        }
    }


    private void updateAddPhotoTextVisibility() {
        TextView textViewNoImage = findViewById(R.id.textViewNoImage);
        if (selectedImages != null && !selectedImages.isEmpty()) {
            textViewNoImage.setVisibility(View.GONE);
        } else {
            textViewNoImage.setVisibility(View.VISIBLE);
        }
    }

    private JSONArray getImageUrls() throws JSONException {
        JSONArray imageUrlsArray = new JSONArray();

        for (ImageView imageView : selectedImages) {
            byte[] imageData = getImageBytes(imageView);

            String base64Image = convertToBase64(imageData);

            JSONObject imageUrlObject = new JSONObject();
            imageUrlObject.put("image", base64Image);
            imageUrlsArray.put(imageUrlObject);
        }

        return imageUrlsArray;
    }

    private byte[] getImageBytes(ImageView imageView) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    private String convertToBase64(byte[] imageData) {
        return android.util.Base64.encodeToString(imageData, android.util.Base64.DEFAULT);
    }
}