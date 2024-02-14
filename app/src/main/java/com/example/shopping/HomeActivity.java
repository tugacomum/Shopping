package com.example.shopping;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HomeActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private String userId;
    private ProgressBar progressBar;
    Button iniciarCompraButton;
    private JSONArray originalCarts = new JSONArray();
    private JSONArray filteredCarts = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        sharedPreferences = getSharedPreferences("userPreferences", MODE_PRIVATE);

        EditText editTextSearchProduct = findViewById(R.id.editTextSearchProduct);
        Button buttonSearch = findViewById(R.id.buttonSearch);

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchText = editTextSearchProduct.getText().toString().trim();
                if (!TextUtils.isEmpty(searchText)) {
                    searchProducts(searchText);
                } else {
                    loadCartsFromApi();
                }
            }
        });

        findViewById(R.id.textViewTickets).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, Tickets.class);
                startActivity(intent);
            }
        });

        String userObjectJson = sharedPreferences.getString("userObject", "");
        String storedCartId = sharedPreferences.getString("cartId", "");

        if (storedCartId != null && !storedCartId.isEmpty()) {
            showAlertDialog(storedCartId);
        } else {

        }

        progressBar = findViewById(R.id.progressBar);
        iniciarCompraButton = findViewById(R.id.buttonStartPurchase);

        if (storedCartId != null && !storedCartId.isEmpty()) {
            iniciarCompraButton.setText("Continuar Compra");
            iniciarCompraButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(HomeActivity.this, CartActivity.class);
                    intent.putExtra("_id", storedCartId);
                    startActivity(intent);
                }
            });
        } else {
            iniciarCompraButton.setText("Iniciar Compra");
            iniciarCompraButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    createCart(userId);
                }
            });
        }

        ImageView settingsImageView = findViewById(R.id.settings);
        settingsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, UserSettings.class));
            }
        });

        try {
            progressBar.setVisibility(View.VISIBLE);
            JSONObject userObject = new JSONObject(userObjectJson);
            userId = userObject.optString("_id", "");
            String userName = userObject.optString("name", "N/A");
            TextView textViewWelcomeMessage = findViewById(R.id.textViewWelcomeMessage);
            textViewWelcomeMessage.setText("Bem-vindo de volta, " + "\n" + userName + " 👋");

            loadCartsFromApi();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void searchProducts(String searchTerm) {
        filteredCarts = new JSONArray();

        try {
            LinearLayout productsContainer = findViewById(R.id.productsContainer);
            productsContainer.removeAllViews();

            for (int i = 0; i < originalCarts.length(); i++) {
                JSONObject cartObject = originalCarts.getJSONObject(i);
                JSONArray productsArray = cartObject.getJSONArray("products");

                for (int j = 0; j < productsArray.length(); j++) {
                    JSONObject productObject = productsArray.getJSONObject(j);
                    String productName = productObject.optString("productName", "");

                    if (productName.toLowerCase().contains(searchTerm.toLowerCase())) {
                        filteredCarts.put(cartObject);
                        break;
                    }
                }
            }

            processApiResponse(filteredCarts);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }




    private void showAlertDialog(String storedCartId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Carrinho existente");
        builder.setMessage("Tem um carrinho existente. Deseja continuar a compra?");
        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(HomeActivity.this, CartActivity.class);
                intent.putExtra("_id", storedCartId);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                handleNoButtonClick(storedCartId);
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void handleNoButtonClick(String storedCartId) {
        removeUserCart(storedCartId);
        iniciarCompraButton.setText("Iniciar Compra");

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("cartId");
        editor.apply();

        Toast.makeText(HomeActivity.this, "Carrinho removido com sucesso", Toast.LENGTH_SHORT).show();
    }

    private void removeUserCart(String storedCartId) {
        String apiUrl = "https://shopping-f0qb.onrender.com/api/carts/deletecart";

        Log.d("storedCartId", storedCartId);

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("_id", storedCartId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                apiUrl,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("CartDeletionResponse", response.toString());
                        Toast.makeText(HomeActivity.this, "Carrinho removido com sucesso", Toast.LENGTH_SHORT).show();
                        removeCartView(storedCartId);
                        loadCartsFromApi();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley Error", "Error during API request", error);
                        error.printStackTrace();
                        Toast.makeText(HomeActivity.this, "Carrinho removido com sucesso", Toast.LENGTH_SHORT).show();
                        removeCartView(storedCartId);
                        loadCartsFromApi();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }

    private void removeCartView(String removedCartId) {
        LinearLayout productsContainer = findViewById(R.id.productsContainer);
        for (int i = 0; i < productsContainer.getChildCount(); i++) {
            View child = productsContainer.getChildAt(i);
            if (child.getTag() != null && child.getTag().equals(removedCartId)) {
                productsContainer.removeViewAt(i);
                break;
            }
        }
    }

    private void createCart(String userId) {
        Log.d("API Request", "Attempting to create cart");

        String apiUrl = "https://shopping-f0qb.onrender.com/api/carts/createcart";
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                apiUrl,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("API Response", response.toString());
                            String cartId = response.optString("_id", "");
                            Intent intent = new Intent(HomeActivity.this, CartActivity.class);
                            intent.putExtra("_id", cartId);
                            startActivity(intent);
                        Toast.makeText(HomeActivity.this, "Carrinho criado com sucesso", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        Log.e("Volley Error", "Error during API request", error);
                        error.printStackTrace();
                        Toast.makeText(HomeActivity.this, "Error creating cart", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public byte[] getBody() {
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("userId", userId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return jsonBody.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        requestQueue.add(jsonObjectRequest);
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
                findViewById(R.id.textViewNoResults).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.textViewNoResults).setVisibility(View.GONE);

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
                                Intent intent = new Intent(HomeActivity.this, Invoice.class);
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
            progressBar.setVisibility(View.GONE);
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