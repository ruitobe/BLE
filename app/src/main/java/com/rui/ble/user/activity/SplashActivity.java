package com.rui.ble.user.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;

import com.daimajia.androidanimations.library.Techniques;
import com.rui.ble.R;
import com.rui.ble.bluetooth.sensor.MainActivity;
import com.rui.ble.user.util.UserDatabaseAdapter;
import com.viksaa.sssplash.lib.activity.AwesomeSplash;
import com.viksaa.sssplash.lib.model.ConfigSplash;

/**
 * Created by rhuang on 9/15/16.
 */
public class SplashActivity extends AwesomeSplash {

    private static final String TAG = "SplashActivity";
    private UserDatabaseAdapter mUserDatabaseAdapter;

    @Override
    public void initSplash(ConfigSplash configSplash) {

        getSupportActionBar().hide();

        //Customize Circular Reveal
        //any color you want form colors.xml
        configSplash.setBackgroundColor(R.color.white);
        //int ms
        configSplash.setAnimCircularRevealDuration(0);

        //configSplash.setRevealFlagX(Flags.REVEAL_RIGHT);  //or Flags.REVEAL_LEFT
        //configSplash.setRevealFlagY(Flags.REVEAL_BOTTOM); //or Flags.REVEAL_TOP

        // Choose LOGO OR PATH; if you don't provide String value for path it's logo by default

        // Customize Logo
        // or any other drawable
        // int ms
        configSplash.setLogoSplash(R.drawable.splash);
        configSplash.setAnimLogoSplashDuration(5000);
        //choose one form Techniques (ref: https://github.com/daimajia/AndroidViewAnimations)
        configSplash.setAnimLogoSplashTechnique(Techniques.ZoomIn);


        //Customize Path
        /*
        configSplash.setPathSplash(AppConstants.DROID_LOGO); //set path String
        configSplash.setOriginalHeight(200); //in relation to your svg (path) resource
        configSplash.setOriginalWidth(200); //in relation to your svg (path) resource
        configSplash.setAnimPathStrokeDrawingDuration(3000);
        configSplash.setPathSplashStrokeSize(3); //I advise value be <5
        configSplash.setPathSplashStrokeColor(R.color.accent); //any color you want form colors.xml
        configSplash.setAnimPathFillingDuration(5000);
        configSplash.setPathSplashFillColor(R.color.accent); //path object filling color
        */

        //Customize Title
        configSplash.setTitleSplash("BLE Application");
        configSplash.setTitleTextColor(R.color.primary_darker);
        //float value
        configSplash.setTitleTextSize(30f);
        configSplash.setAnimTitleDuration(3000);
        configSplash.setAnimTitleTechnique(Techniques.FlipInX);
        configSplash.setTitleFont("Rui-Regular.ttf");

    }

    @Override
    public void animationsFinished() {

        // Check if in the database there is user already.
        // Get the instance of SQLite Database
        mUserDatabaseAdapter = new UserDatabaseAdapter(this).open();
        //Log.e(TAG, "mUserDatabaseAdapter = " + mUserDatabaseAdapter);

        if (!mUserDatabaseAdapter.isTableExisted(UserDatabaseAdapter.DATABASE_SIGNEDUP_TABLE) ||
                mUserDatabaseAdapter.getEntryCountInTable(UserDatabaseAdapter.DATABASE_SIGNEDUP_TABLE) == 0) {

            Intent intent = new Intent(this, SignupActivity.class);
            startActivity(intent);
        }

        else {

            Log.e(TAG, "DATABASE_SIGNEDUP_TABLE = " + mUserDatabaseAdapter.getEntryCountInTable(UserDatabaseAdapter.DATABASE_SIGNEDUP_TABLE));
            Log.e(TAG, "DATABASE_CURRENT_TABLE = " + mUserDatabaseAdapter.getEntryCountInTable(UserDatabaseAdapter.DATABASE_CURRENT_TABLE));
            Log.e(TAG, "current = " + mUserDatabaseAdapter.getSingleEntryByName("current", "Rui"));

            if (!mUserDatabaseAdapter.isTableExisted(UserDatabaseAdapter.DATABASE_CURRENT_TABLE) ||
                    mUserDatabaseAdapter.getEntryCountInTable(UserDatabaseAdapter.DATABASE_SIGNEDUP_TABLE) == 0) {

                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            }

            else {

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }

        }

        mUserDatabaseAdapter.close();

        finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //land
        }
        else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            //port
        }
    }
}
