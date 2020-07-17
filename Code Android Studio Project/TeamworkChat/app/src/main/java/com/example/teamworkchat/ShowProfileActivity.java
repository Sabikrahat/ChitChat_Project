package com.example.teamworkchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.net.Uri;
import android.os.Bundle;
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

public class ShowProfileActivity extends AppCompatActivity {

    private TextView nameTextView, emailTextView, phoneNumberTextView;
    private ImageView profilePictureImageView, showOffline, showOnline;
    private ProgressBar progressBar;

    private DatabaseReference databaseReference;
    private StorageReference mStorageRef;

    String firstName, lastName, email, phoneNumber, imageUrl, status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(" User's Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        nameTextView = findViewById(R.id.showProfileActivityNameTextViewID);
        emailTextView = findViewById(R.id.showProfileActivityEmailTextViewID);
        phoneNumberTextView = findViewById(R.id.showProfileActivityPhoneNumberTextViewID);
        profilePictureImageView = findViewById(R.id.showProfileActivityImageViewID);
        showOffline = findViewById(R.id.showProfileActivityShowOfflineID);
        showOnline = findViewById(R.id.showProfileActivityShowOnlineID);
        progressBar = findViewById(R.id.showProfileActivityProgressBarID);
        progressBar.setVisibility(View.VISIBLE);

        Bundle bundle = getIntent().getExtras();
        String targetUserUid = bundle.getString("targetUserUid");

        // Initialize Firebase Storage
        mStorageRef = FirebaseStorage.getInstance().getReference().child("Users_Profile_Picture");

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(targetUserUid.toString());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                firstName = snapshot.child("firstName").getValue().toString();
                lastName = snapshot.child("lastName").getValue().toString();
                email = snapshot.child("email").getValue().toString();
                phoneNumber = snapshot.child("phoneNumber").getValue().toString();
                imageUrl = snapshot.child("imageURL").getValue().toString();
                status = snapshot.child("status").getValue().toString();
                nameTextView.setText(firstName + " " + lastName);
                emailTextView.setText(email);
                phoneNumberTextView.setText(phoneNumber);

                if (status.equals("online")) {
                    try {
                        showOffline.setVisibility(View.GONE);
                    } catch (Exception e) {
                    }
                    showOnline.setVisibility(View.VISIBLE);
                } else if (status.equals("offline")) {
                    try {
                        showOnline.setVisibility(View.GONE);
                    } catch (Exception e) {
                    }
                    showOffline.setVisibility(View.VISIBLE);
                }

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
                            Toast.makeText(ShowProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (Exception e) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ShowProfileActivity.this, "Please add a profile picture. Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ShowProfileActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });

    }
}