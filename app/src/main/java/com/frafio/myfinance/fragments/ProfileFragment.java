package com.frafio.myfinance.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.frafio.myfinance.MainActivity;
import com.frafio.myfinance.R;

public class ProfileFragment extends Fragment {

    ImageView mUserImage;
    TextView mUserNameTv, mEmailTv;

    static private final String TAG = ProfileFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // collegamento view
        mUserImage = view.findViewById(R.id.profile_propic_iv);
        mUserNameTv = view.findViewById(R.id.profile_username_tv);
        mEmailTv = view.findViewById(R.id.profile_email_tv);

        setUserData();

        return view;
    }

    private void setUserData() {
        mUserNameTv.setText(MainActivity.CURRENT_USER.getFullName());
        mEmailTv.setText(MainActivity.CURRENT_USER.getEmail());
        if (!MainActivity.CURRENT_USER.getImage().equals("")) {
            Glide.with(getContext()).load(MainActivity.CURRENT_USER.getImage()).apply(RequestOptions.circleCropTransform()).into(mUserImage);
        }
    }
}
