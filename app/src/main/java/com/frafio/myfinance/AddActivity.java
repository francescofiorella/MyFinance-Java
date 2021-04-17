package com.frafio.myfinance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.AutoTransition;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cottacush.android.currencyedittext.CurrencyEditText;
import com.frafio.myfinance.objects.Purchase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.TimeZone;

public class AddActivity extends AppCompatActivity {

    RelativeLayout layout;
    Typeface nunito;

    MaterialToolbar mToolbar;
    EditText mNameET;
    CurrencyEditText mPriceET;
    ConstraintLayout mDateBtn;
    GridLayout mTypeLayout;
    GridLayout mBigliettoLayout;
    TextView mDateET, mGenBtn, mSpeBtn, mBigBtn, mTIBtn, mAmBtn, mAltroBtn;
    ImageView mDateArrowImg;
    SwitchMaterial mTotSwitch;
    ExtendedFloatingActionButton mAddBtn;

    int requestCode;
    String purchaseId, purchaseName;
    double purchasePrice;
    int purchaseType, purchasePosition;

    int year, month, day;

    OvershootInterpolator interpolator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        nunito = ResourcesCompat.getFont(getApplicationContext(), R.font.nunito);

        // toolbar
        mToolbar = findViewById(R.id.add_toolbar);
        setSupportActionBar(mToolbar);

        // back arrow
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // collegamento view
        layout = findViewById(R.id.add_layout);
        mNameET = findViewById(R.id.add_name_EditText);
        mPriceET = findViewById(R.id.add_price_EditText);
        mDateBtn = findViewById(R.id.add_dateLayout);
        mDateET = findViewById(R.id.add_dateTextView);
        mDateArrowImg = findViewById(R.id.add_dateArrowImageView);
        mTotSwitch = findViewById(R.id.add_totale_switch);
        mTypeLayout = findViewById(R.id.add_typeLayout);
        mGenBtn = findViewById(R.id.add_generico_tv);
        mSpeBtn = findViewById(R.id.add_spesa_tv);
        mBigBtn = findViewById(R.id.add_biglietto_tv);
        mBigliettoLayout = findViewById(R.id.add_bigliettoLayout);
        mTIBtn = findViewById(R.id.add_trenitalia_tv);
        mAmBtn = findViewById(R.id.add_amtab_tv);
        mAltroBtn = findViewById(R.id.add_altro_tv);
        mAddBtn = findViewById(R.id.add_addButton);

        interpolator = new OvershootInterpolator();
        mBigliettoLayout.setAlpha(0f);

        // stabilisci se bisogna creare un nuovo evento (1) o modificarne uno esistente (2)
        Intent intent = getIntent();
        requestCode = intent.getIntExtra("com.frafio.myfinance.REQUESTCODE", 0);

