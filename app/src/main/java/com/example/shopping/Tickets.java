package com.example.shopping;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

public class Tickets extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private JSONArray originalCarts = new JSONArray();
    private JSONArray filteredCarts = new JSONArray();
    private String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tickets);

        sharedPreferences = getSharedPreferences("userPreferences", MODE_PRIVATE);
        String userObjectJson = sharedPreferences.getString("userObject", "");
        String storedCartId = sharedPreferences.getString("cartId", "");

        findViewById(R.id.imageViewBackArrow1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        try {
            JSONObject userObject = new JSONObject(userObjectJson);
            userId = userObject.optString("_id", "");
            loadCartsFromApi();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void loadCartsFromApi() {
        Log.d("API Request", "Attempting to load carts from API");
        String apiUrl = "https://shopping-f0qb.onrender.com/api/carts/getallcarts/" + userId;
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                apiUrl,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        originalCarts = response;
                        processApiResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley Error", "Error during API request", error);
                        error.printStackTrace();
                    }
                }
        );

        requestQueue.add(jsonArrayRequest);
    }
    private void processApiResponse(JSONArray jsonArray) {
        try {
            LinearLayout productsContainer = findViewById(R.id.productsContainer);
            productsContainer.removeAllViews();

            if (jsonArray.length() == 0) {
                findViewById(R.id.textViewNoResults1).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.textViewNoResults1).setVisibility(View.GONE);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject cartObject = jsonArray.getJSONObject(i);

                    boolean isPaid = cartObject.optBoolean("isPaid", false);

                    if (isPaid) {
                        View cardView = getLayoutInflater().inflate(R.layout.card_cart, null);

                        TextView textViewCreatedAt = cardView.findViewById(R.id.textViewCreatedAt);
                        TextView textViewBuyInfo = cardView.findViewById(R.id.textViewBuyInfo);
                        String rawDate = cartObject.optString("createdAt", "N/A");
                        String formattedDate = formatDateString(rawDate);
                        textViewCreatedAt.setText(formattedDate);

                        String buyInfo = "BUY00" + i;
                        textViewBuyInfo.setText(buyInfo);

                        double totalCartPrice = calculateTotalCartPrice(cartObject);

                        TextView textViewTotalPrice = cardView.findViewById(R.id.textViewTotalPrice);
                        textViewTotalPrice.setText(String.format("%.2f€", totalCartPrice));

                        ImageView buttonViewCart = cardView.findViewById(R.id.buttonViewCart);

                        cardView.setTag(cartObject.optString("_id", ""));

                        buttonViewCart.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Tickets.this, Invoice.class);
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
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private double calculateTotalCartPrice(JSONObject cartObject) {
        double totalCartPrice = 0;

        try {
            JSONArray productsArray = cartObject.getJSONArray("products");

            for (int j = 0; j < productsArray.length(); j++) {
                JSONObject productObject = productsArray.getJSONObject(j);

                int quantity = productObject.optInt("quantity", 0);
                double price = productObject.optDouble("price", 0);

                totalCartPrice += quantity * price;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return totalCartPrice;
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