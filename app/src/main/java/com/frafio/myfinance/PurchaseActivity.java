package com.frafio.myfinance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class PurchaseActivity extends AppCompatActivity {

    RelativeLayout layout;
    Typeface nunito;

    MaterialToolbar mToolbar;
    TextView mTitleTV;
    FloatingActionButton mAddBtn;

    String purchaseID;
    String purchaseName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);

        // servono per la snackbar
        nunito = ResourcesCompat.getFont(getApplicationContext(), R.font.nunito);
        layout = findViewById(R.id.purchase_layout);

        // toolbar
        mToolbar = findViewById(R.id.purchase_toolbar);
        setSupportActionBar(mToolbar);

        // back arrow
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTitleTV = findViewById(R.id.purchase_purchaseTitle);
        mAddBtn = findViewById(R.id.purchase_addBtn);

        // retrieve purchase data from intent
        purchaseID = getIntent().getStringExtra("com.frafio.myfinance.purchaseID");
        purchaseName = getIntent().getStringExtra("com.frafio.myfinance.purchaseName");

        mTitleTV.setText(purchaseName);

        //setPurchaseData();
    }

    private void setPurchaseData() {
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        fStore.collection("purchases").document(purchaseID).collection("receipt").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {

                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("LOG", "Error! " + e.getLocalizedMessage());
            }
        });
    }

    // ends this activity (back arrow)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(item);
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