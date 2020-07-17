package com.example.teamworkchat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context mContext;
    private List<UserClass> mUsers;
    private StorageReference mStorageRef;
    private boolean isChat;

    String theLastMessage;
    FirebaseUser mUser;
    DatabaseReference reference;


    public UserAdapter(Context mContext, List<UserClass> mUsers, boolean isChat) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.user_show_fragment_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final UserClass userClass = mUsers.get(position);
        holder.nameTextView.setText(userClass.getFirstName() + " " + userClass.getLastName());
        holder.emailTextView.setText(userClass.getEmail());
        //image view code
        try {
            //for load the dp
            // Initialize Firebase Storage
            mStorageRef = FirebaseStorage.getInstance().getReference().child("Users_Profile_Picture");

            StorageReference fileRef = mStorageRef.child(userClass.getImageURL().toString());
            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.with(mContext).load(uri).into(holder.profilePictureImageView);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    holder.profilePictureImageView.setImageResource(R.drawable.ic_baseline_account_circle_24);
                }
            });

        } catch (Exception e) {
            holder.profilePictureImageView.setImageResource(R.drawable.ic_baseline_edit_24);
        }

        if (isChat) {
            holder.emailTextView.setVisibility(View.GONE);
            holder.last_msg.setVisibility(View.VISIBLE);
            lastMessage(userClass.getUid(), holder.last_msg);
        } else {
            holder.last_msg.setVisibility(View.GONE);
            holder.emailTextView.setVisibility(View.VISIBLE);
        }

        if (isChat) {
            if (userClass.getStatus().equals("online")) {
                //holder.emailTextView.setVisibility(View.GONE);
                holder.showOnline.setVisibility(View.VISIBLE);
                holder.showOffline.setVisibility(View.GONE);
            } else {
                //holder.emailTextView.setVisibility(View.GONE);
                holder.showOnline.setVisibility(View.GONE);
                holder.showOffline.setVisibility(View.VISIBLE);
            }
        }

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MessageClass messageClass = snapshot.getValue(MessageClass.class);
                    if (messageClass.getReceiver().equals(mUser.getUid()) && messageClass.getSender().equals(userClass.getUid()) && !messageClass.getIsSeen()) {
                        holder.showUnseen.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //ActionListener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MessagingActivity.class);
                intent.putExtra("UserUid", userClass.getUid());
                mContext.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView profilePictureImageView, showOnline, showOffline, showUnseen;
        public TextView nameTextView, emailTextView, last_msg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profilePictureImageView = itemView.findViewById(R.id.userShowFragmentLayoutImageViewID);
            nameTextView = itemView.findViewById(R.id.userShowFragmentLayoutNameTextViewID);
            emailTextView = itemView.findViewById(R.id.userShowFragmentLayoutEmailTextViewID);
            showOnline = itemView.findViewById(R.id.userShowFragmentLayoutShowOnlineID);
            showOffline = itemView.findViewById(R.id.userShowFragmentLayoutShowOfflineID);
            last_msg = itemView.findViewById(R.id.userShowFragmentLayoutLastMessageTextViewID);
            showUnseen = itemView.findViewById(R.id.userShowFragmentLayoutUnseenMessageID);

        }
    }

    //check for last message
    private void lastMessage(final String userid, final TextView last_msg) {
        theLastMessage = "default";

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MessageClass messageClass = snapshot.getValue(MessageClass.class);
                    if (messageClass.getReceiver().equals(firebaseUser.getUid()) && messageClass.getSender().equals(userid) ||
                            messageClass.getReceiver().equals(userid) && messageClass.getSender().equals(firebaseUser.getUid())) {
                        theLastMessage = messageClass.getMessage();
                    }
                }

                switch (theLastMessage) {
                    case "default":
                        last_msg.setText("No Message");
                        break;

                    default:
                        last_msg.setText(theLastMessage);
                        break;
                }
                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
