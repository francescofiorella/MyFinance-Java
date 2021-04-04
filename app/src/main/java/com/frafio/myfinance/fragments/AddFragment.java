package com.frafio.myfinance.fragments;

import android.os.Bundle;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class AddFragment extends Fragment {

    EditText mNameET, mAmounthET;
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
        mAmounthET = view.findViewById(R.id.add_amounth_EditText);
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
}
