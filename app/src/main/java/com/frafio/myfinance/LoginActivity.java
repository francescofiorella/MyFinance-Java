package com.frafio.myfinance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.frafio.myfinance.objects.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {

    // definizione variabili
    RelativeLayout layout;
    Typeface nunito;

    MaterialToolbar mToolbar;
    TextInputLayout mEmailLayout, mPasswordLayout;
    TextInputEditText mEmail, mPassword;
    MaterialButton mLoginBtn, mGoogleBtn;
    TextView mResetBtn, mSignupBtn;
    LinearProgressIndicator mProgressIndicator;

    // firebase
    FirebaseAuth fAuth;

    // login Google
    GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 101;

    static private final String TAG = LoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        fAuth = FirebaseAuth.getInstance();
        if (fAuth.getCurrentUser() != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        nunito = ResourcesCompat.getFont(getApplicationContext(), R.font.nunito);
        layout = findViewById(R.id.login_layout);

        // toolbar
        mToolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);

        // collegamento view
        mEmail = findViewById(R.id.login_nameInputText);
        mPassword = findViewById(R.id.login_passwordInputText);

        mEmailLayout = findViewById(R.id.login_emailInputLayout);
        mPasswordLayout = findViewById(R.id.login_passwordInputLayout);

        mLoginBtn = findViewById(R.id.loginButton);
        mResetBtn = findViewById(R.id.resetPassTextView);
        mGoogleBtn = findViewById(R.id.googleButton);
        mProgressIndicator = findViewById(R.id.login_progressIindicator);
        mSignupBtn = findViewById(R.id.lRegisterTextView);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                // controlla le info aggiunte
                if (TextUtils.isEmpty(email)) {
                    mEmailLayout.setError("Inserisci la tua email.");
                    return;
                } else {
                    mEmailLayout.setErrorEnabled(false);
                }

                if (TextUtils.isEmpty(password)) {
                    mPasswordLayout.setError("Inserisci la password.");
                    return;
                } else {
                    mPasswordLayout.setErrorEnabled(false);
                }

                if (password.length() < 8) {
                    mPasswordLayout.setError("La password deve essere lunga almeno 8 caratteri!");
                    return;
                } else {
                    mPasswordLayout.setErrorEnabled(false);
                }

                mProgressIndicator.show();

                // authenticate the user
                fAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("com.frafio.myfinance.userRequest", true);
                        startActivity(intent);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error! " + e.getLocalizedMessage());
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            String errorCode = ((FirebaseAuthInvalidCredentialsException) e).getErrorCode();

                            if (errorCode.equals("ERROR_INVALID_EMAIL")) {
                                mEmailLayout.setError("L'email inserita non è ben formata.");
                            } else if (errorCode.equals("ERROR_WRONG_PASSWORD")){
                                mPasswordLayout.setError("La password inserita non è corretta.");
                            }
                        } else if (e instanceof FirebaseAuthInvalidUserException) {
                            String errorCode = ((FirebaseAuthInvalidUserException) e).getErrorCode();

                            if (errorCode.equals("ERROR_USER_NOT_FOUND")) {
                                mEmailLayout.setError("L'email inserita non ha un account associato.");
                            } else if (errorCode.equals("ERROR_USER_DISABLED")) {
                                mEmailLayout.setError("Il tuo account è stato disabilitato.");
                            } else {
                                showSnackbar(e.getLocalizedMessage());
                            }
                        }  else {
                            showSnackbar("Accesso fallito.");
                        }
                        mProgressIndicator.hide();
                    }
                });
            }
        });

        // Configure Google Sign In
        createRequest();
        mGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressIndicator.show();
                signIn();
            }
        });

        // reset password tramite email
        mResetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    mEmailLayout.setError("Inserisci la tua email.");
                    return;
                }

                fAuth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        showSnackbar("Email inviata. Controlla la tua posta!");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error! " + e.getLocalizedMessage());
                        if (e instanceof FirebaseTooManyRequestsException) {
                            showSnackbar("Email non inviata! Sono state effettuate troppe richieste.");
                        } else {
                            showSnackbar("Errore! Email non inviata.");
                        }
                    }
                });
            }
        });

        mSignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getApplicationContext(), SignupActivity.class), 1);
            }
        });
    }

    // Configure Google Sign In
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void createRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        fAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    // crea utente in Firestore se non esiste
                    createFirestoreUser();
                } else {
                    // If sign in fails, display a message to the user.
                    showSnackbar("Accesso con Google fallito.");
                    Log.e(TAG, "Error! " + task.getException().getLocalizedMessage());
                    mProgressIndicator.hide();
                }
            }
        });
    }

    // se accedi con Google crea l'utente in firestore (se non è già presente)
    private void createFirestoreUser() {
        FirebaseUser fUser = fAuth.getCurrentUser();
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        fStore.collection("users").whereEqualTo("email", fUser.getEmail()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.isEmpty()) {
                    User user = new User(fUser.getDisplayName(), fUser.getEmail(), fUser.getPhotoUrl().toString());
                    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
                    fStore.collection("users").document(fUser.getUid()).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            MainActivity.CURRENTUSER = user;
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.putExtra("com.frafio.myfinance.userRequest", true);
                            startActivity(intent);
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            showSnackbar("Accesso con Google non effettuato correttamente.");
                            Log.e(TAG, "Error! " + e.getLocalizedMessage());
                            mProgressIndicator.hide();
                            fAuth.signOut();
                        }
                    });
                } else {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("com.frafio.myfinance.userRequest", true);
                    startActivity(intent);
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showSnackbar("Accesso con Google non effettuato correttamente.");
                Log.e(TAG, "Error! " + e.getLocalizedMessage());
                mProgressIndicator.hide();
                fAuth.signOut();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.e(TAG, "Error! " + e.getLocalizedMessage());
                showSnackbar("Accesso con Google fallito.");
                mProgressIndicator.hide();
            }
        } else if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("com.frafio.myfinance.userRequest", true);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void showSnackbar(String string) {
        Snackbar snackbar = Snackbar.make(layout, string, BaseTransientBottomBar.LENGTH_SHORT)
                .setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.snackbar))
                .setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.inverted_primary_text));
        TextView tv = (snackbar.getView()).findViewById((R.id.snackbar_text));
        tv.setTypeface(nunito);
        snackbar.show();
    }
}