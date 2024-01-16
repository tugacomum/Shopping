package com.example.shopping;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextView textViewLogin = findViewById(R.id.textViewLogin);
        Button buttonRegister = findViewById(R.id.buttonRegister);

        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editUsername = findViewById(R.id.editTextUsernamee);
                EditText editEmail = findViewById(R.id.editTextEmail);
                EditText editPassword = findViewById(R.id.editTextPasswordd);

                String username = editUsername.getText().toString().trim();
                String email = editEmail.getText().toString().trim();
                String password = editPassword.getText().toString().trim();

                performRegisterRequest(username, email, password);
            }
        });
    }

    private void performRegisterRequest(final String username, final String email, final String password) {
        String registerUrl = "https://shopping-f0qb.onrender.com/api/users/register";

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("name", username);
            jsonParams.put("email", email);
            jsonParams.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering...");
        progressDialog.show();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, registerUrl, jsonParams,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressDialog.dismiss();
                        Log.d("RegisterResponse", response.toString());

                            String userId = response.optString("_id", "");
                            String userName = response.optString("name", "");
                            String userEmail = response.optString("email", "");

                            if (!userId.isEmpty()) {
                                Toast.makeText(RegisterActivity.this, "Registro bem-sucedido", Toast.LENGTH_SHORT).show();

                                Log.d("UserInfo", "ID: " + userId + ", Name: " + userName + ", Email: " + userEmail);

                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(RegisterActivity.this, "Falha no registro", Toast.LENGTH_SHORT).show();
                            }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Log.e("RegisterError", "Erro de conexão: " + error.toString());
                        Toast.makeText(RegisterActivity.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }
}