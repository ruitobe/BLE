package com.rui.ble.user.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.rui.ble.R;
import com.rui.ble.user.util.UserDatabaseAdapter;

/**
 * Created by ruihuan on 9/24/16.
 */

public class SetUserGenderActivity extends AppCompatActivity {

    private static final String TAG = "SetUserGenderActivity";

    private String mGender = "";

    private ImageButton mMaleBtn;
    private ImageButton mFemaleBtn;
    private Button mNextBtn;

    private boolean mMaleSelected = false;
    private boolean mFemaleSelected = false;

    private int mOption = 0;

    private UserDatabaseAdapter mUserDatabaseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_gender);

        // Get the reference from login activity
        mMaleBtn = (ImageButton) findViewById(R.id.male_ib);
        mFemaleBtn = (ImageButton) findViewById(R.id.female_ib);
        mNextBtn = (Button) findViewById(R.id.gender_next_btn);

        // Create a instance of SQLite Database
        mUserDatabaseAdapter = new UserDatabaseAdapter(this).open();

        mMaleBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mMaleSelected == false) {

                    if (mFemaleSelected == true) {
                        mMaleSelected = true;
                        mFemaleSelected = false;
                        mOption = 1;
                    } else {
                        mMaleSelected = true;
                        mOption = 1;
                    }
                } else {
                    // Do nothing
                }
                updateImageBtns(mOption);
            }
        });

        mFemaleBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mFemaleSelected == false) {

                    if (mMaleSelected == true) {
                        mFemaleSelected = true;
                        mMaleSelected = false;
                        mOption = 2;
                    } else {
                        mFemaleSelected = true;
                        mOption = 2;
                    }
                } else {
                    // Do nothing
                }
                updateImageBtns(mOption);
            }

        });

        mNextBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mOption == 1 || mOption == 2) {
                    // TODO: update the database
                    // Jump to SetUserAgeActivity
                    Intent intent = new Intent(SetUserGenderActivity.this, SetUserAgeActivity.class);
                    startActivity(intent);

                }
            }
        });

    }

    private void updateImageBtns(int option) {

        switch (mOption) {

            case 0:
                mMaleBtn.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.male_0));
                mFemaleBtn.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.female_0));
                break;

            case 1:
                mMaleBtn.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.male));
                mFemaleBtn.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.female_0));
                break;

            case 2:
                mMaleBtn.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.male_0));
                mFemaleBtn.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.female));
                break;

            default:

                break;
        }
    }



}



