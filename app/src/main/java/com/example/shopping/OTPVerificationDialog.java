package com.example.shopping;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

public class OTPVerificationDialog extends Dialog {
    private EditText otpET1, otpET2, otpET3, otpET4, otpET5, otpET6;
    private TextView resendBtn;
    private Button verifyBtn;
    private boolean resendEnabled = false;
    private int resendTime = 60;
    private int selectedETPosition = 0;
    private final String emailTxt;
    public OTPVerificationDialog (@NonNull Context context, String email) {
        super(context);
        this.emailTxt = email;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(getContext().getResources().getColor(android.R.color.transparent)));
        setContentView(R.layout.otp_dialog_layout);

        otpET1 = findViewById(R.id.otpET1);
        otpET2 = findViewById(R.id.otpET2);
        otpET3 = findViewById(R.id.otpET3);
        otpET4 = findViewById(R.id.otpET4);
        otpET5 = findViewById(R.id.otpET5);
        otpET6 = findViewById(R.id.otpET6);

        resendBtn = findViewById(R.id.resendBtn);
        verifyBtn = findViewById(R.id.verifyBtn);
        final TextView email = findViewById(R.id.otpEmail);

        otpET1.addTextChangedListener(textWatcher);
        otpET2.addTextChangedListener(textWatcher);
        otpET3.addTextChangedListener(textWatcher);
        otpET4.addTextChangedListener(textWatcher);
        otpET5.addTextChangedListener(textWatcher);
        otpET6.addTextChangedListener(textWatcher);

        showKeyboard(otpET1);

        startCountDownTimer();

        email.setText(emailTxt);

        resendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (resendEnabled) {
                    startCountDownTimer();
                }
            }
        });

        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String getOTP = otpET1.getText().toString() + otpET2.getText().toString() + otpET3.getText().toString() + otpET4.getText().toString() + otpET5.getText().toString() + otpET6.getText().toString();
                if (getOTP.length() == 6) {
                    postVerificationCode(emailTxt, getOTP);
                }
            }
        });
    }

    private void postVerificationCode(String email, String verificationCode) {
        String apiUrl = "https://shopping-f0qb.onrender.com/api/users/verify";

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("email", email);
            requestBody.put("code", verificationCode);
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
                        Toast.makeText(getContext(), "Verification Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getContext(), MainActivity.class);
                        getContext().startActivity(intent);

                        dismiss();
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

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() > 0) {
                if (selectedETPosition == 0) {
                    selectedETPosition = 1;
                    showKeyboard(otpET2);
                } else if (selectedETPosition == 1) {
                    selectedETPosition = 2;
                    showKeyboard(otpET3);
                } else if (selectedETPosition == 2) {
                    selectedETPosition = 3;
                    showKeyboard(otpET4);
                } else if (selectedETPosition == 3) {
                    selectedETPosition = 4;
                    showKeyboard(otpET5);
                } else if (selectedETPosition == 4) {
                    selectedETPosition = 5;
                    showKeyboard(otpET6);
                } else {
                    verifyBtn.setBackgroundResource(R.drawable.round_back_red100);
                }
            }
        }
    };

    private void showKeyboard(EditText otpET) {
        otpET.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(otpET, InputMethodManager.SHOW_IMPLICIT);
    }

    private void startCountDownTimer() {
        resendEnabled = false;
        resendBtn.setTextColor(Color.parseColor("#99000000"));

        new CountDownTimer(resendTime * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                resendBtn.setText("Resend Code ("+(millisUntilFinished/1000)+")");
            }
            @Override
            public void onFinish() {
                resendEnabled = true;
                resendBtn.setText("Resend Code");
                resendBtn.setTextColor(getContext().getResources().getColor(android.R.color.holo_blue_dark));
            }
        }.start();
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DEL) {
            if (selectedETPosition == 5) {
                selectedETPosition = 4;
                showKeyboard(otpET5);
            } else if (selectedETPosition == 4) {
                selectedETPosition = 3;
                showKeyboard(otpET4);
            } else if (selectedETPosition == 3) {
                selectedETPosition = 2;
                showKeyboard(otpET3);
            } else if (selectedETPosition == 2) {
                selectedETPosition = 1;
                showKeyboard(otpET2);
            } else if (selectedETPosition == 1) {
                selectedETPosition = 0;
                showKeyboard(otpET1);
            }

            verifyBtn.setBackgroundColor(R.drawable.round_back_brown100);
            return true;
        } else {
            return super.onKeyUp(keyCode, event);
        }
    }
}