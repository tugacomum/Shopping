package com.example.shopping;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class HomeActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        sharedPreferences = getSharedPreferences("userPreferences", MODE_PRIVATE);

        Button iniciarCompraButton = findViewById(R.id.buttonStartPurchase);
        iniciarCompraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, CartActivity.class));
            }
        });

        ImageView settingsImageView = findViewById(R.id.settings);
        settingsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, UserSettings.class));
            }
        });
        String userObjectJson = sharedPreferences.getString("userObject", "");
        try {
            JSONObject userObject = new JSONObject(userObjectJson);

            // Obter o nome do usuário do objeto do usuário
            String userName = userObject.optString("name", "N/A");

            // Atualizar o texto de saudação
            TextView textViewWelcomeMessage = findViewById(R.id.textViewWelcomeMessage);
            textViewWelcomeMessage.setText("Bem-vindo de volta, " + "\n" + userName + " 👋");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}