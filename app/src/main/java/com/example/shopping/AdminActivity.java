package com.example.shopping;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AdminActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        sharedPreferences = getSharedPreferences("userPreferences", MODE_PRIVATE);
        String userObjectJson = sharedPreferences.getString("userObject", "");

        try {
            JSONObject userObject = new JSONObject(userObjectJson);
            String userName = userObject.optString("name", "N/A");
            TextView textViewWelcomeMessage = findViewById(R.id.textViewWelcomeMessage);
            textViewWelcomeMessage.setText("Bem-vindo de volta, " + "\n" + userName + " 👋");

            loadCarts();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadCarts() {
        String apiUrl = "https://shopping-f0qb.onrender.com/api/tickets/getalltickets";
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                apiUrl,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        processApiResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley Error", "Error during API request", error);
                        error.printStackTrace();
                    }
                });
        requestQueue.add(jsonArrayRequest);
    }

    private void processApiResponse(JSONArray jsonArray) {
        try {
            LinearLayout productsContainer = findViewById(R.id.productsContainer);

            if (jsonArray.length() == 0) {
                findViewById(R.id.textViewNoResultss).setVisibility(View.VISIBLE);
            } else {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject cartObject = jsonArray.getJSONObject(i);

                        if (cartObject.optString("isActive") == "true") {
                            findViewById(R.id.textViewNoResultss).setVisibility(View.GONE);
                            View cardView = getLayoutInflater().inflate(R.layout.card_cart, null);

                            TextView textViewCreatedAt = cardView.findViewById(R.id.textViewCreatedAt);
                            TextView textViewBuyInfo = cardView.findViewById(R.id.textViewBuyInfo);
                            String rawDate = cartObject.optString("createdAt", "N/A");
                            String formattedDate = formatDateString(rawDate);
                            textViewCreatedAt.setText(formattedDate);

                            String buyInfo = "BUY00" + i;
                            textViewBuyInfo.setText(buyInfo);

                            ImageView buttonViewCart = cardView.findViewById(R.id.buttonViewCart);

                            buttonViewCart.setVisibility(View.GONE);

                            cardView.setTag(cartObject.optString("_id", ""));

                            cardView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(AdminActivity.this, ReportResponse.class);
                                    intent.putExtra("cartId", cartObject.optString("cartId", ""));
                                    intent.putExtra("userId", cartObject.optString("userId", ""));
                                    intent.putExtra("productId", cartObject.optString("productId", ""));
                                    intent.putExtra("_id", cartObject.optString("_id", ""));
                                    startActivity(intent);
                                }
                            });

                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );

                            layoutParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.card_margin_bottom);

                            cardView.setLayoutParams(layoutParams);

                            productsContainer.addView(cardView);
                        } else {
                            findViewById(R.id.textViewNoResultss).setVisibility(View.VISIBLE);
                        }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private String formatDateString(String rawDate) {
        if (rawDate != null && rawDate.length() >= 10) {
            String year = rawDate.substring(0, 4);
            String month = rawDate.substring(5, 7);
            String day = rawDate.substring(8, 10);

            return day + "/" + month + "/" + year;
        } else {
            return "Data Inválida";
        }
    }
}