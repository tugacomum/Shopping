package com.example.shopping;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {
    private List<Product> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        findViewById(R.id.imageViewBackArrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        productList = new ArrayList<>();

        ImageView settingsImageView = findViewById(R.id.settingss);
        settingsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CartActivity.this, UserSettings.class));
            }
        });

        Button buttonFinish = findViewById(R.id.buttonFinish);
        buttonFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CartActivity.this, FinishBuy.class);
                startActivity(intent);
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
    private void startQRScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("");
        integrator.initiateScan();
    }

    private void resetOrientation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
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
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardLayoutParams.setMargins(0, 0, 0, 8);
        cardView.setLayoutParams(cardLayoutParams);
        cardView.setCardBackgroundColor(Color.WHITE);
        cardView.setRadius(20);
        LinearLayout cardContentLayout = new LinearLayout(this);
        cardContentLayout.setOrientation(LinearLayout.HORIZONTAL);
        cardContentLayout.setPadding(30, 30, 30, 30);
        ImageView imageView = new ImageView(this);
        String productImage = productData.getString("productImage");
        Picasso.get().load(productImage).into(imageView);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                150,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        imageView.setLayoutParams(imageParams);
        LinearLayout textContentLayout = new LinearLayout(this);
        textContentLayout.setOrientation(LinearLayout.VERTICAL);
        textContentLayout.setPadding(16, 0, 0, 0);
        String productName = productData.getString("productName");
        String productId = productData.getString("productId");
        String productPrice = productData.getString("price");
        TextView nameTextView = new TextView(this);
        nameTextView.setText(productName);
        nameTextView.setTypeface(null, Typeface.BOLD);
        TextView priceTextView = new TextView(this);
        priceTextView.setText("Preço: " + productPrice + "€");
        textContentLayout.addView(nameTextView);
        textContentLayout.addView(priceTextView);
        cardContentLayout.addView(imageView);
        cardContentLayout.addView(textContentLayout);
        cardView.addView(cardContentLayout);
        LinearLayout productsContainer = findViewById(R.id.productsContainer);
        productsContainer.addView(cardView);

        Product product = new Product(productName, productPrice, productImage, productId);
        productList.add(product);

        Toast.makeText(this, "Produto adicionado ao carrinho", Toast.LENGTH_SHORT).show();
    }
}
