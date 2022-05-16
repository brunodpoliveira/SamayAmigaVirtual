package com.internaltest.sarahchatbotmvp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class Splash extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceStare) {
        super.onCreate(savedInstanceStare);
        setContentView(R.layout.splash);

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            Intent intent = new Intent(getApplicationContext(),
                    com.internaltest.sarahchatbotmvp.MainActivity.class);
            startActivity(intent);
            finish();
        }, 2000);
    }
    @Override
    protected void onPause(){
        super.onPause();
        finish();
    }
}