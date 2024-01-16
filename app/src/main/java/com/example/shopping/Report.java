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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
import java.io.FileNotFoundException;
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
    private EditText editTextMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        String cartId = getIntent().getStringExtra("_id");

        editTextMessage = findViewById(R.id.editTextMessage);

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

        Button buttonReport = findViewById(R.id.buttonReport);
        buttonReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendReportToServer(cartId);
                findViewById(R.id.addimage).setVisibility(View.GONE);
                findViewById(R.id.buttonReport).setVisibility(View.GONE);
            }
        });

        getUserTicket(cartId);
    }

    private void getUserTicket(String cartId) {
        SharedPreferences sharedPreferences = getSharedPreferences("userPreferences", MODE_PRIVATE);
        String userObjectJson = sharedPreferences.getString("userObject", "");

        JSONObject requestBody = new JSONObject();
        try {
            JSONObject userObject = new JSONObject(userObjectJson);
            String userId = userObject.optString("_id", "");
            requestBody.put("userId", userId);
            requestBody.put("cartId", cartId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String apiUrl = "https://shopping-f0qb.onrender.com/api/tickets/getuserticket";
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                apiUrl,
                requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response.length() > 0) {
                            processTicketDetailsResponse(response);
                        } else return;
                    }
                },
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    private void processTicketDetailsResponse(JSONObject response) {
            String description = response.optString("description", "");
            editTextMessage.setText(description);

            JSONArray imageUrlsArray = response.optJSONArray("imageUrls");
            if (imageUrlsArray != null) {
                loadAndDisplayImages(imageUrlsArray);

                findViewById(R.id.addimage).setVisibility(View.GONE);
                editTextMessage.setEnabled(false);

                Button button = findViewById(R.id.buttonReport);
                button.setText("Ver resposta");

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!response.optString("response", "").isEmpty() && response.optString("response", "") != "null") {
                            showAlert("Resposta suporte", response.optString("response", ""));
                        } else {
                            showAlert("Resposta suporte", "Ainda não há resposta do suporte.");
                        }
                    }
                });

                findViewById(R.id.textViewNoImage).setVisibility(View.GONE);
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

        int targetSize = getResources().getDimensionPixelSize(R.dimen.image_size);

        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(targetSize, targetSize));

        Picasso.get().load(httpsImageUrl).into(imageView);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) imageView.getLayoutParams();
        if (imageContainer.getChildCount() >= 1) {
            layoutParams.setMargins(45, 0, 0, 0);
        } else {
            layoutParams.setMargins(0, 0, 0, 0);
        }

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
        Map config = new HashMap();
        config.put("cloud_name", "dnechwjbm");
        config.put("api_key", "586245818114969");
        config.put("api_secret", "k3N3bRy7-sdbMUcrsgiMgWxdULU");
        MediaManager.init(this, config);
    }

    private void uploadImagesToCloudinary(final CloudinaryUploadCallback callback) {
        final JSONArray imageUrlsArray = new JSONArray();
        final int[] imagesUploaded = {0};

        for (ImageView imageView : selectedImages) {
            Uri imageUri = getImageUri(imageView);

            MediaManager.get().upload(imageUri).callback(new UploadCallback() {
                @Override
                public void onStart(String requestId) {
                }

                @Override
                public void onProgress(String requestId, long bytes, long totalBytes) {
                }

                @Override
                public void onSuccess(String requestId, Map resultData) {
                    String imageUrl = resultData.get("url").toString();
                    try {
                        JSONObject imageUrlObject = new JSONObject();
                        imageUrlObject.put("image", imageUrl);
                        imageUrlsArray.put(imageUrlObject);

                        imagesUploaded[0]++;
                        if (imagesUploaded[0] == selectedImages.size()) {
                            callback.onSuccess(imageUrlsArray);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(String requestId, ErrorInfo error) {
                    callback.onError(error);
                }

                @Override
                public void onReschedule(String requestId, ErrorInfo error) {
                    callback.onError(error);
                }
            }).dispatch();
        }
    }


    public interface CloudinaryUploadCallback {
        void onSuccess(JSONArray imageUrls);
        void onError(ErrorInfo error);
    }

    private Uri getImageUri(ImageView imageView) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    private void sendReportToServer(String cartId) {
        SharedPreferences sharedPreferences = getSharedPreferences("userPreferences", MODE_PRIVATE);
        String userObjectJson = sharedPreferences.getString("userObject", "");

        initCloudinary();

        JSONObject requestBody = new JSONObject();
        try {
            JSONObject userObject = new JSONObject(userObjectJson);
            String userId = userObject.optString("_id", "");
            requestBody.put("userId", userId);
            requestBody.put("description", editTextMessage.getText().toString());
            requestBody.put("cartId", cartId);

            uploadImagesToCloudinary(new CloudinaryUploadCallback() {
                @Override
                public void onSuccess(JSONArray imageUrls) {
                    try {
                        requestBody.put("imageUrls", imageUrls);
                        sendReportToServerWithImages(requestBody);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(ErrorInfo error) {
                    showAlert("Ticket", "Obrigado por reportar o problema. Entraremos em contato em breve.");
                }
            });
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
                        showAlert("Ticket", "Obrigado por reportar o problema. Entraremos em contato em breve.");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showAlert("Ticket", "Obrigado por reportar o problema. Entraremos em contato em breve.");
                        error.printStackTrace();
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    private void showAlert(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Report.this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                editTextMessage.setEnabled(false);
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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

                findViewById(R.id.textViewNoImage).setVisibility(View.GONE);

                if (selectedImages.size() == MAX_IMAGES) {
                    findViewById(R.id.addimage).setVisibility(View.GONE);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
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