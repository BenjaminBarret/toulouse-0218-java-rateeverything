package fr.wildcodeschool.rateeverything;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashScreen extends AppCompatActivity {

    private final int SPLASH_TIME_OUT = 6000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        getSupportActionBar().hide();

        ImageView splashScreen = findViewById(R.id.splash_screen);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade);
        splashScreen.startAnimation(animation);

        // chargement des infos de l'utilisateur
        Singleton singleton = Singleton.getsIntance();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
                SplashScreen.this.startActivity(intent);
                SplashScreen.this.finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
