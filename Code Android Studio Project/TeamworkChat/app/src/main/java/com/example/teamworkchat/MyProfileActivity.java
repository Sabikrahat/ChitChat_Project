package com.example.teamworkchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.squareup.picasso.Picasso;

public class MyProfileActivity extends AppCompatActivity {

    private TextView nameTextView, emailTextView, phoneNumberTextView;
    private ImageView profilePictureImageView;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private StorageReference mStorageRef;

    String firstName, lastName, email, phoneNumber, imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(" Your Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        nameTextView = findViewById(R.id.myProfileActivityNameTextViewID);
        emailTextView = findViewById(R.id.myProfileActivityEmailTextViewID);
        phoneNumberTextView = findViewById(R.id.myProfileActivityPhoneNumberTextViewID);
        profilePictureImageView = findViewById(R.id.myProfileActivityImageViewID);
        progressBar = findViewById(R.id.myProfileActivityProgressBarID);
        progressBar.setVisibility(View.VISIBLE);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Firebase Storage
        mStorageRef = FirebaseStorage.getInstance().getReference().child("Users_Profile_Picture");

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid().toString());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                firstName = snapshot.child("firstName").getValue().toString();
                lastName = snapshot.child("lastName").getValue().toString();
                email = snapshot.child("email").getValue().toString();
                phoneNumber = snapshot.child("phoneNumber").getValue().toString();
                imageUrl = snapshot.child("imageURL").getValue().toString();
                nameTextView.setText(firstName + " " + lastName);
                emailTextView.setText(email);
                phoneNumberTextView.setText(phoneNumber);

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
                            Toast.makeText(MyProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (Exception e) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MyProfileActivity.this, "Please add a profile picture. Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyProfileActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_my_profile_layout, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuEditInformationID) {
            finish();
            Intent intent = new Intent(MyProfileActivity.this, EditInformationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if (item.getItemId() == R.id.menuResetPasswordID) {
            finish();
            Intent intent = new Intent(MyProfileActivity.this, ResetPasswordActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if (item.getItemId() == R.id.menuSignOutFromProfileID) {
            FirebaseAuth.getInstance().signOut();
            finish();
            Toast.makeText(getApplicationContext(), "Sign out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(MyProfileActivity.this, SigninActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

}