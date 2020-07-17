package com.example.teamworkchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;

public class AboutMeActivity extends AppCompatActivity {

    private WebView webView;
    private ImageButton gmailButton, githubButton, personalWebsiteButton, facebookButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_me);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(" About me");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        webView = findViewById(R.id.aboutMeActivityWebViewID);
        gmailButton = findViewById(R.id.aboutMeActivityGmailImageButtonID);
        githubButton = findViewById(R.id.aboutMeActivityGithubImageButtonID);
        personalWebsiteButton = findViewById(R.id.aboutMeActivityPersonalWebsiteImageButtonID);
        facebookButton = findViewById(R.id.aboutMeActivityFacebookImageButtonID);
        progressBar = findViewById(R.id.aboutMeActivityProgressBarID);

        gmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/email");
                String subject = "Teamwork Notifier Application's Queries";
                String body = "";
                intent.putExtra(intent.EXTRA_SUBJECT, subject);
                intent.putExtra(intent.EXTRA_TEXT, body);
                intent.putExtra(intent.EXTRA_EMAIL, new String[]{"sabikrahat72428@gmail.com", "2019-1-60-256@std.ewubd.edu"});
                startActivity(Intent.createChooser(intent, " Queries "));
                progressBar.setVisibility(View.GONE);
            }
        });

        githubButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                WebSettings webSettings = webView.getSettings();
                webSettings.setJavaScriptEnabled(true);
                webView.setWebViewClient(new WebViewClient());
                webView.loadUrl("https://github.com/Sabikrahat");
                webView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        });

        personalWebsiteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                WebSettings webSettings = webView.getSettings();
                webSettings.setJavaScriptEnabled(true);
                webView.setWebViewClient(new WebViewClient());
                webView.loadUrl("https://sites.google.com/view/sabikrahat");
                webView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        });

        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                WebSettings webSettings = webView.getSettings();
                webSettings.setJavaScriptEnabled(true);
                webView.setWebViewClient(new WebViewClient());
                webView.loadUrl("https://www.facebook.com/sabikrahat72428");
                webView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            if (webView.getVisibility() == View.VISIBLE) {
                webView.setVisibility(View.GONE);
            } else {
                super.onBackPressed();
            }
        }
    }
}