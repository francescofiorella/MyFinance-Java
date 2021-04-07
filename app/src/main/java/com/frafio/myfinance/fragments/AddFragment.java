package com.frafio.myfinance.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.transition.AutoTransition;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.cottacush.android.currencyedittext.CurrencyEditText;
import com.frafio.myfinance.MainActivity;
import com.frafio.myfinance.R;
import com.frafio.myfinance.objects.Purchase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class AddFragment extends Fragment {

    EditText mNameET;
    CurrencyEditText mPriceET;
    ConstraintLayout mDateBtn, parent;
    GridLayout mBigliettoLayout;
    TextView mDateET, mGenBtn, mSpeBtn, mBigBtn, mTIBtn, mAmBtn, mAltroBtn;
    SwitchMaterial mTotSwitch;
    MaterialButton mAddBtn;

    int year, month, day;

    OvershootInterpolator interpolator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);

        // collegamento view
        parent = view.findViewById(R.id.add_layout);
        mNameET = view.findViewById(R.id.add_name_EditText);
        mPriceET = view.findViewById(R.id.add_price_EditText);
        mDateBtn = view.findViewById(R.id.add_dateLayout);
        mDateET = view.findViewById(R.id.add_dateTextView);
        mTotSwitch = view.findViewById(R.id.add_totale_switch);
        mGenBtn = view.findViewById(R.id.add_generico_tv);
        mSpeBtn = view.findViewById(R.id.add_spesa_tv);
        mBigBtn = view.findViewById(R.id.add_biglietto_tv);
        mBigliettoLayout = view.findViewById(R.id.add_bigliettoLayout);
        mTIBtn = view.findViewById(R.id.add_trenitalia_tv);
        mAmBtn = view.findViewById(R.id.add_amtab_tv);
        mAltroBtn = view.findViewById(R.id.add_altro_tv);
        mAddBtn = view.findViewById(R.id.add_addButton);

        interpolator = new OvershootInterpolator();
        mBigliettoLayout.setAlpha(0f);

        setDatePicker();

        mTotSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mNameET.setText("Totale");
                    mNameET.setEnabled(false);

                    mPriceET.setText("€ 0.00");
                    mPriceET.setEnabled(false);

                    mNameET.setError(null);
                    mPriceET.setError(null);

                    mGenBtn.setEnabled(false);
                    mSpeBtn.setEnabled(false);
                    mBigBtn.setEnabled(false);

                    closeTicketBtn();
                } else {
                    mNameET.setText("");
                    mNameET.setEnabled(true);

                    mPriceET.setText("");
                    mPriceET.setEnabled(true);

                    mGenBtn.setEnabled(true);
                    mSpeBtn.setEnabled(true);
                    mBigBtn.setEnabled(true);

                    setBigliettoLayout();
                }
            }
        });

        mGenBtn.setSelected(true);
        setTypeButton();

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

    private void setTypeButton() {
        mGenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mGenBtn.isSelected()) {
                    mGenBtn.setSelected(true);
                    mSpeBtn.setSelected(false);
                    mBigBtn.setSelected(false);

                    closeTicketBtn();

                    mNameET.setText("");
                    mNameET.setEnabled(true);
                }
            }
        });

        mSpeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mSpeBtn.isSelected()) {
                    mGenBtn.setSelected(false);
                    mSpeBtn.setSelected(true);
                    mBigBtn.setSelected(false);

                    closeTicketBtn();

                    mNameET.setText("");
                    mNameET.setEnabled(true);
                }
            }
        });

        mBigBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBigBtn.isSelected()) {
                    mGenBtn.setSelected(false);
                    mSpeBtn.setSelected(false);
                    mBigBtn.setSelected(true);
                    setBigliettoLayout();
                }
            }
        });
    }

    private void setBigliettoLayout() {
        if (mBigBtn.isSelected()) {
            openTicketBtn();

            mTIBtn.setSelected(true);
            mAmBtn.setSelected(false);
            mAltroBtn.setSelected(false);

            mNameET.setText("Biglietto TrenItalia");
            mNameET.setEnabled(false);

            mTIBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mTIBtn.isSelected()) {
                        mTIBtn.setSelected(true);
                        mAmBtn.setSelected(false);
                        mAltroBtn.setSelected(false);

                        mNameET.setText("Biglietto TrenItalia");
                        mNameET.setEnabled(false);
                    }
                }
            });

            mAmBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mAmBtn.isSelected()) {
                        mTIBtn.setSelected(false);
                        mAmBtn.setSelected(true);
                        mAltroBtn.setSelected(false);

                        mNameET.setText("Biglietto Amtab");
                        mNameET.setEnabled(false);
                    }
                }
            });

            mAltroBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mAltroBtn.isSelected()) {
                        mTIBtn.setSelected(false);
                        mAmBtn.setSelected(false);
                        mAltroBtn.setSelected(true);

                        mNameET.setText("");
                        mNameET.setEnabled(true);
                    }
                }
            });
        } else {
            closeTicketBtn();
        }
    }

    private void openTicketBtn() {
        if (mBigliettoLayout.getVisibility() == View.GONE) {
            mBigliettoLayout.animate().setInterpolator(interpolator).alpha(1f).setDuration(1500).start();
            mBigliettoLayout.setVisibility(View.VISIBLE);

            ViewGroup root = (ViewGroup) parent;
            android.transition.TransitionManager.beginDelayedTransition(root);
            AutoTransition transition = new AutoTransition();
            transition.setDuration(2000);
            android.transition.TransitionManager.beginDelayedTransition(root, transition);
        }
    }

    private void closeTicketBtn() {
        if (mBigliettoLayout.getVisibility() == View.VISIBLE) {
            mBigliettoLayout.animate().setInterpolator(interpolator).alpha(0f).setDuration(1500).start();
            mBigliettoLayout.setVisibility(View.GONE);

            ViewGroup root = (ViewGroup) parent;
            android.transition.TransitionManager.beginDelayedTransition(root);
            AutoTransition transition = new AutoTransition();
            transition.setDuration(2000);
            android.transition.TransitionManager.beginDelayedTransition(root, transition);
        }
    }

    private void addPurchase() {
        String name = mNameET.getText().toString().trim();

        // controlla le info aggiunte
        if (TextUtils.isEmpty(name)) {
            mNameET.setError("Inserisci il nome dell'acquisto.");
            return;
        }

        if (name.equals("Totale") && !mTotSwitch.isChecked()) {
            mNameET.setError("L'acquisto non può chiamarsi 'Totale'.");
            return;
        }

        if (mTotSwitch.isChecked()) {
            Purchase purchase = new Purchase(MainActivity.CURRENTUSER.getEmail(), name, 0.0, year, month, day, 0);

            FirebaseFirestore fStore = FirebaseFirestore.getInstance();
            String totID = year + "" + month + ""+ day;
            fStore.collection("purchases").document(totID).set(purchase)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            ((MainActivity)getActivity()).updateList(true);
                            ((MainActivity)getActivity()).showSnackbar("Totale aggiunto!");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("LOG", "Error! " + e.getLocalizedMessage());
                    ((MainActivity)getActivity()).showSnackbar("Totale non aggiunto!");
                }
            });
        } else {
            int type;
            if (mGenBtn.isSelected()) {
                type = 2;
            } else if (mSpeBtn.isSelected()) {
                type = 1;
            } else {
                type = 3;
            }

            double price = mPriceET.getNumericValue();
            String priceString = mPriceET.getText().toString().trim();
            if (TextUtils.isEmpty(priceString)) {
                mPriceET.setError("Inserisci il costo dell'acquisto.");
                return;
            }

            Purchase purchase = new Purchase(MainActivity.CURRENTUSER.getEmail(), name, price, year, month, day, type);

            FirebaseFirestore fStore = FirebaseFirestore.getInstance();
            fStore.collection("purchases").add(purchase)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            double sum;
                            if (purchase.getType() != 3) {
                                sum = purchase.getPrice();
                            } else {
                                sum = 0;
                            }
                            for (Purchase item : MainActivity.PURCHASELIST) {
                                if (item.getEmail().equals(MainActivity.CURRENTUSER.getEmail())
                                        && item.getType() != 0 && item.getType() != 3
                                        && item.getYear() == year && item.getMonth() == month && item.getDay() == day) {
                                    sum += item.getPrice();
                                }
                            }
                            Purchase totalP = new Purchase(MainActivity.CURRENTUSER.getEmail(), "Totale", sum, year, month, day, 0);
                            FirebaseFirestore fStore = FirebaseFirestore.getInstance();
                            String totID = year + "" + month + ""+ day;
                            fStore.collection("purchases").document(totID).set(totalP)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            ((MainActivity)getActivity()).updateList(true);
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
}
