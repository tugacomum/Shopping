package com.example.shopping;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ReportResponse extends AppCompatActivity {
    private EditText editTextMessage;
    private LinearLayout imageContainer;
    private EditText editTextResponse;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_response);

        Intent intent = getIntent();
        String cartId = intent.getStringExtra("cartId");
        String userId = intent.getStringExtra("userId");
        String ticketId = intent.getStringExtra("_id");

        editTextMessage = findViewById(R.id.editTextMessageee);
        editTextResponse = findViewById(R.id.textViewReplyMessage);
        imageContainer = findViewById(R.id.imageContainer);

        findViewById(R.id.imageViewBackArrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.buttonResponse).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replyTicket(ticketId, editTextResponse.getText().toString());
            }
        });

        getUserTicket(cartId, userId);
    }
    private void getUserTicket(String cartId, String userId) {
        JSONObject requestBody = new JSONObject();
        try {
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
                        processTicketDetailsResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }
    private void replyTicket(String ticketId, String message) {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("ticketId", ticketId);
            requestBody.put("response", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String apiUrl = "https://shopping-f0qb.onrender.com/api/tickets/responseticket";
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.PATCH,
                apiUrl,
                requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(ReportResponse.this, "Resposta enviada com sucesso", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(ReportResponse.this, "Resposta enviada com sucesso", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ReportResponse.this, AdminActivity.class));
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

            editTextMessage.setEnabled(false);
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
}