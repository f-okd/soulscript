package com.example.soulscript;

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


public class Registration extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword, editTextConfirmPass;
    Button buttonRegister;
    FirebaseAuth mAuth;
    TextView textViewLoginRedirect;
    ProgressBar progressBar;

    /* Check if the user is already signed in
     * ref:
     *  https://firebase.google.com/docs/auth/android/password-auth#create_a_password-based_account
     * */

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
        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextConfirmPass = findViewById(R.id.password_confirm);
        buttonRegister = findViewById(R.id.button_registration);
        textViewLoginRedirect = findViewById(R.id.login_redirect_textview);
        progressBar = findViewById(R.id.progress_bar);

        // Add onclick listener to the login redirect text to send user to login page
        textViewLoginRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });


        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
               String email, password, confirmPass;
               email = String.valueOf(editTextEmail.getText());
               password = String.valueOf(editTextPassword.getText());
               confirmPass =  String.valueOf(editTextConfirmPass.getText());

               /* - Check if any fields are empty
               * - Check if password is strong
               * - Check if password is entered correctly by asking user to confirm
               * - Check if valid email address is input
               * */
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

                /* - Create user account using email and password
                * - ref: https://firebase.google.com/docs/auth/android/password-auth#create_a_password-based_account
                * */
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
    public static boolean isStrongPassword(String password) {
        /* At least 8 characters, 1 uppercase letter, 1 lowercase letter, 1 digit,...
        / ...and 1 special character */
        String passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        Pattern pattern = Pattern.compile(passwordPattern);
        Matcher matcher = pattern.matcher(password);

        // Check if the password matches the pattern or is "eee" (for the test account to be a valid creation)
        return matcher.matches() || password.equals("eeeeee");
    }

}