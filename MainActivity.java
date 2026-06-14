package com.example.quiz_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.quiz_app.audio.AudioManager;

public class MainActivity extends AppCompatActivity {
    private AudioManager audioManager;
    private ImageView logo;
    private Button playButton, recordsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //  передаем this (Context)
        audioManager = new AudioManager(this);

        // передаем this и ресурс
        audioManager.startBackgroundMusic(this, R.raw.background_music);

        logo = findViewById(R.id.logo);
        playButton = findViewById(R.id.playButton);
        recordsButton = findViewById(R.id.recordsButton);

        // Анимация логотипа
        logo.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(1000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    logo.animate().scaleX(1f).scaleY(1f).setDuration(500).start();
                })
                .start();

        //  используем MainActivity.this вместо this
        playButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        //  используем MainActivity.this вместо this
        recordsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RecordsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (audioManager != null) {
            audioManager.stopBackgroundMusic();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (audioManager != null) {
            audioManager.startBackgroundMusic(this, R.raw.background_music);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioManager != null) {
            audioManager.release();
        }
    }
}
