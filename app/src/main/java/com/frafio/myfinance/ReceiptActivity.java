package com.frafio.myfinance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.frafio.myfinance.fragments.ListFragment;
import com.frafio.myfinance.objects.Purchase;
import com.frafio.myfinance.objects.ReceiptItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class ReceiptActivity extends AppCompatActivity {

    RelativeLayout layout;
    Typeface nunito;

    MaterialToolbar mToolbar;
    TextView mTitleTV;
    RecyclerView mRecyclerView;
    FirestoreRecyclerAdapter adapter;
    ExtendedFloatingActionButton mAddBtn;

    String purchaseID;
    String purchaseName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        // servono per la snackbar
        nunito = ResourcesCompat.getFont(getApplicationContext(), R.font.nunito);
        layout = findViewById(R.id.receipt_layout);

        // toolbar
        mToolbar = findViewById(R.id.receipt_toolbar);
        setSupportActionBar(mToolbar);

        // back arrow
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTitleTV = findViewById(R.id.receipt_purchaseTitle);
        mRecyclerView = findViewById(R.id.receipt_recView);
        mAddBtn = findViewById(R.id.receipt_addBtn);

        // retrieve purchase data from intent
        purchaseID = getIntent().getStringExtra("com.frafio.myfinance.purchaseID");
        purchaseName = getIntent().getStringExtra("com.frafio.myfinance.purchaseName");

        mTitleTV.setText(purchaseName);

        loadReceiptList();
    }

    private void loadReceiptList() {
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        Query query = fStore.collection("purchases").document(purchaseID).collection("receipt")
                .orderBy("name");

        // recyclerOptions
        FirestoreRecyclerOptions<ReceiptItem> options = new FirestoreRecyclerOptions.Builder<ReceiptItem>().setQuery(query, ReceiptItem.class).build();

        adapter = new FirestoreRecyclerAdapter<ReceiptItem, ReceiptActivity.ReceiptItemViewHolder>(options) {
            @NonNull
            @Override
            public ReceiptActivity.ReceiptItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_recycler_view_receipt_item, parent, false);
                return new ReceiptActivity.ReceiptItemViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ReceiptActivity.ReceiptItemViewHolder holder, int position, @NonNull ReceiptItem model) {

            }
        };

        // View Holder
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mRecyclerView.setAdapter(adapter);
    }

    private class ReceiptItemViewHolder extends RecyclerView.ViewHolder{

        ConstraintLayout rItemLayout;
        TextView rNomeTV, rPriceTV;

        public ReceiptItemViewHolder(@NonNull View itemView) {
            super(itemView);
            rItemLayout = itemView.findViewById(R.id.recView_receiptItem_constraintLayout);
            rNomeTV = itemView.findViewById(R.id.recView_receiptItem_nomeTextView);
            rPriceTV = itemView.findViewById(R.id.recView_receiptItem_priceTextView);
        }
    }

    //start&stop listening
    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
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