        if (requestCode == 1) {
            // set data odierna
            year = LocalDate.now().getYear();
            month = LocalDate.now().getMonthValue();
            day = LocalDate.now().getDayOfMonth();

            mGenBtn.setSelected(true);

            setTypeButton();
            setTotSwitch();

            setDatePicker();
        } else if (requestCode == 2) {
            mTotSwitch.setVisibility(View.GONE);

            purchaseId = intent.getStringExtra("com.frafio.myfinance.PURCHASE_ID");
            purchaseName = intent.getStringExtra("com.frafio.myfinance.PURCHASE_NAME");
            purchasePrice = intent.getDoubleExtra("com.frafio.myfinance.PURCHASE_PRICE", 0);
            purchaseType = intent.getIntExtra("com.frafio.myfinance.PURCHASE_TYPE", 0);
            purchasePosition = intent.getIntExtra("com.frafio.myfinance.PURCHASE_POSITION", 0);
            year = intent.getIntExtra("com.frafio.myfinance.PURCHASE_YEAR", 0);
            month = intent.getIntExtra("com.frafio.myfinance.PURCHASE_MONTH", 0);
            day = intent.getIntExtra("com.frafio.myfinance.PURCHASE_DAY", 0);

            mNameET.setText(purchaseName);

            Locale locale = new Locale("en", "UK");
            NumberFormat nf = NumberFormat.getInstance(locale);
            DecimalFormat formatter = (DecimalFormat) nf;
            formatter.applyPattern("###,###,##0.00");
            mPriceET.setText("€ " + formatter.format(purchasePrice));

            switch (purchaseType) {
                case 1:
                    mGenBtn.setEnabled(false);
                    mSpeBtn.setSelected(true);
                    mBigBtn.setEnabled(false);
                    break;
                case 2:
                    mGenBtn.setSelected(true);
                    mSpeBtn.setEnabled(false);
                    mBigBtn.setEnabled(false);
                    break;
                case 3:
                    mGenBtn.setEnabled(false);
                    mSpeBtn.setEnabled(false);
                    mBigBtn.setSelected(true);

                    setBigliettoLayout();
                    break;
            }

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
            mDateET.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.disabled_text));
            mDateBtn.setClickable(false);
            mDateArrowImg.setVisibility(View.GONE);

            mAddBtn.setText("Modifica");
            mAddBtn.setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_create));
        }

        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPurchase();
            }
        });
    }

    private void setDatePicker() {
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
            materialDatePicker.show(getSupportFragmentManager(), "DATE_PICKER");
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
                    closeTicketBtn();

                    if (mBigBtn.isSelected()) {
                        mNameET.setText("");
                    }
                    mNameET.setEnabled(true);

                    mGenBtn.setSelected(true);
                    mSpeBtn.setSelected(false);
                    mBigBtn.setSelected(false);
                }
            }
        });

        mSpeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mSpeBtn.isSelected()) {
                    closeTicketBtn();

                    if (mBigBtn.isSelected()) {
                        mNameET.setText("");
                    }
                    mNameET.setEnabled(true);

                    mGenBtn.setSelected(false);
                    mSpeBtn.setSelected(true);
                    mBigBtn.setSelected(false);
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

            if (requestCode == 2) {
                if (purchaseName.equals("Biglietto TrenItalia")) {
                    mTIBtn.performClick();
                } else if (purchaseName.equals("Biglietto Amtab")) {
                    mAmBtn.performClick();
                } else {
                    mAltroBtn.performClick();
                }
            } else {
                mTIBtn.performClick();
            }
        } else {
            closeTicketBtn();
        }
    }

    private void openTicketBtn() {
        if (mBigliettoLayout.getVisibility() == View.GONE) {
            mBigliettoLayout.animate().setInterpolator(interpolator).alpha(1f).setDuration(1500).start();
            mBigliettoLayout.setVisibility(View.VISIBLE);

            ViewGroup root = (ViewGroup) layout;
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

            ViewGroup root = (ViewGroup) layout;
            android.transition.TransitionManager.beginDelayedTransition(root);
            AutoTransition transition = new AutoTransition();
            transition.setDuration(2000);
            android.transition.TransitionManager.beginDelayedTransition(root, transition);
        }
    }

    private void setTotSwitch() {
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
                            updateAndGoToList();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("LOG", "Error! " + e.getLocalizedMessage());
                    showSnackbar("Totale non aggiunto!");
                }
            });
        } else {
            double price = mPriceET.getNumericValue();
            String priceString = mPriceET.getText().toString().trim();
            if (TextUtils.isEmpty(priceString)) {
                mPriceET.setError("Inserisci il costo dell'acquisto.");
                return;
            }

            if (requestCode == 1) {
                int type;
                if (mGenBtn.isSelected()) {
                    type = 2;
                } else if (mSpeBtn.isSelected()) {
                    type = 1;
                } else {
                    type = 3;
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
                                            && item.getType() != 0 && item.getType() != 3 && item.getYear() == purchase.getYear()
                                            && item.getMonth() == purchase.getMonth() && item.getDay() == purchase.getDay()) {
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
                                                updateAndGoToList();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("LOG", "Error! " + e.getLocalizedMessage());
                                        showSnackbar("Acquisto non aggiunto correttamente!");
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("LOG", "Error! " + e.getLocalizedMessage());
                        showSnackbar("Acquisto non aggiunto!");
                    }
                });
            } else if (requestCode == 2) {
                Purchase purchase = new Purchase(MainActivity.CURRENTUSER.getEmail(), name, price, year, month, day, purchaseType);

                FirebaseFirestore fStore = FirebaseFirestore.getInstance();
                fStore.collection("purchases").document(purchaseId).set(purchase)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        MainActivity.PURCHASELIST.set(purchasePosition, purchase);
                        if (price != purchasePrice) {
                            double sum = 0;
                            for (Purchase item : MainActivity.PURCHASELIST) {
                                if (item.getEmail().equals(MainActivity.CURRENTUSER.getEmail())
                                        && item.getType() != 0 && item.getType() != 3 && item.getYear() == purchase.getYear()
                                        && item.getMonth() == purchase.getMonth() && item.getDay() == purchase.getDay()) {
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
                                            updateAndGoToList();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("LOG", "Error! " + e.getLocalizedMessage());
                                    showSnackbar("Acquisto non aggiunto correttamente!");
                                }
                            });
                        } else {
                            // torna alla home
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("com.frafio.myfinance.purchaseRequest", true);
                            setResult(Activity.RESULT_OK, returnIntent);
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("LOG", "Error! " + e.getLocalizedMessage());
                        showSnackbar("Acquisto non modificato!");
                    }
                });
            }
        }
    }

    // metodo per aggiornare i progressi dell'utente
    public void updateAndGoToList() {
        MainActivity.PURCHASELIST = new LinkedList<>();
        MainActivity.PURCHASEIDLIST = new LinkedList<>();

        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        fStore.collection("purchases").whereEqualTo("email", MainActivity.CURRENTUSER.getEmail())
                .orderBy("year", Query.Direction.DESCENDING).orderBy("month", Query.Direction.DESCENDING)
                .orderBy("day", Query.Direction.DESCENDING).orderBy("type")
                .orderBy("price", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        int position = 0;
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            Purchase purchase = document.toObject(Purchase.class);
                            MainActivity.PURCHASEIDLIST.add(position, document.getId());
                            MainActivity.PURCHASELIST.add(position, purchase);
                            position ++;
                        }

                        // torna alla home
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("com.frafio.myfinance.purchaseRequest", true);
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
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