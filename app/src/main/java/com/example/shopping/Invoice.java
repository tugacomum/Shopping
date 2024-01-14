package com.example.shopping;

import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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

import java.util.ArrayList;
import java.util.List;

public class Invoice extends AppCompatActivity {

    private List<Product> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice);

        String cartId = getIntent().getStringExtra("_id");

        findViewById(R.id.imageViewBackArrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.buttonFinishBuyyyy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Realize o pedido para obter os detalhes do carrinho e exibir os produtos na fatura
        getUserCart(cartId);
    }

    private void displayProductCards(List<Product> products) {
        LinearLayout productsContainerFinishBuy = findViewById(R.id.productsContainerFinishBuy);

        for (Product product : products) {
            View cardView = getLayoutInflater().inflate(R.layout.product_card_layout, productsContainerFinishBuy, false);
            configureCardView(cardView, product);
            productsContainerFinishBuy.addView(cardView);
        }
    }

    private void configureCardView(View cardView, Product product) {
        ImageView imageViewProduct = cardView.findViewById(R.id.imageViewProduct);
        TextView textViewProductName = cardView.findViewById(R.id.textViewProductName);
        TextView textViewProductPrice = cardView.findViewById(R.id.textViewProductPrice);
        EditText editTextQuantity = cardView.findViewById(R.id.editTextQuantity);
        ImageView imageViewMinus = cardView.findViewById(R.id.buttonDecrease);
        ImageView imageViewPlus = cardView.findViewById(R.id.buttonIncrease);
        ImageView imageViewDelete = cardView.findViewById(R.id.buttonDeleteee);

        imageViewMinus.setVisibility(View.GONE);
        imageViewPlus.setVisibility(View.GONE);
        imageViewDelete.setVisibility(View.GONE);

        Picasso.get().load(product.getProductImage()).into(imageViewProduct);
        textViewProductName.setText(product.getProductName());
        textViewProductPrice.setText("Preço: " + product.getProductPrice() + "€");
        editTextQuantity.setText(String.valueOf(product.getQuantity()));
    }

    private void getUserCart(String cartId) {
        String apiUrl = "https://shopping-f0qb.onrender.com/api/carts/getusercart";
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("cartId", cartId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                apiUrl,
                requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        productList = parseCartResponse(response);
                        displayProductCards(productList);
                        displayTotalPrice(productList);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley Error", "Error during API request", error);
                        error.printStackTrace();
                        Toast.makeText(Invoice.this, "Erro ao obter detalhes do carrinho", Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    private void displayTotalPrice(List<Product> products) {
        double totalPrice = 0;

        for (Product product : products) {
            double productPrice = Double.parseDouble(product.getProductPrice());
            int quantity = product.getQuantity();
            totalPrice += productPrice * quantity;
        }

        TextView textViewTotalPriceFinishBuy = findViewById(R.id.textViewTotalPriceFinishBuy);
        textViewTotalPriceFinishBuy.setText(String.format("Total: %.2f€", totalPrice));
    }

    private List<Product> parseCartResponse(JSONObject response) {
        List<Product> productList = new ArrayList<>();

        try {
            JSONArray productsArray = response.getJSONArray("products");
            for (int i = 0; i < productsArray.length(); i++) {
                JSONObject productObject = productsArray.getJSONObject(i);

                String productId = productObject.getString("productId");
                String productName = productObject.getString("productName");
                String productPrice = productObject.getString("price");
                String productImage = productObject.getString("productImage");
                int quantity = productObject.getInt("quantity");

                Product product = new Product(productId, productName, productPrice, productImage, quantity);
                productList.add(product);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return productList;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}