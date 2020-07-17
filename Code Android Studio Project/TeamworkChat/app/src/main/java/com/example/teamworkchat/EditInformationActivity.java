package com.example.teamworkchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ContentResolver;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class EditInformationActivity extends AppCompatActivity {

    private ImageView profilePictureImageView;
    private EditText firstNameEditText, lastNameEditText, phoneNumberEditText;
    private Button saveChangesButton;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private StorageReference mStorageRef;

    private StorageTask storageTask;
    private Uri imageUri;
    private static final int IMAGE_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_information);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(" Edit your Information");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Firebase Storage
        mStorageRef = FirebaseStorage.getInstance().getReference().child("Users_Profile_Picture");

        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid().toString());

        profilePictureImageView = findViewById(R.id.editInformationActivityProfilePictureImageViewID);
        firstNameEditText = findViewById(R.id.editInformationActivityFirstNameEditTextID);
        lastNameEditText = findViewById(R.id.editInformationActivityLastNameEditTextID);
        phoneNumberEditText = findViewById(R.id.editInformationActivityPhoneNumberEditTextID);
        saveChangesButton = findViewById(R.id.editInformationActivitySaveChangesButtonID);
        progressBar = findViewById(R.id.editInformationActivityProgressBarID);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.VISIBLE);
                String firstName = snapshot.child("firstName").getValue().toString();
                String lastName = snapshot.child("lastName").getValue().toString();
                String phoneNumber = snapshot.child("phoneNumber").getValue().toString();
                firstNameEditText.setText(firstName);
                lastNameEditText.setText(lastName);
                phoneNumberEditText.setText(phoneNumber);

                try {
                    //for load the dp
                    StorageReference fileRef = mStorageRef.child(snapshot.child("imageURL").getValue().toString());
                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.with(getApplicationContext()).load(uri).into(profilePictureImageView);
                            progressBar.setVisibility(View.GONE);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            profilePictureImageView.setImageResource(R.drawable.ic_launcher_background);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(EditInformationActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (Exception e) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(EditInformationActivity.this, "Please add a profile picture. Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditInformationActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });

        profilePictureImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open gallery
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, IMAGE_REQUEST_CODE);
            }
        });

        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (storageTask != null && storageTask.isInProgress()) {
                    Toast.makeText(getApplicationContext(), "Please wait. Task is processing...", Toast.LENGTH_LONG).show();
                } else {
                    String finalFirstName = firstNameEditText.getText().toString();
                    String finalLastName = lastNameEditText.getText().toString();
                    String finalPhoneNumber = phoneNumberEditText.getText().toString();

                    //checking the validity of the first name
                    if (finalFirstName.isEmpty()) {
                        firstNameEditText.setError("Enter your first name");
                        firstNameEditText.requestFocus();
                        return;
                    }

                    //checking the validity of the last name
                    if (finalLastName.isEmpty()) {
                        lastNameEditText.setError("Enter your last name");
                        lastNameEditText.requestFocus();
                        return;
                    }

                    //checking the validity of the phone number
                    if (finalPhoneNumber.isEmpty()) {
                        phoneNumberEditText.setError("Enter your last name");
                        phoneNumberEditText.requestFocus();
                        return;
                    }

                    if (!Patterns.PHONE.matcher(finalPhoneNumber).matches()) {
                        phoneNumberEditText.setError("Enter a valid phone number");
                        phoneNumberEditText.requestFocus();
                        phoneNumberEditText.setText("");
                        return;
                    }

                    if (finalPhoneNumber.length() < 9 && finalPhoneNumber.length() > 12) {
                        phoneNumberEditText.setError("Enter a valid phone number");
                        phoneNumberEditText.requestFocus();
                        phoneNumberEditText.setText("");
                        return;
                    }

                    progressBar.setVisibility(View.VISIBLE);

                    databaseReference.child("firstName").setValue(finalFirstName);
                    databaseReference.child("lastName").setValue(finalLastName);
                    databaseReference.child("phoneNumber").setValue(finalPhoneNumber);

                    if (imageUri != null) {
                        uploadImageToFirebase();
                    }

                    Toast.makeText(EditInformationActivity.this, "Information updated successfully.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    finish();
                    Intent intent = new Intent(EditInformationActivity.this, MyProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Picasso.with(this).load(imageUri).into(profilePictureImageView);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //for geeting the email extention
    public String getFileExtenstion(Uri imageUri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri));
    }

    private void uploadImageToFirebase() {
        //upload image to the firebase storage
        final StorageReference fileRef = mStorageRef.child(mAuth.getCurrentUser().getUid() + "." + getFileExtenstion(imageUri));
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid().toString());
                databaseReference.child("imageURL").setValue(mAuth.getCurrentUser().getUid() + "." + getFileExtenstion(imageUri));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditInformationActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}