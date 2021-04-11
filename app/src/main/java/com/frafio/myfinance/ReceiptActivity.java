package com.frafio.myfinance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cottacush.android.currencyedittext.CurrencyEditText;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.frafio.myfinance.fragments.ListFragment;
import com.frafio.myfinance.objects.Purchase;
import com.frafio.myfinance.objects.ReceiptItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
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
    TextView mTitleTV, mPriceTV;
    RecyclerView mRecyclerView;
    FirestoreRecyclerAdapter adapter;
    EditText mNameET;
    CurrencyEditText mPriceET;
    ExtendedFloatingActionButton mAddBtn;

    String purchaseID, purchaseName, purchasePrice;

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
        mPriceTV = findViewById(R.id.receipt_purchasePrice);
        mRecyclerView = findViewById(R.id.receipt_recView);
        mNameET = findViewById(R.id.receipt_name_EditText);
        mPriceET = findViewById(R.id.receipt_price_EditText);
        mAddBtn = findViewById(R.id.receipt_addBtn);

        // retrieve purchase data from intent
        purchaseID = getIntent().getStringExtra("com.frafio.myfinance.purchaseID");
        purchaseName = getIntent().getStringExtra("com.frafio.myfinance.purchaseName");
        purchasePrice = getIntent().getStringExtra("com.frafio.myfinance.purchasePrice");

        mTitleTV.setText(purchaseName);
        mPriceTV.setText(purchasePrice);

        loadReceiptList();

        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem();
            }
        });
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
                holder.rNomeTV.setText(model.getName());

                Locale locale = new Locale("en", "UK");
                NumberFormat nf = NumberFormat.getInstance(locale);
                DecimalFormat formatter = (DecimalFormat) nf;
                formatter.applyPattern("###,###,##0.00");
                holder.rPriceTV.setText("€ " + formatter.format(model.getPrice()));
                holder.rItemLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        String voceID = getSnapshots().getSnapshot(position).getId();

                        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ReceiptActivity.this, R.style.ThemeOverlay_MyFinance_AlertDialog);
                        builder.setTitle(model.getName());
                        builder.setMessage("Vuoi eliminare la voce selezionata?");
                        builder.setNegativeButton("Annulla", null);
                        builder.setPositiveButton("Elimina", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseFirestore fStore = FirebaseFirestore.getInstance();
                                fStore.collection("purchases").document(purchaseID)
                                        .collection("receipt").document(voceID).delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        showSnackbar("Voce eliminata!");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("LOG", "Error! " + e.getLocalizedMessage());
                                        showSnackbar("Voce non eliminata!");
                                    }
                                });
                            }
                        });
                        builder.show();
                        return true;
                    }
                });
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

    private void addItem() {
        String name = mNameET.getText().toString().trim();
        double price = mPriceET.getNumericValue();
        String priceString = mPriceET.getText().toString().trim();

        // controlla le info aggiunte
        if (TextUtils.isEmpty(name)) {
            mNameET.setError("Inserisci il nome dell'acquisto.");
            return;
        }

        if (TextUtils.isEmpty(priceString)) {
            mPriceET.setError("Inserisci il costo dell'acquisto.");
            return;
        }

        ReceiptItem item = new ReceiptItem(name, price);
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        fStore.collection("purchases").document(purchaseID).collection("receipt").add(item)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                showSnackbar("Voce aggiunta!");
                mNameET.setText("");
                mPriceET.setText("");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("LOG", "Error! " + e.getLocalizedMessage());
                showSnackbar("Voce non aggiunta!");
            }
        });
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
        Snackbar snackbar = Snackbar.make(layout, string, BaseTransientBottomBar.LENGTH_SHORT).setAnchorView(mNameET)
                .setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.snackbar))
                .setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.inverted_primary_text));
        TextView tv = (snackbar.getView()).findViewById((R.id.snackbar_text));
        tv.setTypeface(nunito);
        snackbar.show();
    }
}