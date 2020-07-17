package com.example.teamworkchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SigninActivity extends AppCompatActivity {

    private EditText signinEmailEditText, signinPasswordEditText;
    private Button signinButton;
    private TextView signinTextView, forgetPasswordTextView;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(" Sign in Activity");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        userIsLoggedIn();

        signinEmailEditText = findViewById(R.id.signinActivityEmailEditTextID);
        signinPasswordEditText = findViewById(R.id.signinActivityPasswordEditTextID);
        //rememberMeCheckBox = findViewById(R.id.signinActivityCheckBoxID);
        signinButton = findViewById(R.id.signinActivitySigninButtonID);
        signinTextView = findViewById(R.id.signinActivitySignupTextViewID);
        forgetPasswordTextView = findViewById(R.id.signinActivityForgetPasswordTextViewID);
        progressBar = findViewById(R.id.signinActivityProgressBarID);

        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userLogin();
            }
        });

        signinTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent intent = new Intent(SigninActivity.this, SignupActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        forgetPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent intent = new Intent(SigninActivity.this, ResetPasswordActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    //new activity's top will clear
                startActivity(intent);
            }
        });
    }

    private void userLogin() {

        final String email = signinEmailEditText.getText().toString().trim();
        final String password = signinPasswordEditText.getText().toString();

        //checking the validity of the email
        if (email.isEmpty()) {
            signinEmailEditText.setError("Enter an email address");
            signinEmailEditText.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            signinEmailEditText.setError("Enter a valid email address");
            signinEmailEditText.requestFocus();
            return;
        }

        //checking the validity of the password
        if (password.isEmpty()) {
            signinPasswordEditText.setError("Enter a password");
            signinPasswordEditText.requestFocus();
            return;
        }

        if (password.length() < 6) {
            signinPasswordEditText.setError("Minimum length of a password should be 6");
            signinPasswordEditText.requestFocus();
            signinPasswordEditText.setText("");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser mUser = mAuth.getCurrentUser();
                    if (!mUser.isEmailVerified()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SigninActivity.this, "Please verify your email.", Toast.LENGTH_LONG).show();
                    } else {
                        // Sign in success, update UI with the signed-in user's information
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "Logged in", Toast.LENGTH_SHORT).show();
                        finish();
                        Intent intent = new Intent(SigninActivity.this, ApplicationActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    //new activity's top will clear
                        startActivity(intent);
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void userIsLoggedIn() {
        FirebaseUser mUser = mAuth.getCurrentUser();
        if (mUser != null && mUser.isEmailVerified()) {
            Intent intent = new Intent(SigninActivity.this, ApplicationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    //new activity's top will clear
            startActivity(intent);
            finish();
            Toast.makeText(getApplicationContext(), "Automatically logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void alertMessege() {
        AlertDialog.Builder alertdialogbuilder = new AlertDialog.Builder(SigninActivity.this);

        //for setting title
        alertdialogbuilder.setTitle(R.string.alertTitle);

        //for setting message
        alertdialogbuilder.setMessage(R.string.alertMessege);

        //for setting icon
        alertdialogbuilder.setIcon(R.drawable.alert);

        //for setting buttons
        alertdialogbuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();   //for cancel the dialog
                finish();         //for exit the application
            }
        });

        alertdialogbuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertdialogbuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertdialogbuilder.setCancelable(false);    //for not cancel the alert dialog for unexpected click.
        AlertDialog alertdialog = alertdialogbuilder.create();
        alertdialog.show();
    }

    @Override
    public void onBackPressed() {
        alertMessege();
    }
}