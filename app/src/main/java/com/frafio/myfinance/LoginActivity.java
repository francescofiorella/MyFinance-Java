package com.frafio.myfinance;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    TextInputLayout mEmailLayout, mPasswordLayout;
    TextInputEditText mEmail, mPassword;
    Button mLoginBtn, mResendBtn;
    TextView mResetBtn, mRegisterBtn;
    Button mGoogleBtn;
    LinearProgressIndicator mProgressIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }
}