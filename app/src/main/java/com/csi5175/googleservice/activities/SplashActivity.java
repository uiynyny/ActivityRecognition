/*
 * Yan Zhang
 * 300052103
 * splash screen activity for present current date variable to user.
 * */
package com.csi5175.googleservice.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.csi5175.googleservice.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/*
 * This activity is used for splash screen with greetings. only appear when app started
 * can find tutorial here
 * https://android.jlelse.eu/the-complete-android-splash-screen-guide-c7db82bce565
 * */
public class SplashActivity extends AppCompatActivity {
    //DEBUG only
    private static final String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //This is the way of doing static splash screen
        setTheme(R.style.AppTheme_NoActionBar);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        TextView textView = findViewById(R.id.textTime);
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.US);
        String formattedDate = df.format(c);
        textView.setText(formattedDate);

        //set splash activity by timer.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                SplashActivity.this.startActivity(intent);
                SplashActivity.this.finish();
            }
        }, 1000);
    }
}
