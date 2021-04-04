package com.frafio.myfinance.fragments;

import android.content.Intent;
import android.os.Bundle;
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
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.frafio.myfinance.MainActivity;
import com.frafio.myfinance.R;
import com.frafio.myfinance.objects.Purchase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ListFragment extends Fragment {

    RecyclerView mRecyclerView;
    FirestoreRecyclerAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        mRecyclerView = view.findViewById(R.id.list_recyclerView);
        loadLista();

        return view;
    }

    private void loadLista() {
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        // query
        Query query = fStore.collection("purchases").orderBy("year", Query.Direction.DESCENDING)
                .orderBy("month", Query.Direction.DESCENDING).orderBy("day", Query.Direction.DESCENDING)
                .orderBy("name");

        // recyclerOptions
        FirestoreRecyclerOptions<Purchase> options = new FirestoreRecyclerOptions.Builder<Purchase>().setQuery(query, Purchase.class).build();

        adapter = new FirestoreRecyclerAdapter<Purchase, ListFragment.PurchaseViewHolder>(options) {
            @NonNull
            @Override
            public ListFragment.PurchaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_recycler_view_purchase_item, parent, false);
                return new ListFragment.PurchaseViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ListFragment.PurchaseViewHolder holder, int position, @NonNull Purchase model) {
                holder.rNomeTV.setText(model.getName());
                holder.rCostoTV.setText("€" + Double.toString(model.getPrice()));
                String dayString, monthString;
                if (model.getDay() < 10) {
                    dayString = "0" + model.getDay();
                } else {
                    dayString = model.getDay() + "";
                }
                if (model.getMonth() < 10) {
                    monthString = "0" + model.getMonth();
                } else {
                    monthString = model.getMonth() + "";
                }
                holder.rDataTV.setText(dayString + "/" + monthString + "/" + model.getYear());
            }
        };

        // View Holder
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(adapter);
    }

    private class PurchaseViewHolder extends RecyclerView.ViewHolder{

        TextView rNomeTV, rCostoTV, rDataTV;
        ConstraintLayout itemLayout;

        public PurchaseViewHolder(@NonNull View itemView) {
            super(itemView);
            itemLayout = itemView.findViewById(R.id.recView_purchaseItem_constraintLayout);
            rNomeTV = itemView.findViewById(R.id.recView_purchaseItem_nomeTextView);
            rCostoTV = itemView.findViewById(R.id.recView_purchaseItem_costoTextView);
            rDataTV = itemView.findViewById(R.id.recView_purchaseItem_dataTextView);
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
}
