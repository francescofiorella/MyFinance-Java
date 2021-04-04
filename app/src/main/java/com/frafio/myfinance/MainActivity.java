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
    TextView mFragmentTitle;
    BottomNavigationView mBottomNavigationView;
    FloatingActionButton mAddBtn;

    FirebaseAuth fAuth;

    // 1 home, 2 list, 3 profile, 4 settings
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
        mFragmentTitle = findViewById(R.id.main_fragmentTitle);
        mBottomNavigationView = findViewById(R.id.main_bottomNavView);
        mAddBtn = findViewById(R.id.main_addBtn);

        if (savedInstanceState == null) {
            setFragment(1);
        }

        updateCurrentUser();

        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        setFragment(1);
                        break;
                    case R.id.list:
                        setFragment(2);
                        break;
                    case R.id.profile:
                        setFragment(3);
                        break;
                    case R.id.settings:
                        setFragment(4);
                        break;
                }
                return true;
            }
        });
    }

    private void setFragment(int num) {
        if (currentFragment != num) {
            Fragment mFragmentToSet = null;
            switch (num) {
                case 1:
                    mFragmentTitle.setText("Dashboard");
                    mFragmentToSet = new HomeFragment();
                    break;
                case 2:
                    mFragmentTitle.setText("Lista");
                    mFragmentToSet = new ListFragment();
                    break;
                case 3:
                    mFragmentTitle.setText("Profilo");
                    mFragmentToSet = new ProfileFragment();
                    break;
                case 4:
                    mFragmentTitle.setText("Impostazioni");
                    mFragmentToSet = new SettingsFragment();
                    break;
            }
            getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.main_frameLayout, mFragmentToSet).commit();
            currentFragment = num;
        }
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
                mFragmentTitle.setText("Profilo");
                mBottomNavigationView.setSelectedItemId(R.id.profile);
                getSupportFragmentManager().beginTransaction().replace(R.id.main_frameLayout, new ProfileFragment()).commit();
                currentFragment = 2;
                showSnackbar("Hai effettuato l'accesso come " + fAuth.getCurrentUser().getDisplayName());
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (currentFragment != 1) {
            setFragment(1);
            mBottomNavigationView.setSelectedItemId(R.id.home);
        } else {
            super.onBackPressed();
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