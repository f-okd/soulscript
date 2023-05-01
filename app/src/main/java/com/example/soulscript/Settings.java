package com.example.soulscript;

// Import necessary classes:
// The code imports several classes from the Android SDK and Firebase Authentication libraries.
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Settings extends AppCompatActivity {
    // Declare variables:
    // The code declares several variables to represent UI elements and Firebase authentication objects.
    private Button buttonLogout, buttonChangePassword;
    private TextInputEditText editTextNewPassword, editTextConfirmNewPassword;
    private TextView textViewUserDetails;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize Firebase authentication objects:
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Initialize UI elements:
        // The code initializes the UI elements by finding their references using their unique IDs, which are defined in an XML file.
        buttonLogout = findViewById(R.id.logout);
        buttonChangePassword = findViewById(R.id.change_password_button);
        editTextNewPassword = findViewById(R.id.new_password);
        editTextConfirmNewPassword = findViewById(R.id.confirm_new_password);
        textViewUserDetails = findViewById(R.id.user_details);
        radioGroup = findViewById(R.id.radio_group);

        // Check if user is logged in:
        // The code checks if a user is logged in. If not, the user is redirected to the login activity.
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            textViewUserDetails.setText(user.getEmail());
        }


        /* The code initializes the change password button click listener:
        * When the button is clicked, the code retrieves the new password and confirms the new password from the text fields.
        * If the passwords match, and is strong, the code calls the updatePassword() method to update the user's password. */
        buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String password, confirmPassword;
                password = editTextNewPassword.getText().toString(); // Updated to use getText().toString()
                confirmPassword = editTextConfirmNewPassword.getText().toString(); // Updated to use getText().toString()

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Settings.this, "Please enter password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(Settings.this, "Passwords do not match", Toast.LENGTH_SHORT).show(); // Updated the error message
                    return;
                }
                if (!isStrongPassword(password)) {
                    Toast.makeText(Settings.this, " Password needs at least 8 characters, 1 uppercase letter, 1 lowercase letter, 1 digit, and 1 special character", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(confirmPassword)) {
                    Toast.makeText(Settings.this, "Please confirm password", Toast.LENGTH_SHORT).show();
                    return;
                }

                updatePassword(password); // Updated to pass the new password
            }
        });

        // The code initializes the logout button click listener:
        // When the button is clicked, the code calls the signOut() method to sign out the user.
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

    }

    // The code defines the updatePassword() method:
    // The code calls the updatePassword() method on the user object, which is retrieved from the FirebaseAuth instance.
    private void updatePassword(String newPassword) {
        FirebaseUser user = auth.getCurrentUser();
        https://firebase.google.com/docs/auth/web/manage-users#set_a_users_password
        user.updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Settings.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Settings.this, "Error updating password", Toast.LENGTH_SHORT).show();
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
