package com.example.soulscript;

// Import necessary classes:
// The code imports several classes from the Android SDK and Firebase Authentication libraries.
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Patterns;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.example.soulscript.CustomTitleTextView;



public class Registration extends AppCompatActivity {

    // Declare variables:
    // The code declares several variables to represent UI elements and Firebase authentication objects.
    TextInputEditText editTextEmail, editTextPassword, editTextConfirmPass;
    Button buttonRegister;
    FirebaseAuth mAuth;
    TextView textViewLoginRedirect;
    ProgressBar progressBar;

    // The code checks if a user is logged in. If so, the user is redirected to the home activity.
    // ref: https://firebase.google.com/docs/auth/android/password-auth#create_a_password-based_account
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in, if so then redirect to main activity
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), Home.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize Firebase authentication objects:
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements:
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextConfirmPass = findViewById(R.id.password_confirm);
        buttonRegister = findViewById(R.id.button_registration);
        textViewLoginRedirect = findViewById(R.id.login_redirect_textview);
        progressBar = findViewById(R.id.progress_bar);

        // Redirect the user to the login activity when the user clicks on the login redirect text view.
        textViewLoginRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        /* The code defines an onClickListener for the Register button:
        * When the user clicks on the Register button, the code checks if the following conditions are met:
        * - The email field is not empty.
        * - The password field is not empty.
        * - The password is strong.
        * - The password and confirm password fields match.
        * - The email address is valid.
        * If the conditions are met, the code creates a user account using the email and password.
        * If the account creation is successful, the user is redirected to the login activity.
        * If the account creation is unsuccessful, the user is notified of the failure. */
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
               String email, password, confirmPass;
               email = String.valueOf(editTextEmail.getText());
               password = String.valueOf(editTextPassword.getText());
               confirmPass =  String.valueOf(editTextConfirmPass.getText());

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(Registration.this, "Enter email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Registration.this, "Enter password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isStrongPassword(password)) {
                    Toast.makeText(Registration.this, " Password needs at least 8 characters, 1 uppercase letter, 1 lowercase letter, 1 digit, and 1 special character", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(confirmPass)) {
                    Toast.makeText(Registration.this, "Please confirm password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!password.equals(confirmPass)){
                    Toast.makeText(Registration.this, "Passwords must match", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(Registration.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                    return;
                }

                /* The code creates a user account using the email and password.
                * If the account creation is successful, the user is redirected to the login activity.
                * If the account creation is unsuccessful, the user is notified of the failure.
                * ref:  https://firebase.google.com/docs/auth/android/password-auth#create_a_password-based_account */
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                /* Notify account creation success and redirect to Login page, which will redirect to main activity if user is already logged in*/
                                if (task.isSuccessful()) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(Registration.this, "Account created.",
                                            Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), Login.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    // If sign in fails, display a message to the user.
                                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                        Toast.makeText(Registration.this, "The email address is already in use by another account.",
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(Registration.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });


            }
        });
    }

    /* The code defines the isStrongPassword() method:
     * The code checks if the password matches the regular expression pattern.
     * The regular expression pattern is defined as follows:
     * At least 8 characters, 1 uppercase letter, 1 lowercase letter, 1 digit, and 1 special character.
     * Allows exception of eee for the test account */
    private static boolean isStrongPassword(String password) {
        String passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        Pattern pattern = Pattern.compile(passwordPattern);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches() || password.equals("eeeeee");
    }

}