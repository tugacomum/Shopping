package com.example.shopping;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class MainActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("userPreferences", MODE_PRIVATE);
        if (isUserLoggedIn()) {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
        } else {
            View textViewRegister = findViewById(R.id.textViewRegister);
            Button buttonLogin = findViewById(R.id.buttonLogin);
            textViewRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MainActivity.this, RegisterActivity.class));
                }
            });
            buttonLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText editLogin = findViewById(R.id.editTextUsername);
                    EditText editSenha = findViewById(R.id.editTextPassword);
                    String email = editLogin.getText().toString().trim();
                    String password = editSenha.getText().toString().trim();
                    performLoginRequest(email, password);
                }
            });
        }
    }
    private void saveUserToPreferences(JSONObject userObject) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userObject", userObject.toString());
        editor.apply();
    }

    private boolean isUserLoggedIn() {
        return sharedPreferences.contains("userObject");
    }
    private ProgressDialog progressDialog;
    private void performLoginRequest(final String email, final String password) {
        String loginUrl = "https://shopping-f0qb.onrender.com/api/users/login";
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("email", email);
            jsonParams.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.show();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, loginUrl, jsonParams,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressDialog.dismiss();
                            saveUserToPreferences(response);
                            String userName = response.optString("name", "N/A");
                            Log.d("UserName", userName);
                            if (response != null) {
                                startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this, "Credenciais incorretas", Toast.LENGTH_SHORT).show();
                            }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Log.e("LoginError", "Credenciais Incorretas" + error.toString());
                        Toast.makeText(MainActivity.this, "Credenciais Incorretas", Toast.LENGTH_SHORT).show();
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }
}
