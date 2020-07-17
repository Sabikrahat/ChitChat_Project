package com.example.teamworkchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ApplicationActivity extends AppCompatActivity {

    private final String websiteLink = "https://sites.google.com/view/sabikrahat";

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private DatabaseReference databaseReference, reference;

    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(" ChitChat");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Firebase Auth User
        mUser = mAuth.getCurrentUser();

        tabLayout = findViewById(R.id.applicationActivityTabLayoutID);
        viewPager = findViewById(R.id.applicationActivityViewPagerID);

        //chat number
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ViewPageAdapter viewPageAdapter = new ViewPageAdapter(getSupportFragmentManager());

                int unread = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MessageClass messageClass = snapshot.getValue(MessageClass.class);
                    if (messageClass.getReceiver().equals(mUser.getUid()) && !messageClass.getIsSeen()) {
                        unread++;
                    }
                }

                if (unread == 0) {
                    viewPageAdapter.addFragment(new ChatFragment(), "Chats");
                } else {
                    viewPageAdapter.addFragment(new ChatFragment(), "(" + unread + ") " + "Chats");
                }

                //viewPageAdapter.addFragment(new GroupFragment(), "Groups");
                viewPageAdapter.addFragment(new UserFragment(), "Users");
                //viewPageAdapter.addFragment(new MeetingFragment(), "Meetings");

                viewPager.setAdapter(viewPageAdapter);

                tabLayout.setupWithViewPager(viewPager);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ApplicationActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    class ViewPageAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        ViewPageAdapter(FragmentManager fm) {
            super(fm);

            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_application_layout, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.menuMyProfileID) {
            Intent intent = new Intent(ApplicationActivity.this, MyProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } /*else if (item.getItemId() == R.id.menuSignOutID) {
            FirebaseAuth.getInstance().signOut();
            finish();
            Toast.makeText(getApplicationContext(), "Sign out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getApplicationContext(), SigninActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }*/ else if (item.getItemId() == R.id.menuShareID) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            String subject = "ChitChat Application's Invitation";
            String body = "This is my first application, by which you can notify your team members for a meeting very easily. You can easily get the application from my website using the link below: \n\n" + websiteLink;
            intent.putExtra(intent.EXTRA_SUBJECT, subject);
            intent.putExtra(intent.EXTRA_TEXT, body);
            startActivity(Intent.createChooser(intent, " Sharing is Caring "));
        } else if (item.getItemId() == R.id.menuFeedbackID) {
            Intent intent = new Intent(ApplicationActivity.this, FeedbackActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if (item.getItemId() == R.id.menuAboutMeID) {
            Intent intent = new Intent(ApplicationActivity.this, AboutMeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void alertMessege() {
        AlertDialog.Builder alertdialogbuilder = new AlertDialog.Builder(ApplicationActivity.this);

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

    private void status(String status) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(mUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        databaseReference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }
}