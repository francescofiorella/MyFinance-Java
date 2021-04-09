package com.frafio.myfinance.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.frafio.myfinance.MainActivity;
import com.frafio.myfinance.ReceiptActivity;
import com.frafio.myfinance.R;
import com.frafio.myfinance.objects.Purchase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ListFragment extends Fragment {

    RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        mRecyclerView = view.findViewById(R.id.list_recyclerView);
        loadPurchasesList();

        return view;
    }

    public void loadPurchasesList() {
        PurchaseAdapter mAdapter = new PurchaseAdapter(getContext(), MainActivity.PURCHASELIST, MainActivity.PURCHASEIDLIST);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    class PurchaseAdapter extends RecyclerView.Adapter<PurchaseAdapter.PurchaseViewHolder> {

        Context context;
        List<Purchase> purchaseList;
        List<String> purchaseIDList;

        public PurchaseAdapter(Context c, List<Purchase> purList, List<String> purIDList){
            context = c;
            purchaseList = purList;
            purchaseIDList = purIDList;
        }

        @NonNull
        @Override
        public PurchaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.layout_recycler_view_purchase_item, parent, false);

            return new PurchaseViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PurchaseViewHolder holder, int position) {
            Locale locale = new Locale("en", "UK");
            NumberFormat nf = NumberFormat.getInstance(locale);
            DecimalFormat formatter = (DecimalFormat) nf;
            formatter.applyPattern("###,###,##0.00");
            String priceString = "€ " + formatter.format(purchaseList.get(position).getPrice());
            holder.prezzoTV.setText(priceString);

            if (purchaseList.get(position).getType() == 0) {
                holder.itemLayout.setOnClickListener(null);
                holder.nomeTV.setText(purchaseList.get(position).getName());
                holder.nomeTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                holder.dataLayout.setVisibility(View.VISIBLE);

                String dayString, monthString;
                if (purchaseList.get(position).getDay() < 10) {
                    dayString = "0" + purchaseList.get(position).getDay();
                } else {
                    dayString = purchaseList.get(position).getDay() + "";
                }
                if (purchaseList.get(position).getMonth() < 10) {
                    monthString = "0" + purchaseList.get(position).getMonth();
                } else {
                    monthString = purchaseList.get(position).getMonth() + "";
                }

                holder.dataTV.setText(dayString + "/" + monthString + "/" + purchaseList.get(position).getYear());
            } else if (purchaseList.get(position).getType() == 1){
                holder.nomeTV.setText("   " + purchaseList.get(position).getName());
                holder.nomeTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                holder.dataLayout.setVisibility(View.GONE);

                holder.itemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), ReceiptActivity.class);
                        intent.putExtra("com.frafio.myfinance.purchaseID", purchaseIDList.get(position));
                        intent.putExtra("com.frafio.myfinance.purchaseName", purchaseList.get(position).getName());
                        intent.putExtra("com.frafio.myfinance.purchasePrice", priceString);
                        startActivity(intent);
                    }
                });
            } else {
                holder.itemLayout.setClickable(true);
                holder.itemLayout.setOnClickListener(null);
                holder.nomeTV.setText("   " + purchaseList.get(position).getName());
                holder.nomeTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                holder.dataLayout.setVisibility(View.GONE);
            }

            holder.itemLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext(), R.style.ThemeOverlay_MyFinance_AlertDialog);
                    builder.setTitle(holder.nomeTV.getText().toString().trim());
                    builder.setMessage("Vuoi modificare o eliminare l'acquisto selezionato?");
                    builder.setNegativeButton("Modifica", null);
                    builder.setPositiveButton("Elimina", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseFirestore fStore = FirebaseFirestore.getInstance();
                            fStore.collection("purchases").document(purchaseIDList.get(position)).delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    MainActivity.PURCHASEIDLIST.remove(position);
                                    MainActivity.PURCHASELIST.remove(position);
                                    mRecyclerView.removeViewAt(position);
                                    mRecyclerView.getAdapter().notifyItemRemoved(position);
                                    mRecyclerView.getAdapter().notifyItemRangeChanged(position, MainActivity.PURCHASELIST.size());
                                    ((MainActivity)getActivity()).showSnackbar("Acquisto eliminato!");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("LOG", "Error! " + e.getLocalizedMessage());
                                    ((MainActivity)getActivity()).showSnackbar("Acquisto non eliminato correttamente!");
                                }
                            });
                        }
                    });
                    builder.show();
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return purchaseList.size();
        }

        public class PurchaseViewHolder extends RecyclerView.ViewHolder {

            ConstraintLayout itemLayout, dataLayout;
            TextView dataTV, nomeTV, prezzoTV;

            public PurchaseViewHolder(@NonNull View itemView) {
                super(itemView);
                itemLayout = itemView.findViewById(R.id.recView_purchaseItem_constraintLayout);
                dataLayout = itemView.findViewById(R.id.recView_purchaseItem_dataLayout);
                dataTV = itemView.findViewById(R.id.recView_purchaseItem_dataTextView);
                nomeTV = itemView.findViewById(R.id.recView_purchaseItem_nomeTextView);
                prezzoTV = itemView.findViewById(R.id.recView_purchaseItem_priceTextView);
            }
        }
    }
}
