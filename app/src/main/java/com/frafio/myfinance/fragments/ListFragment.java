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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.frafio.myfinance.AddActivity;
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
    TextView mWarningTV;

    // utile quando si elimina un acquisto
    int totPosition;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        mRecyclerView = view.findViewById(R.id.list_recyclerView);
        mWarningTV = view.findViewById(R.id.list_warningTV);

        if (MainActivity.PURCHASELIST.isEmpty()) {
            mWarningTV.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            mWarningTV.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            loadPurchasesList();
        }

        return view;
    }

    public void loadPurchasesList() {
        PurchaseAdapter mAdapter = new PurchaseAdapter(getContext());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    class PurchaseAdapter extends RecyclerView.Adapter<PurchaseAdapter.PurchaseViewHolder> {

        Context context;

        public PurchaseAdapter(Context c){
            context = c;
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
            String priceString = "€ " + formatter.format(MainActivity.PURCHASELIST.get(position).getPrice());
            holder.prezzoTV.setText(priceString);

            if (MainActivity.PURCHASELIST.get(position).getType() == 0) {
                holder.itemLayout.setOnClickListener(null);
                holder.nomeTV.setText(MainActivity.PURCHASELIST.get(position).getName());
                holder.nomeTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                holder.dataLayout.setVisibility(View.VISIBLE);

                String dayString, monthString;
                if (MainActivity.PURCHASELIST.get(position).getDay() < 10) {
                    dayString = "0" + MainActivity.PURCHASELIST.get(position).getDay();
                } else {
                    dayString = MainActivity.PURCHASELIST.get(position).getDay() + "";
                }
                if (MainActivity.PURCHASELIST.get(position).getMonth() < 10) {
                    monthString = "0" + MainActivity.PURCHASELIST.get(position).getMonth();
                } else {
                    monthString = MainActivity.PURCHASELIST.get(position).getMonth() + "";
                }

                holder.dataTV.setText(dayString + "/" + monthString + "/" + MainActivity.PURCHASELIST.get(position).getYear());
            } else if (MainActivity.PURCHASELIST.get(position).getType() == 1) {
                holder.nomeTV.setText("   " + MainActivity.PURCHASELIST.get(position).getName());
                holder.nomeTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                holder.dataLayout.setVisibility(View.GONE);

                holder.itemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), ReceiptActivity.class);
                        intent.putExtra("com.frafio.myfinance.purchaseID", MainActivity.PURCHASEIDLIST.get(position));
                        intent.putExtra("com.frafio.myfinance.purchaseName", MainActivity.PURCHASELIST.get(position).getName());
                        intent.putExtra("com.frafio.myfinance.purchasePrice", priceString);
                        startActivity(intent);
                    }
                });
            } else {
                holder.itemLayout.setOnClickListener(null);
                holder.nomeTV.setText("   " + MainActivity.PURCHASELIST.get(position).getName());
                holder.nomeTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                holder.dataLayout.setVisibility(View.GONE);
            }

            if (!(MainActivity.PURCHASELIST.get(position).getName().equals("Totale")
                    && MainActivity.PURCHASELIST.get(position).getPrice() != 0)) {
                holder.itemLayout.setEnabled(true);
                holder.itemLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext(), R.style.ThemeOverlay_MyFinance_AlertDialog);
                        builder.setTitle(MainActivity.PURCHASELIST.get(position).getName());

                        if (!(MainActivity.PURCHASELIST.get(position).getName().equals("Totale")
                                && MainActivity.PURCHASELIST.get(position).getPrice() == 0)) {
                            builder.setMessage("Vuoi modificare o eliminare l'acquisto selezionato?");
                            builder.setNegativeButton("Modifica", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(getContext(), AddActivity.class);
                                    intent.putExtra("com.frafio.myfinance.REQUESTCODE", 2);
                                    intent.putExtra("com.frafio.myfinance.PURCHASE_ID", MainActivity.PURCHASEIDLIST.get(position));
                                    intent.putExtra("com.frafio.myfinance.PURCHASE_NAME", MainActivity.PURCHASELIST.get(position).getName());
                                    intent.putExtra("com.frafio.myfinance.PURCHASE_PRICE", MainActivity.PURCHASELIST.get(position).getPrice());
                                    intent.putExtra("com.frafio.myfinance.PURCHASE_TYPE", MainActivity.PURCHASELIST.get(position).getType());
                                    intent.putExtra("com.frafio.myfinance.PURCHASE_POSITION", position);
                                    intent.putExtra("com.frafio.myfinance.PURCHASE_YEAR", MainActivity.PURCHASELIST.get(position).getYear());
                                    intent.putExtra("com.frafio.myfinance.PURCHASE_MONTH", MainActivity.PURCHASELIST.get(position).getMonth());
                                    intent.putExtra("com.frafio.myfinance.PURCHASE_DAY", MainActivity.PURCHASELIST.get(position).getDay());
                                    getActivity().startActivityForResult(intent, 2);
                                }
                            });
                        } else {
                            builder.setMessage("Vuoi eliminare l'acquisto selezionato?");
                        }

                        builder.setPositiveButton("Elimina", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseFirestore fStore = FirebaseFirestore.getInstance();
                                fStore.collection("purchases").document(MainActivity.PURCHASEIDLIST.get(position)).delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                if (MainActivity.PURCHASELIST.get(position).getName().equals("Totale")) {
                                                    MainActivity.PURCHASEIDLIST.remove(position);
                                                    MainActivity.PURCHASELIST.remove(position);
                                                    mRecyclerView.removeViewAt(position);
                                                    mRecyclerView.getAdapter().notifyItemRemoved(position);
                                                    mRecyclerView.getAdapter().notifyItemRangeChanged(position, MainActivity.PURCHASELIST.size());
                                                    ((MainActivity)getActivity()).showSnackbar("Totale eliminato!");
                                                } else if (MainActivity.PURCHASELIST.get(position).getType() != 3) {
                                                    for (int i = position - 1; i>=0; i--) {
                                                        if (MainActivity.PURCHASELIST.get(i).getType() == 0) {
                                                            totPosition = i;
                                                            FirebaseFirestore fStore = FirebaseFirestore.getInstance();
                                                            fStore.collection("purchases").document(MainActivity.PURCHASEIDLIST.get(i))
                                                                    .set(MainActivity.PURCHASELIST.get(i)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    MainActivity.PURCHASELIST.get(totPosition).setPrice(MainActivity.PURCHASELIST.get(totPosition)
                                                                            .getPrice() - MainActivity.PURCHASELIST.get(position).getPrice());
                                                                    MainActivity.PURCHASEIDLIST.remove(position);
                                                                    MainActivity.PURCHASELIST.remove(position);
                                                                    mRecyclerView.removeViewAt(position);
                                                                    mRecyclerView.getAdapter().notifyItemRemoved(position);
                                                                    mRecyclerView.getAdapter().notifyItemRangeChanged(position, MainActivity.PURCHASELIST.size());
                                                                    mRecyclerView.getAdapter().notifyItemChanged(totPosition);
                                                                    ((MainActivity)getActivity()).showSnackbar("Acquisto eliminato!");
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.e("LOG", "Error! " + e.getLocalizedMessage());
                                                                    ((MainActivity)getActivity()).showSnackbar("Acquisto non eliminato correttamente!");
                                                                }
                                                            });
                                                            break;
                                                        }
                                                    }
                                                } else {
                                                    MainActivity.PURCHASEIDLIST.remove(position);
                                                    MainActivity.PURCHASELIST.remove(position);
                                                    mRecyclerView.removeViewAt(position);
                                                    mRecyclerView.getAdapter().notifyItemRemoved(position);
                                                    mRecyclerView.getAdapter().notifyItemRangeChanged(position, MainActivity.PURCHASELIST.size());
                                                    ((MainActivity)getActivity()).showSnackbar("Acquisto eliminato!");
                                                }
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
            } else {
                holder.itemLayout.setOnLongClickListener(null);
                holder.itemLayout.setEnabled(false);
            }
        }

        @Override
        public int getItemCount() {
            return MainActivity.PURCHASELIST.size();
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
