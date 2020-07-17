package com.example.teamworkchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class SignupActivity extends AppCompatActivity {

    private ImageView imageView;
    private EditText signupEmailEditText, signupPasswordEditText, signupConfirmPasswordEditText, signupFirstNameEditText, signupLastNameEditText, signupPhoneNumberEditText;
    private Button signupButton;
    private TextView signupTextView;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private StorageReference mStorageRef;

    //private String token;
    private String imageURL;
    private Uri imageUri;
    private static final int IMAGE_REQUEST_CODE = 1;

    private StorageTask storageTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(" Sign up Activity");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);

        imageView = findViewById(R.id.signupActivityImageViewID);
        signupEmailEditText = findViewById(R.id.signupActivityEmailEditTextID);
        signupPasswordEditText = findViewById(R.id.signupActivityPasswordEditTextID);
        signupConfirmPasswordEditText = findViewById(R.id.signupActivityConfirmPasswordEditTextID);
        signupFirstNameEditText = findViewById(R.id.signupActivityFirstNameEditTextID);
        signupLastNameEditText = findViewById(R.id.signupActivityLastNameEditTextID);
        signupPhoneNumberEditText = findViewById(R.id.signupActivityPhoneNumberEditTextID);
        signupButton = findViewById(R.id.signupActivitySignupButtonID);
        signupTextView = findViewById(R.id.signupActivitySigninTextViewID);
        progressBar = findViewById(R.id.signupActivityProgressBarID);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Firebase Storage
        mStorageRef = FirebaseStorage.getInstance().getReference().child("Users_Profile_Picture");

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open gallery
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, IMAGE_REQUEST_CODE);
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (storageTask != null && storageTask.isInProgress()) {
                    Toast.makeText(getApplicationContext(), "Please wait. Task is processing...", Toast.LENGTH_LONG).show();
                } else {
                    userRegister();
                }
            }
        });

        signupTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent intent = new Intent(SignupActivity.this, SigninActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private void userRegister() {
        final String email = signupEmailEditText.getText().toString().trim();
        String password = signupPasswordEditText.getText().toString();
        String confirmPassword = signupConfirmPasswordEditText.getText().toString();
        final String firstName = signupFirstNameEditText.getText().toString();
        final String lastName = signupLastNameEditText.getText().toString();
        final String phoneNumber = signupPhoneNumberEditText.getText().toString();

        //checking the validity of the email
        if (email.isEmpty()) {
            signupEmailEditText.setError("Enter an email address");
            signupEmailEditText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            signupEmailEditText.setError("Enter a valid email address");
            signupEmailEditText.requestFocus();
            return;
        }

        //checking the validity of the password
        if (password.isEmpty()) {
            signupPasswordEditText.setError("Enter a password");
            signupPasswordEditText.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            signupConfirmPasswordEditText.setError("Enter a password");
            signupConfirmPasswordEditText.requestFocus();
            return;
        }

        if (password.length() < 6) {
            signupPasswordEditText.setError("Minimum length of a password should be 6");
            signupPasswordEditText.requestFocus();
            signupConfirmPasswordEditText.setError("Minimum length of a password should be 6");
            signupConfirmPasswordEditText.requestFocus();
            signupPasswordEditText.setText("");
            signupConfirmPasswordEditText.setText("");
            return;
        }

        if (confirmPassword.length() < 6) {
            signupPasswordEditText.setError("Minimum length of a password should be 6");
            signupPasswordEditText.requestFocus();
            signupConfirmPasswordEditText.setError("Minimum length of a password should be 6");
            signupConfirmPasswordEditText.requestFocus();
            signupPasswordEditText.setText("");
            signupConfirmPasswordEditText.setText("");
            return;
        }

        if (!password.equals(confirmPassword)) {
            signupPasswordEditText.setError("Password doesn't match");
            signupPasswordEditText.requestFocus();
            signupConfirmPasswordEditText.setError("Password doesn't match");
            signupConfirmPasswordEditText.requestFocus();
            signupPasswordEditText.setText("");
            signupConfirmPasswordEditText.setText("");
            return;
        }

        //checking the validity of the first name
        if (firstName.isEmpty()) {
            signupFirstNameEditText.setError("Enter your first name");
            signupFirstNameEditText.requestFocus();
            return;
        }

        //checking the validity of the last name
        if (lastName.isEmpty()) {
            signupLastNameEditText.setError("Enter your last name");
            signupLastNameEditText.requestFocus();
            return;
        }

        //checking the validity of the phone number
        if (phoneNumber.isEmpty()) {
            signupPhoneNumberEditText.setError("Enter your last name");
            signupPhoneNumberEditText.requestFocus();
            return;
        }

        if (!Patterns.PHONE.matcher(phoneNumber).matches()) {
            signupPhoneNumberEditText.setError("Enter a valid phone number");
            signupPhoneNumberEditText.requestFocus();
            return;
        }

        if (phoneNumber.length() < 9 && phoneNumber.length() > 12) {
            signupPhoneNumberEditText.setError("Enter a valid phone number");
            signupPhoneNumberEditText.requestFocus();
            return;
        }
        if (imageUri == null) {
            Toast.makeText(this, "Please insert a picture.", Toast.LENGTH_LONG).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information

                    imageURL = mAuth.getCurrentUser().getUid() + "." + getFileExtenstion(imageUri);

                    //Image upload to firebaseStorage
                    try {
                        final StorageReference fileRef = mStorageRef.child(imageURL);
                        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                signupEmailEditText.setText("");
                                signupPasswordEditText.setText("");
                                signupConfirmPasswordEditText.setText("");
                                signupFirstNameEditText.setText("");
                                signupLastNameEditText.setText("");
                                signupPhoneNumberEditText.setText("");

                                UserClass userClass = new UserClass(mAuth.getCurrentUser().getUid(), email, firstName, lastName, phoneNumber, imageURL, "offline");
                                databaseReference.child(mAuth.getCurrentUser().getUid()).setValue(userClass);

                                //send verification email
                                FirebaseUser mUser = mAuth.getCurrentUser();
                                mUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(getApplicationContext(), "Register completed. Verification email has been sent.", Toast.LENGTH_LONG).show();
                                        finish();
                                        Intent intent = new Intent(SignupActivity.this, SigninActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(SignupActivity.this, "Email not sent. " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SignupActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (Exception e) {
                        Toast.makeText(SignupActivity.this, "You didn't set any picture.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SignupActivity.this, "User is already Registered.", Toast.LENGTH_LONG).show();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SignupActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Picasso.with(this).load(imageUri).into(imageView);
        }
    }

    //for geeting the email extention
    public String getFileExtenstion(Uri imageUri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri));
    }

    private void alertMessege() {
        AlertDialog.Builder alertdialogbuilder = new AlertDialog.Builder(SignupActivity.this);

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