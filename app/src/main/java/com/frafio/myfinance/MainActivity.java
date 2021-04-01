package com.frafio.myfinance;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.frafio.myfinance.objects.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    // definizione variabili
    static public User CURRENTUSER;

    CoordinatorLayout layout;
    Typeface nunito;

    Toolbar mToolbar;
    TextView mUserNameTv, mEmailTv;
    MaterialButton mLoginBtn;
    FloatingActionButton mAddBtn;

    RelativeLayout mListLayout, mProfileLayout;
    AppCompatImageView mList, mProfile;

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
        mUserNameTv = findViewById(R.id.main_username_tv);
        mEmailTv = findViewById(R.id.main_email_tv);
        mLoginBtn = findViewById(R.id.loginBtn);
        mAddBtn = findViewById(R.id.main_addBtn);

        mListLayout = findViewById(R.id.listLayout);
        mProfileLayout = findViewById(R.id.profileLayout);
        mList = findViewById(R.id.listImageView);
        mProfile = findViewById(R.id.profileImageView);

        updateCurrentUser();
        setBottomBar();
    }

    private void updateCurrentUser() {
        fAuth = FirebaseAuth.getInstance();
        FirebaseUser fUser = fAuth.getCurrentUser();

        if (fUser != null) {
            mUserNameTv.setVisibility(View.VISIBLE);
            mEmailTv.setVisibility(View.VISIBLE);
            if (CURRENTUSER == null) {
                FirebaseFirestore fStore = FirebaseFirestore.getInstance();
                fStore.collection("users").document(fUser.getUid()).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        CURRENTUSER = documentSnapshot.toObject(User.class);
                        mUserNameTv.setText(CURRENTUSER.getFullName());
                        mEmailTv.setText(CURRENTUSER.getEmail());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("LOG", "Error! " + e.getLocalizedMessage());
                    }
                });
            } else {
                mUserNameTv.setText(CURRENTUSER.getFullName());
                mEmailTv.setText(CURRENTUSER.getEmail());
            }
            mLoginBtn.setText("Log Out");
            mLoginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseAuth.getInstance().signOut();
                    showSnackbar("Logout effettuato!");
                    updateCurrentUser();
                }
            });
        } else {
            CURRENTUSER = null;
            mUserNameTv.setVisibility(View.GONE);
            mEmailTv.setVisibility(View.GONE);
            mLoginBtn.setText("Log In");
            mLoginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(new Intent(getApplicationContext(), LoginActivity.class), 1);
                }
            });
        }
    }

    private void setBottomBar() {
        mListLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mList.setSelected(true);
                mProfile.setSelected(false);
            }
        });

        mProfileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mList.setSelected(false);
                mProfile.setSelected(true);
            }
        });
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

    private void showSnackbar(String string) {
        Snackbar snackbar = Snackbar.make(layout, string, BaseTransientBottomBar.LENGTH_SHORT).setAnchorView(mAddBtn)
                .setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.snackbar))
                .setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.inverted_primary_text));
        TextView tv = (snackbar.getView()).findViewById((R.id.snackbar_text));
        tv.setTypeface(nunito);
        snackbar.show();
    }
}