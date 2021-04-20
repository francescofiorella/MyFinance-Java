package com.frafio.myfinance.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.frafio.myfinance.BuildConfig;
import com.frafio.myfinance.LoginActivity;
import com.frafio.myfinance.MainActivity;
import com.frafio.myfinance.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class MenuFragment extends Fragment {

    MaterialButton mLogoutBtn;
    TextView mAppVersionTV;

    static private final String TAG = MenuFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        mLogoutBtn = view.findViewById(R.id.menu_logoutBtn);
        mAppVersionTV = view.findViewById(R.id.menu_appVersion_TV);

        mLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.CURRENT_USER = null;
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getContext(), LoginActivity.class));
                getActivity().finish();
            }
        });

        mAppVersionTV.setText("MyFinance " + BuildConfig.VERSION_NAME);

        return view;
    }
}
