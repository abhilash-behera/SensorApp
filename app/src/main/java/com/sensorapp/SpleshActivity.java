package com.sensorapp;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SpleshActivity extends AppCompatActivity {

    ImageView imageViewSplash;
    TextView textViewSplash;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splesh);

        imageViewSplash= (ImageView) findViewById(R.id.imageViewSplash);
        textViewSplash= (TextView) findViewById(R.id.textViewSplash);
//Hide status bar
        ActionBar ab=getSupportActionBar();
        ab.hide();

        //Animation
        Animation anim= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_in);
     Animation anim1= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.side_in_left);

//        AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(getApplicationContext(),R.anim.property_animator);
//        set.setTarget(imageViewSplash);
//        set.start();

        //assign animation to image view
        textViewSplash.setAnimation(anim);
        imageViewSplash.setAnimation(anim1);



//define what happen when animation complite
        Thread background = new Thread() {
            public void run() {

                try {
                    // Thread will sleep for 8 seconds
                    sleep(2*1000);

                    // After 5 seconds redirect to another intent
                    Intent i=new Intent(SpleshActivity.this,DashBoardActivity.class);
                    SpleshActivity.this.startActivity(i);
                    finish();
                } catch (Exception e) {

                }
            }
        };

        // start thread
        background.start();
    }
}
