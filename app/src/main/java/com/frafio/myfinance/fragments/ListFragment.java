package com.frafio.myfinance.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
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

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.frafio.myfinance.MainActivity;
import com.frafio.myfinance.ReceiptActivity;
import com.frafio.myfinance.R;
import com.frafio.myfinance.objects.Purchase;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ListFragment extends Fragment {

    RecyclerView mRecyclerView;
    FirestoreRecyclerAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        mRecyclerView = view.findViewById(R.id.list_recyclerView);
        loadPurchasesList();

        return view;
    }

    private void loadPurchasesList() {
        PurchaseAdapter purchaseAdapter = new PurchaseAdapter(getContext(), MainActivity.PURCHASELIST, MainActivity.PURCHASEIDLIST);
        mRecyclerView.setAdapter(purchaseAdapter);
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
                holder.itemLayout.setClickable(false);
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
            } else {
                holder.nomeTV.setText("   " + purchaseList.get(position).getName());
                holder.dataLayout.setVisibility(View.GONE);
            }

            if (purchaseList.get(position).getType() == 1) {
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
            }
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
