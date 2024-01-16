package com.example.shopping;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {
    private List<Product> productList;
    private SharedPreferences sharedPreferences;
    private String storedCartId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        Intent intent = getIntent();
        String cartId = intent.getStringExtra("_id");

        findViewById(R.id.imageViewBackArrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        productList = new ArrayList<>();
        sharedPreferences = getSharedPreferences("userPreferences", MODE_PRIVATE);
        storedCartId = sharedPreferences.getString("cartId", null);

        Button buttonPause = findViewById(R.id.buttonPause);

        if (storedCartId != null && !storedCartId.isEmpty()) {
            findViewById(R.id.textViewEmptyCart).setVisibility(View.GONE);
            getUserCart(storedCartId);
        }

        buttonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (productList.isEmpty()) {
                    Toast.makeText(CartActivity.this, "Adicione pelo menos um produto ao carrinho.", Toast.LENGTH_SHORT).show();
                } else {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("cartId", cartId);
                    editor.apply();
                    storedCartId = sharedPreferences.getString("cartId", null);
                    addProductsToCart(cartId, productList, false);
                }
            }
        });

        Button buttonFinish = findViewById(R.id.buttonFinish);
        buttonFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (productList.isEmpty()) {
                    Toast.makeText(CartActivity.this, "Adicione pelo menos um produto ao carrinho.", Toast.LENGTH_SHORT).show();
                } else {
                    addProductsToCart(cartId, productList, true);
                }
            }
        });
        ImageView scanButton = findViewById(R.id.buttonScan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQRScanner();
            }
        });
    }

    private void getUserCart(String storedCartId) {
        String apiUrl = "https://shopping-f0qb.onrender.com/api/carts/getusercart";

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("cartId", storedCartId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                apiUrl,
                requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        processUserCartResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley Error", "Error during API request", error);
                        error.printStackTrace();
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    private void processUserCartResponse(JSONObject response) {
        try {
            if (response.has("products")) {
                JSONArray productsArray = response.getJSONArray("products");
                for (int i = 0; i < productsArray.length(); i++) {
                    JSONObject productObject = productsArray.getJSONObject(i);

                    String productId = productObject.optString("productId", "");
                    String productName = productObject.optString("productName", "");
                    String productPrice = productObject.optString("price", "");
                    String productImage = productObject.optString("productImage", "");
                    int quantity = productObject.optInt("quantity", 0);

                    Product product = new Product(productId, productName, productPrice, productImage, quantity);
                    productList.add(product);
                }

                displayProductsInUI(productList);
                displayTotalPrice(productList);
            } else {
                Toast.makeText(CartActivity.this, "O carrinho do usuário está vazio", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void displayProductsInUI(List<Product> products) {
        LinearLayout productsContainer = findViewById(R.id.productsContainer);
        productsContainer.removeAllViews();

        for (Product product : products) {
            View cardView = getLayoutInflater().inflate(R.layout.product_card_layout, productsContainer, false);
            configureCardView(cardView, product);
            cardView.setTag(product.getProductId());
            productsContainer.addView(cardView);
        }
    }

    private void addProductsToCart(String cartId, List<Product> products, Boolean isCheckout) {
        String apiUrl = "https://shopping-f0qb.onrender.com/api/carts/addproducttocart";
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JSONArray productsArray = new JSONArray();
        for (Product product : products) {
            try {
                JSONObject productObject = new JSONObject();
                productObject.put("quantity", product.getQuantity());
                productObject.put("price", product.getProductPrice());
                productObject.put("productName", product.getProductName());
                productObject.put("productImage", product.getProductImage());
                productObject.put("productId", product.getProductId());
                productsArray.put(productObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("cartId", cartId);
            requestBody.put("products", productsArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.PATCH,
                apiUrl,
                requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (!isCheckout) {
                            Intent intent = new Intent(CartActivity.this, Checkout.class);
                            intent.putExtra("cartId", storedCartId);
                            startActivity(intent);
                            Toast.makeText(CartActivity.this, "Compra suspensa com sucesso!", Toast.LENGTH_SHORT).show();
                        } else {
                            Intent intent = new Intent(CartActivity.this, FinishBuy.class);
                            intent.putExtra("_id", cartId);
                            startActivity(intent);
                            Toast.makeText(CartActivity.this, "Compra efetuada com sucesso!", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley Error", "Error during API request", error);
                        error.printStackTrace();
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    private void startQRScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("");
        integrator.setOrientationLocked(true);
        integrator.initiateScan();
    }

    private void resetOrientation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    private void displayTotalPrice(List<Product> products) {
        double totalPrice = 0;

        for (Product product : products) {
            double productPrice = Double.parseDouble(product.getProductPrice());
            int quantity = product.getQuantity();
            totalPrice += productPrice * quantity;
        }

        TextView textViewTotalPriceFinishBuyyy = findViewById(R.id.textViewTotalPriceFinishBuyyy);
        textViewTotalPriceFinishBuyyy.setText(String.format("Total: %.2f€", totalPrice));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                String scanResult = result.getContents();

                try {
                    JSONObject jsonProduct = new JSONObject(scanResult);
                    createProductCard(jsonProduct);
                } catch (JSONException e) {
                    Toast.makeText(this, "QR Code inválido", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } else {
                Log.d("QRCodeResult", "Scanner Canceled");
            }
        }
        resetOrientation();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    private void createProductCard(JSONObject productData) throws JSONException {
        findViewById(R.id.textViewEmptyCart).setVisibility(View.GONE);
        String productId = productData.getString("productId");
        Product existingProduct = findProductById(productId);

        LinearLayout productsContainer = findViewById(R.id.productsContainer);

        if (existingProduct != null) {
            View cardView = findProductCardView(existingProduct);

            if (cardView != null) {
                EditText editTextQuantity = cardView.findViewById(R.id.editTextQuantity);
                int currentQuantity = existingProduct.getQuantity();
                currentQuantity++;
                existingProduct.setQuantity(currentQuantity);
                editTextQuantity.setText(String.valueOf(currentQuantity));
                updateProductQuantity(existingProduct.getProductId(), currentQuantity);

                Toast.makeText(this, "Produto já adicionado. Quantidade atualizada.", Toast.LENGTH_SHORT).show();
            }
        } else {
            View cardView = getLayoutInflater().inflate(R.layout.product_card_layout, productsContainer, false);

            Product product = new Product(
                    productData.getString("productId"),
                    productData.getString("productName"),
                    productData.getString("price"),
                    productData.getString("productImage"),
                    1
            );

            configureCardView(cardView, product);

            cardView.setTag(productId);

            productsContainer.addView(cardView);

            productList.add(product);

            displayTotalPrice(productList);

            Toast.makeText(this, "Produto adicionado ao carrinho", Toast.LENGTH_SHORT).show();
        }
    }

    private void configureCardView(View cardView, Product product) {
        ImageView imageViewProduct = cardView.findViewById(R.id.imageViewProduct);
        TextView textViewProductName = cardView.findViewById(R.id.textViewProductName);
        TextView textViewProductPrice = cardView.findViewById(R.id.textViewProductPrice);
        ImageView buttonDecrease = cardView.findViewById(R.id.buttonDecrease);
        EditText editTextQuantity = cardView.findViewById(R.id.editTextQuantity);
        ImageView buttonIncrease = cardView.findViewById(R.id.buttonIncrease);
        ImageView buttonDelete = cardView.findViewById(R.id.buttonDeleteee);

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteProduct(product);
            }
        });

        editTextQuantity.setText(String.valueOf(product.getQuantity()));

        String productImage = product.getProductImage();
        String productName = product.getProductName();
        String productPrice = product.getProductPrice();

        Picasso.get().load(productImage).into(imageViewProduct);
        textViewProductName.setText(productName);
        textViewProductPrice.setText("Preço: " + productPrice + "€");

        buttonDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentQuantity = Integer.parseInt(editTextQuantity.getText().toString());
                if (currentQuantity > 1) {
                    currentQuantity--;
                    editTextQuantity.setText(String.valueOf(currentQuantity));
                    updateProductQuantity(product.getProductId(), currentQuantity);
                    updateTotalPrice();
                }
            }
        });

        buttonIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentQuantity = Integer.parseInt(editTextQuantity.getText().toString());
                currentQuantity++;
                editTextQuantity.setText(String.valueOf(currentQuantity));
                updateProductQuantity(product.getProductId(), currentQuantity);
                updateTotalPrice();
            }
        });
    }

    private void deleteProduct(Product product) {
        productList.remove(product);
        displayProductsInUI(productList);
        updateTotalPrice();
        if (productList.isEmpty()) {
            findViewById(R.id.textViewEmptyCart).setVisibility(View.VISIBLE);
        }
    }

    private void updateTotalPrice() {
        displayTotalPrice(productList);
    }

    private View findProductCardView(Product product) {
        LinearLayout productsContainer = findViewById(R.id.productsContainer);
        for (int i = 0; i < productsContainer.getChildCount(); i++) {
            View cardView = productsContainer.getChildAt(i);
            if (cardView != null && cardView.getTag() != null) {
                String cardProductId = (String) cardView.getTag();
                if (cardProductId.equals(product.getProductId())) {
                    return cardView;
                }
            }
        }
        return null;
    }

    private void updateProductQuantity(String productId, int quantity) {
        for (Product product : productList) {
            if (product.getProductId().equals(productId)) {
                product.setQuantity(quantity);
                break;
            }
        }
        displayTotalPrice(productList);
    }

    private Product findProductById(String productId) {
        for (Product product : productList) {
            if (product.getProductId().equals(productId)) {
                return product;
            }
        }
        return null;
    }
}
