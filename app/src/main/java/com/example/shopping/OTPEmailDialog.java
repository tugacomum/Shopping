package com.example.shopping;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.view.WindowManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.os.CountDownTimer;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class OTPEmailDialog extends Dialog {
    private Button nextButton;
    private EditText emailTxt;

    public OTPEmailDialog(@NonNull Context context, EditText email) {
        super(context);
        this.emailTxt = email;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(getContext().getResources().getColor(android.R.color.transparent)));
        setContentView(R.layout.otp_email_layout);

        nextButton = findViewById(R.id.nextBtn);
        emailTxt = findViewById(R.id.editText123);

        showKeyboard(emailTxt);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postVerificationCode(emailTxt);
            }
        });
    }

    private void showKeyboard(EditText emailTxt) {
        emailTxt.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(emailTxt, InputMethodManager.SHOW_IMPLICIT);
    }

    private void postVerificationCode(EditText emailTxt) {
        String apiUrl = "https://shopping-f0qb.onrender.com/api/users/resendcode";

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("email", emailTxt.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                apiUrl,
                requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getContext(), "Email sent successfully", Toast.LENGTH_SHORT).show();
                        OTPVerificationDialog otpEmailDialog = new OTPVerificationDialog(getContext(), emailTxt.getText().toString());
                        otpEmailDialog.setCancelable(false);
                        otpEmailDialog.show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }
}