package com.example.shopping;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.CountDownTimer;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.json.JSONException;
import org.json.JSONObject;

public class Checkout extends AppCompatActivity {
    private String cartId;
    private TextView textBelowQRCode;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 15 * 60 * 1000;
    private boolean timerRunning;
    private String jsonString;
    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        Intent intent = getIntent();
        cartId = intent.getStringExtra("_id");
        String storedCartId = intent.getStringExtra("cartId");

        sharedPreferences = getSharedPreferences("userPreferences", MODE_PRIVATE);

        textBelowQRCode = findViewById(R.id.textBelowQRCode);
        Button checkout = findViewById(R.id.buttonCheckout);

        startTimer();
        getUserCart(cartId);

        checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (storedCartId != null && !storedCartId.isEmpty()) {
                    //startActivity(new Intent(Checkout.this, HomeActivity.class));
                    //Toast.makeText(Checkout.this, "Compra suspensa com sucesso!", Toast.LENGTH_SHORT).show();
                    finishAffinity();
                } else {
                    finishBuy();
                }
            }
        });
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                textBelowQRCode.setText("");
            }
        }.start();

        timerRunning = true;
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format("Tem %02d:%02d minutos\npara realizar o checkout!", minutes, seconds);
        textBelowQRCode.setText(timeLeftFormatted);
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
                        try {
                            jsonString = response.toString();
                            Bitmap qrCodeBitmap = encodeAsBitmap(jsonString);
                            ImageView qrCodeImageView = findViewById(R.id.qrCodeImageView);
                            qrCodeImageView.setImageBitmap(qrCodeBitmap);

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.remove("cartId");
                            editor.apply();
                        } catch (WriterException e) {
                            e.printStackTrace();
                            Toast.makeText(Checkout.this, "Erro ao gerar QR Code", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        //Toast.makeText(Checkout.this, "Erro ao obter detalhes do carrinho", Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    private void finishBuy() {
        if (cartId != null && !cartId.isEmpty()) {
            String apiUrl = "https://shopping-f0qb.onrender.com/api/carts/finishbuy";
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
                            startActivity(new Intent(Checkout.this, HomeActivity.class));
                            Toast.makeText(Checkout.this, "Compra finalizada com sucesso!", Toast.LENGTH_SHORT).show();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("Volley Error", "Error during API request", error);
                            error.printStackTrace();
                            Toast.makeText(Checkout.this, "Erro ao finalizar compra", Toast.LENGTH_SHORT).show();
                        }
                    });

            requestQueue.add(jsonObjectRequest);
        } else {
            Toast.makeText(this, "ID do carrinho inválido", Toast.LENGTH_SHORT).show();
        }
    }

    private void generateQRCode(JSONObject response) {
        try {
            String cartDetails = response.toString();

            Bitmap bitmap = encodeAsBitmap(cartDetails);

            ImageView qrCodeImageView = findViewById(R.id.qrCodeImageView);
            qrCodeImageView.setImageBitmap(bitmap);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private Bitmap encodeAsBitmap(String jsonString) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(jsonString, BarcodeFormat.QR_CODE, 500, 500, null);
        } catch (IllegalArgumentException e) {
            return null;
        }

        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        Log.d("Bitmapteste", bitmap.toString());
        return bitmap;
    }
}