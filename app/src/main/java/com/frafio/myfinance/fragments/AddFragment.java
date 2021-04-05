package com.frafio.myfinance.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.frafio.myfinance.MainActivity;
import com.frafio.myfinance.R;
import com.frafio.myfinance.objects.Purchase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class AddFragment extends Fragment {

    EditText mNameET, mPriceET;
    ConstraintLayout mDateBtn;
    TextView mDateET;
    MaterialButton mTypeBtn, mAddBtn;

    int year, month, day;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);

        // collegamento view
        mNameET = view.findViewById(R.id.add_name_EditText);
        mPriceET = view.findViewById(R.id.add_price_EditText);
        mDateBtn = view.findViewById(R.id.add_dateLayout);
        mDateET = view.findViewById(R.id.add_dateTextView);
        mTypeBtn = view.findViewById(R.id.add_typeButton);
        mAddBtn = view.findViewById(R.id.add_addButton);

        setDatePicker();

        mTypeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mTypeBtn.getText().toString().trim()) {
                    case "Generico":
                        mTypeBtn.setText("Spesa");
                        break;
                    case "Spesa":
                        mTypeBtn.setText("Biglietto");
                        break;
                    case "Biglietto":
                        mTypeBtn.setText("Generico");
                        break;
                }
            }
        });

        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPurchase();
            }
        });
        return view;
    }

    private void setDatePicker() {
        // set data odierna
        year = LocalDate.now().getYear();
        month = LocalDate.now().getMonthValue();
        day = LocalDate.now().getDayOfMonth();

        String dayString, monthString;
        if (day < 10) {
            dayString = "0" + day;
        } else {
            dayString = day + "";
        }
        if (month < 10) {
            monthString = "0" + month;
        } else {
            monthString = month + "";
        }
        mDateET.setText(dayString + "/" + monthString + "/" + year);

        // date picker
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.clear();
        final long today = MaterialDatePicker.todayInUtcMilliseconds();
        MaterialDatePicker.Builder builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Seleziona una data");
        builder.setSelection(today);
        builder.setTheme(R.style.ThemeOverlay_MyFinance_DatePicker);
        final MaterialDatePicker materialDatePicker = builder.build();

        mDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(materialDatePicker);
            }
        });
    }

    private void showDatePicker(MaterialDatePicker materialDatePicker) {
        if (!materialDatePicker.isAdded()) {
            materialDatePicker.show(getActivity().getSupportFragmentManager(), "DATE_PICKER");
            materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
                @Override
                public void onPositiveButtonClick(Object selection) {
                    // get selected date
                    Date date = new Date(Long.parseLong(selection.toString()));
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    year = calendar.get(Calendar.YEAR);
                    month = calendar.get(Calendar.MONTH) + 1;
                    day = calendar.get(Calendar.DAY_OF_MONTH);
                    String dayString, monthString;
                    if (day < 10) {
                        dayString = "0" + day;
                    } else {
                        dayString = day + "";
                    }
                    if (month < 10) {
                        monthString = "0" + month;
                    } else {
                        monthString = month + "";
                    }
                    mDateET.setText(dayString + "/" + monthString + "/" + year);
                }
            });
        }
    }

    private void addPurchase() {
        String name = mNameET.getText().toString().trim();
        String type = mTypeBtn.getText().toString().trim();
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
        double price = Double.parseDouble(priceString);

        Purchase purchase = new Purchase(name, type, price, year, month, day, 1);

        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        fStore.collection("purchases").add(purchase)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                MainActivity.PURCHASELIST.add(purchase);
                int sum = 0;
                for (Purchase item : MainActivity.PURCHASELIST) {
                    if (item.getYear() == year && item.getMonth() == month && item.getDay() == day && item.getNum() == 1) {
                        sum += item.getPrice();
                    }
                }
                Purchase totalP = new Purchase("Totale", "Generico", sum, year, month, day, 0);
                FirebaseFirestore fStore = FirebaseFirestore.getInstance();
                fStore.collection("purchases").add(totalP)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        getActivity().onBackPressed();
                        ((MainActivity)getActivity()).showSnackbar("Acquisto aggiunto!");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("LOG", "Error! " + e.getLocalizedMessage());
                        ((MainActivity)getActivity()).showSnackbar("Acquisto non aggiunto correttamente!");
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("LOG", "Error! " + e.getLocalizedMessage());
                ((MainActivity)getActivity()).showSnackbar("Acquisto non aggiunto!");
            }
        });
    }
}
