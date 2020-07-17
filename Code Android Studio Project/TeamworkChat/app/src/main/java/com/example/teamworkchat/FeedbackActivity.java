package com.example.teamworkchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.storage.StorageTask;

public class FeedbackActivity extends AppCompatActivity {

    private EditText editText;
    private Button button;
    private ProgressBar progressBar;
    private StorageTask storageTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(" Your Feedback");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        editText = findViewById(R.id.feedbackActivityEditTextID);
        button = findViewById(R.id.feedbackActivityButtonID);
        progressBar = findViewById(R.id.feedbackActivityProgressBarID);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (storageTask != null && storageTask.isInProgress()) {
                    Toast.makeText(getApplicationContext(), "Please wait. Task is processing...", Toast.LENGTH_LONG).show();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/email");
                    String subject = "ChitChat Application's Feedback";
                    String body = editText.getText().toString();
                    intent.putExtra(intent.EXTRA_SUBJECT, subject);
                    intent.putExtra(intent.EXTRA_TEXT, body);
                    intent.putExtra(intent.EXTRA_EMAIL, new String[]{"sabikrahat72428@gmail.com", "2019-1-60-256@std.ewubd.edu"});
                    startActivity(Intent.createChooser(intent, " Feedback "));
                    progressBar.setVisibility(View.GONE);
                    editText.setText("");
                }
            }
        });
    }
}