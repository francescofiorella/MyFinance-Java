package com.frafio.myfinance;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.frafio.myfinance.fragments.HomeFragment;
import com.frafio.myfinance.fragments.ListFragment;
import com.frafio.myfinance.fragments.ProfileFragment;
import com.frafio.myfinance.fragments.SettingsFragment;
import com.frafio.myfinance.objects.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

    MaterialToolbar mToolbar;
    BottomNavigationView mBottomNavigationView;
    FloatingActionButton mAddBtn;

    FirebaseAuth fAuth;

    // 0 home, 1 list, 2 profile, 3 settings
    int currentFragment;

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
        mBottomNavigationView = findViewById(R.id.main_bottomNavView);
        mAddBtn = findViewById(R.id.main_addBtn);

        if (savedInstanceState == null) {
            mToolbar.setTitle("MyFinance");
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frameLayout, new HomeFragment()).commit();
            currentFragment = 0;
        }

        updateCurrentUser();

        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment mFragmentToSet = null;
                switch (item.getItemId()) {
                    case R.id.home:
                        if (currentFragment != 0) {
                            mToolbar.setTitle("MyFinance");
                            mFragmentToSet = new HomeFragment();
                            currentFragment = 0;
                        }
                        break;
                    case R.id.list:
                        if (currentFragment != 1) {
                            mToolbar.setTitle("Lista");
                            mFragmentToSet = new ListFragment();
                            currentFragment = 1;
                        }
                        break;
                    case R.id.profile:
                        if (currentFragment != 2) {
                            mToolbar.setTitle("Profilo");
                            mFragmentToSet = new ProfileFragment();
                            currentFragment = 2;
                        }
                        break;
                    case R.id.settings:
                        if (currentFragment != 3) {
                            mToolbar.setTitle("Impostazioni");
                            mFragmentToSet = new SettingsFragment();
                            currentFragment = 3;
                        }
                        break;
                }
                if (mFragmentToSet != null) {
                    getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                            .replace(R.id.main_frameLayout, mFragmentToSet).commit();
                }
                return true;
            }
        });
    }

    private void updateCurrentUser() {
        fAuth = FirebaseAuth.getInstance();
        FirebaseUser fUser = fAuth.getCurrentUser();

        if (fUser != null) {
            if (CURRENTUSER == null) {
                FirebaseFirestore fStore = FirebaseFirestore.getInstance();
                fStore.collection("users").document(fUser.getUid()).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        CURRENTUSER = documentSnapshot.toObject(User.class);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("LOG", "Error! " + e.getLocalizedMessage());
                    }
                });
            }
        } else {
            CURRENTUSER = null;
        }
    }

    public void goToLogin() {
        startActivityForResult(new Intent(getApplicationContext(), LoginActivity.class), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            boolean userRequest = data.getBooleanExtra("com.frafio.myfinance.userRequest", false);
            if (userRequest) {
                updateCurrentUser();
                mToolbar.setTitle("Profilo");
                getSupportFragmentManager().beginTransaction().replace(R.id.main_frameLayout, new ProfileFragment()).commit();
                currentFragment = 2;
                showSnackbar("Hai effettuato l'accesso come " + fAuth.getCurrentUser().getDisplayName());
            }
        }
    }

    public void showSnackbar(String string) {
        Snackbar snackbar = Snackbar.make(layout, string, BaseTransientBottomBar.LENGTH_SHORT).setAnchorView(mAddBtn)
                .setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.snackbar))
                .setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.inverted_primary_text));
        TextView tv = (snackbar.getView()).findViewById((R.id.snackbar_text));
        tv.setTypeface(nunito);
        snackbar.show();
    }
}