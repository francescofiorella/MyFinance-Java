package com.frafio.myfinance.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.frafio.myfinance.MainActivity;
import com.frafio.myfinance.R;
import com.frafio.myfinance.objects.Purchase;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    TextView mDayAvgTV, mMonthAvgTV, mTodayTotTV, mTotTV, mNumTotTV, mTicketTotTV, mTrenTotTV, mAmTotTV;

    double dayAvg, monthAvg, todayTot, tot, ticketTot;
    int numTot, trenTot, amTot;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        mDayAvgTV = view.findViewById(R.id.dayAvg_TV);
        mMonthAvgTV = view.findViewById(R.id.monthAvg_TV);
        mTodayTotTV = view.findViewById(R.id.todayTot_TV);
        mTotTV = view.findViewById(R.id.tot_TV);
        mNumTotTV = view.findViewById(R.id.numTot_TV);
        mTicketTotTV = view.findViewById(R.id.ticketTot_TV);
        mTrenTotTV = view.findViewById(R.id.trenTot_TV);
        mAmTotTV = view.findViewById(R.id.amTot_TV);

        calculateStats();

        return view;
    }

    private void calculateStats() {
        tot = 0;
        numTot = 0;
        ticketTot = 0;
        trenTot = 0;
        amTot = 0;

        int nDays = 0;
        int nMonth = 0;
        int lastMonth = 0;
        int lastYear = 0;

        for (Purchase purchase : MainActivity.PURCHASELIST) {
            // totale biglietti Amtab
            if (purchase.getName().equals("Biglietto Amtab")) {
                amTot++;
            }

            if (purchase.getType() == 0) {
                // totale di oggi
                int year = LocalDate.now().getYear();
                int month = LocalDate.now().getMonthValue();
                int day = LocalDate.now().getDayOfMonth();
                if (purchase.getYear() == year && purchase.getMonth() == month && purchase.getDay() == day) {
                    todayTot = purchase.getPrice();
                }

                // incrementa il totale
                tot += purchase.getPrice();

                // conta i giorni
                nDays++;

                // conta i mesi
                if (purchase.getYear() != lastYear) {
                    lastYear = purchase.getYear();
                    lastMonth = purchase.getMonth();
                    nMonth++;
                } else if (purchase.getMonth() != lastMonth){
                    lastMonth = purchase.getMonth();
                    nMonth++;
                }
            } else if (purchase.getType() != 3){
                // totale acquisti (senza biglietti)
                numTot ++;
            } else {
                // totale biglietti
                ticketTot += purchase.getPrice();

                // totale biglietti TrenItalia
                if (purchase.getName().equals("Biglietto TrenItalia")) {
                    trenTot++;
                }
            }
        }

        dayAvg = tot/nDays;
        monthAvg = tot/nMonth;

        setViews();
    }

    private void setViews() {
        Locale locale = new Locale("en", "UK");
        NumberFormat nf = NumberFormat.getInstance(locale);
        DecimalFormat formatter = (DecimalFormat) nf;
        formatter.applyPattern("###,###,##0.00");

        mDayAvgTV.setText("€ " + formatter.format(dayAvg));
        mMonthAvgTV.setText("€ " + formatter.format(monthAvg));
        mTodayTotTV.setText("€ " + formatter.format(todayTot));
        mTotTV.setText("€ " + formatter.format(tot));
        mNumTotTV.setText(numTot + "");
        mTicketTotTV.setText("€ " + formatter.format(ticketTot));
        mTrenTotTV.setText(trenTot + "");
        mAmTotTV.setText(amTot + "");
    }
}
