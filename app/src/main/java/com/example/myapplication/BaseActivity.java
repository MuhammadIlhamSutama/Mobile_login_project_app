package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class BaseActivity extends AppCompatActivity {
    private View loadingOverlay;
    private ImageView loadingGif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // child activity yg setContentView-nya
    }

    protected void setupLoadingOverlay() {
        loadingOverlay = findViewById(R.id.loading_overlay);
        loadingGif = findViewById(R.id.loading_gif);

        Glide.with(this)
                .asGif()
                .load(R.drawable.loading_animation)  // pastikan GIF ada di drawable
                .into(loadingGif);
    }

    public void showLoading() {
        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);
    }

    public void hideLoading() {
        if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
    }
}
