package com.frafio.myfinance;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityOptionsCompat;
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
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.frafio.myfinance.fragments.DashboardFragment;
import com.frafio.myfinance.fragments.ListFragment;
import com.frafio.myfinance.fragments.ProfileFragment;
import com.frafio.myfinance.fragments.MenuFragment;
import com.frafio.myfinance.objects.Purchase;
import com.frafio.myfinance.objects.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // definizione variabili
    static public User CURRENTUSER;
    static public List<Purchase> PURCHASELIST;
    static public List<String> PURCHASEIDLIST;

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

        // servono per la snackbar
        nunito = ResourcesCompat.getFont(getApplicationContext(), R.font.nunito);
        layout = findViewById(R.id.main_layout);

        // toolbar
        mToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);

        // collegamento view
        mFragmentTitle = findViewById(R.id.main_fragmentTitle);
        mBottomNavigationView = findViewById(R.id.main_bottomNavView);
        mAddBtn = findViewById(R.id.main_addBtn);

        // inizializza i fragments
        currentFragment = 0;

        // controlla se si è appena fatto l'accesso
        fAuth = FirebaseAuth.getInstance();
        if (getIntent().hasExtra("com.frafio.myfinance.userRequest")){
            boolean userRequest = getIntent().getExtras().getBoolean("com.frafio.myfinance.userRequest", false);
            if (userRequest) {
                showSnackbar("Hai effettuato l'accesso come " + fAuth.getCurrentUser().getDisplayName());
            }
        }

        // aggiorna i dati dell'utente
        updateCurrentUser();

        // imposta la bottomNavView
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.dashboard:
                        setFragment(1);
                        break;
                    case R.id.list:
                        setFragment(2);
                        break;
                    case R.id.profile:
                        setFragment(3);
                        break;
                    case R.id.menu:
                        setFragment(4);
                        break;
                }
                return true;
            }
        });

        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeClipRevealAnimation(
                        mAddBtn, 0, 0,
                        mAddBtn.getMeasuredWidth(), mAddBtn.getMeasuredHeight());
                Intent intent = new Intent(getApplicationContext(), AddActivity.class);
                startActivityForResult(intent, 1, activityOptionsCompat.toBundle());
            }
        });
    }

    // metodo per cambiare fragment (senza influenzare la bottomNavView)
    public void setFragment(int num) {
        if (currentFragment != num) {
            Fragment mFragmentToSet = null;
            switch (num) {
                case 1:
                    mFragmentTitle.setText("Dashboard");
                    mFragmentToSet = new DashboardFragment();
                    break;
                case 2:
                    mFragmentTitle.setText("Lista acquisti");
                    mFragmentToSet = new ListFragment();
                    break;
                case 3:
                    mFragmentTitle.setText("Profilo");
                    mFragmentToSet = new ProfileFragment();
                    break;
                case 4:
                    mFragmentTitle.setText("Menu");
                    mFragmentToSet = new MenuFragment();
                    break;
            }
            getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.main_frameLayout, mFragmentToSet).commit();
            currentFragment = num;
        }
    }

    // metodo per aggiornare i dati dell'utente
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

            updateList();
        }
    }

    // metodo per aggiornare i progressi dell'utente
    public void updateList() {
        PURCHASELIST = new LinkedList<>();
        PURCHASEIDLIST = new LinkedList<>();
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        fStore.collection("purchases").orderBy("year", Query.Direction.DESCENDING)
                .orderBy("month", Query.Direction.DESCENDING).orderBy("day", Query.Direction.DESCENDING)
                .orderBy("type").orderBy("price", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int position = 0;
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    Purchase purchase = document.toObject(Purchase.class);
                    PURCHASEIDLIST.add(position, document.getId());
                    PURCHASELIST.add(position, purchase);
                    position ++;
                }

                if (currentFragment == 0) {
                    setFragment(1);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("LOG", "Error! " + e.getLocalizedMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            boolean purchaseRequest = data.getBooleanExtra("com.frafio.myfinance.purchaseRequest", false);
            if (purchaseRequest) {
                if (currentFragment == 2) {
                    ListFragment fragment = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.main_frameLayout);
                    fragment.loadPurchasesList();
                } else {
                    mBottomNavigationView.setSelectedItemId(R.id.list);
                }
                showSnackbar("Acquisto aggiunto!");
            }
        }
    }

    // backPressed
    @Override
    public void onBackPressed() {
        if (currentFragment != 1) {
            mBottomNavigationView.setSelectedItemId(R.id.dashboard);
        } else {
            super.onBackPressed();
        }
    }

    // snackbar
    public void showSnackbar(String string) {
        Snackbar snackbar = Snackbar.make(layout, string, BaseTransientBottomBar.LENGTH_SHORT).setAnchorView(mAddBtn)
                .setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.snackbar))
                .setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.inverted_primary_text));
        TextView tv = (snackbar.getView()).findViewById((R.id.snackbar_text));
        tv.setTypeface(nunito);
        snackbar.show();
    }
}