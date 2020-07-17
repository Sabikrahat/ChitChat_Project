package com.example.teamworkchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.teamworkchat.Notification.APIService;
import com.example.teamworkchat.Notification.Client;
import com.example.teamworkchat.Notification.Data;
import com.example.teamworkchat.Notification.MyResponse;
import com.example.teamworkchat.Notification.Sender;
import com.example.teamworkchat.Notification.Token;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessagingActivity extends AppCompatActivity {

    private CircleImageView targetUserProfileCircleImageView, showOffline, showOnline;
    private TextView targetUserName, typedMessege;
    private ImageButton backImageButton, sendImageButton;

    FirebaseUser muser;
    DatabaseReference databaseReference, messageDatabaseReference, seenMessageDatabaseReference, reference;
    StorageReference mStorageRef;

    MessageAdapter messageAdapter;
    List<MessageClass> mMessages;

    RecyclerView recyclerView;
    ValueEventListener seenListener;

    Intent intent;
    String targetUserUid;

    APIService apiService;

    boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        /*Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(" Teamwork Chat");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });*/

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        targetUserProfileCircleImageView = findViewById(R.id.messagingActivityTargetUserImageViewID);
        showOffline = findViewById(R.id.messagingActivityShowOfflineID);
        showOnline = findViewById(R.id.messagingActivityShowOnlineID);
        targetUserName = findViewById(R.id.messagingActivityTargetUserNameTextViewID);
        typedMessege = findViewById(R.id.messagingActivityTypedMessageEditTextID);
        backImageButton = findViewById(R.id.messagingActivityBackButtonID);
        sendImageButton = findViewById(R.id.messagingActivityMessageSendButtonID);

        recyclerView = findViewById(R.id.messagingActivityRecyclerViewID);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        intent = getIntent();
        targetUserUid = intent.getStringExtra("UserUid");

        muser = FirebaseAuth.getInstance().getCurrentUser();

        targetUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent intent = new Intent(getApplicationContext(), ShowProfileActivity.class);
                intent.putExtra("targetUserUid", targetUserUid);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        targetUserProfileCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent intent = new Intent(getApplicationContext(), ShowProfileActivity.class);
                intent.putExtra("targetUserUid", targetUserUid);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        sendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                String message = typedMessege.getText().toString();
                if (!message.equals("")) {
                    sendMessege(muser.getUid(), targetUserUid, message);
                } else {
                    Toast.makeText(MessagingActivity.this, "You can't send a empty message.", Toast.LENGTH_SHORT).show();
                }
                typedMessege.setText("");
            }
        });


        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(targetUserUid);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserClass userClass = snapshot.getValue(UserClass.class);
                targetUserName.setText(userClass.getFirstName() + " " + userClass.getLastName());
                String status = userClass.getStatus().toString();

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

                //image view code
                try {
                    //for load the dp
                    // Initialize Firebase Storage
                    mStorageRef = FirebaseStorage.getInstance().getReference().child("Users_Profile_Picture");

                    StorageReference fileRef = mStorageRef.child(userClass.getImageURL().toString());
                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.with(getApplicationContext()).load(uri).into(targetUserProfileCircleImageView);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            targetUserProfileCircleImageView.setImageResource(R.drawable.ic_baseline_account_circle_24);
                        }
                    });

                } catch (Exception e) {
                    targetUserProfileCircleImageView.setImageResource(R.drawable.ic_baseline_edit_24);
                }
                readMessages(muser.getUid(), targetUserUid);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MessagingActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        seenMessage(targetUserUid);
    }

    private void seenMessage(final String userUid) {
        seenMessageDatabaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = seenMessageDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MessageClass messageClass = snapshot.getValue(MessageClass.class);
                    if (messageClass.getReceiver().equals(muser.getUid()) && messageClass.getSender().equals(userUid)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isSeen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MessagingActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessege(String sender, final String receiver, String message) {
        messageDatabaseReference = FirebaseDatabase.getInstance().getReference();

        final String msg = message;

        reference = FirebaseDatabase.getInstance().getReference("Users").child(muser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserClass userClass = dataSnapshot.getValue(UserClass.class);
                if (notify) {
                    sendNotification(receiver, userClass.getFirstName() + " " + userClass.getLastName(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MessagingActivity.this, "Error_1: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isSeen", false);

        messageDatabaseReference.child("Chats").push().setValue(hashMap);
    }

    private void sendNotification(String receiver, final String username, final String message) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(muser.getUid(), R.drawable.notification_logo, username + ": " + message, "New Message", targetUserUid);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            Toast.makeText(MessagingActivity.this, "Failed", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    Toast.makeText(MessagingActivity.this, "Error_2: " + t.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MessagingActivity.this, "Error_3: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void readMessages(final String myUid, final String userUid) {
        mMessages = new ArrayList<>();

        messageDatabaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        messageDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mMessages.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MessageClass messageClass = snapshot.getValue(MessageClass.class);
                    if (messageClass.getReceiver().equals(myUid) && messageClass.getSender().equals(userUid) ||
                            messageClass.getReceiver().equals(userUid) && messageClass.getSender().equals(myUid)) {
                        mMessages.add(messageClass);
                    }
                    messageAdapter = new MessageAdapter(MessagingActivity.this, mMessages);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MessagingActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void currentUser(String userid) {
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }

    private void status(String status) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(muser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        databaseReference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        currentUser(targetUserUid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        seenMessageDatabaseReference.removeEventListener(seenListener);
        status("offline");
        currentUser("none");
    }
}