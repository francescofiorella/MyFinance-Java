package com.frafio.myfinance.fragments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.frafio.myfinance.LoginActivity;
import com.frafio.myfinance.MainActivity;
import com.frafio.myfinance.R;
import com.frafio.myfinance.objects.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    ImageView mUserImage;
    TextView mUserNameTv, mEmailTv;

    FirebaseAuth fAuth;

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
        fAuth = FirebaseAuth.getInstance();
        FirebaseUser fUser = fAuth.getCurrentUser();

        if (fUser != null) {
            if (MainActivity.CURRENTUSER == null) {
                FirebaseFirestore fStore = FirebaseFirestore.getInstance();
                fStore.collection("users").document(fUser.getUid()).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                MainActivity.CURRENTUSER = documentSnapshot.toObject(User.class);
                                mUserNameTv.setText(MainActivity.CURRENTUSER.getFullName());
                                mEmailTv.setText(MainActivity.CURRENTUSER.getEmail());
                                if (!MainActivity.CURRENTUSER.getImage().equals("")) {
                                    Glide.with(getContext()).load(MainActivity.CURRENTUSER.getImage()).apply(RequestOptions.circleCropTransform()).into(mUserImage);
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("LOG", "Error! " + e.getLocalizedMessage());
                    }
                });
            } else {
                mUserNameTv.setText(MainActivity.CURRENTUSER.getFullName());
                mEmailTv.setText(MainActivity.CURRENTUSER.getEmail());
                if (!MainActivity.CURRENTUSER.getImage().equals("")) {
                    Glide.with(getContext()).load(MainActivity.CURRENTUSER.getImage()).apply(RequestOptions.circleCropTransform()).into(mUserImage);
                }
            }
        }
    }
}
