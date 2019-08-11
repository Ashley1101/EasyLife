package com.deepspring.lib.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.deepspring.lib.R;

public class SplashActivity extends AppCompatActivity {

    private Handler mHandler;
    private ImageView mImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = LayoutInflater.from(this).inflate(R.layout.activity_splash,null);
        setContentView(rootView);
        mImg = (ImageView) findViewById(R.id.splash_img);
        mHandler  = new Handler();
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.splash_up);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(SplashActivity.this,SportActivity.class);
                        startActivity(intent);
                        SplashActivity.this.finish();
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mImg.startAnimation(animation);
    }
}
