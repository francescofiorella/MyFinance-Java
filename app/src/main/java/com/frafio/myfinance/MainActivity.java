package com.frafio.myfinance;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    // definizione variabili
    RelativeLayout layout;
    Typeface nunito;

    Toolbar mToolbar;
    MaterialButton loginBtn;

    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nunito = ResourcesCompat.getFont(getApplicationContext(), R.font.nunito);
        layout = findViewById(R.id.main_layout);

        // toolbar
        mToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);

        // collegamento view
        loginBtn = findViewById(R.id.loginBtn);

        updateCurrentUser();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            boolean userRequest = data.getBooleanExtra("com.frafio.myfinance.userRequest", false);
            if (userRequest) {
                updateCurrentUser();
                showSnackbar("Hai effettuato l'accesso come " + fAuth.getCurrentUser().getDisplayName());
            }
        }
    }

    private void updateCurrentUser() {
        fAuth = FirebaseAuth.getInstance();
        FirebaseUser fUser = fAuth.getCurrentUser();

        if (fUser != null) {
            loginBtn.setText("Log Out");
            loginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseAuth.getInstance().signOut();
                    showSnackbar("Logout effettuato!");
                    updateCurrentUser();
                }
            });
        } else {
            loginBtn.setText("Log In");
            loginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(new Intent(getApplicationContext(), LoginActivity.class), 1);
                }
            });
        }
    }

    private void showSnackbar(String string) {
        Snackbar snackbar = Snackbar.make(layout, string, BaseTransientBottomBar.LENGTH_SHORT)
                .setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.snackbar))
                .setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.inverted_primary_text));
        TextView tv = (snackbar.getView()).findViewById((R.id.snackbar_text));
        tv.setTypeface(nunito);
        snackbar.show();
    }
}