package com.rui.ble.user.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.rui.ble.R;
import com.rui.ble.bluetooth.sensor.MainActivity;
import com.rui.ble.user.util.UserDatabaseAdapter;

/**
 * Created by rhuang on 9/15/16.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private static final int REQUEST_SIGNUP = 0;

    private Button mLoginBtn;
    private EditText mPassword;
    private EditText mName;
    private TextView mLinkSignup;
    private ProgressDialog mProgressDialog;

    private UserDatabaseAdapter mUserDatabaseAdapter;

    private static final int USERNAME_ERR = 0;
    private static final int PASSWORD_ERR = 1;
    private static final int RETRY_LIMIT = 2;

    private int mLoginCnt = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Get the reference from login activity
        mLoginBtn = (Button)findViewById(R.id.login_btn);
        mPassword = (EditText)findViewById(R.id.input_password_et);
        mName = (EditText)findViewById(R.id.user_name_et);
        mLinkSignup = (TextView) findViewById(R.id.signup_link_tv);

        // Create a instance of SQLite Database
        mUserDatabaseAdapter = new UserDatabaseAdapter(this).open();

        //mUserDatabaseAdapter = UserDatabaseAdapter.getInstance();
        //Log.e(TAG, "mUserDatabaseAdapter = " + mUserDatabaseAdapter);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        mLinkSignup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
    }

    public void login() {

        Log.d(TAG, "Login");

        mLoginCnt++;

        if (!validate()) {

            mLoginBtn.setEnabled(true);
            mProgressDialog.dismiss();
            Toast.makeText(getBaseContext(), "Incorrect username or password format, please try again!", Toast.LENGTH_LONG).show();
            mLoginCnt = 0;
            return;
        }

        mLoginBtn.setEnabled(false);

        mProgressDialog = new ProgressDialog(LoginActivity.this, R.style.AppTheme_Dark_Dialog);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage("Authenticating...");
        mProgressDialog.show();

        // TODO: Implement your own authentication logic here.

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {

                        // On complete call either onLoginSuccess or onLoginFailed
                        // Check if the username input matches the username in the database

                        //Log.e(TAG, "mUserDatabaseAdapter = " + mUserDatabaseAdapter);
                        //Log.e(TAG, mName.getText().toString() + " vs." + mUserDatabaseAdapter.getSingleEntryByName(UserDatabaseAdapter.DATABASE_CURRENT_TABLE, mName.getText().toString()));
                        //Log.e(TAG, mName.getText().toString() + " vs." + mUserDatabaseAdapter.getSingleEntryByName(UserDatabaseAdapter.DATABASE_SIGNEDUP_TABLE, mName.getText().toString()));

                        if(!mName.getText().toString().equals(mUserDatabaseAdapter.getSingleEntryByName(UserDatabaseAdapter.DATABASE_CURRENT_TABLE, mName.getText().toString()))) {

                            onLoginFailed(USERNAME_ERR);
                            return;
                        }

                        else if (!mPassword.getText().toString().equals(mUserDatabaseAdapter.getSingleEntryByPassword(UserDatabaseAdapter.DATABASE_CURRENT_TABLE, mPassword.getText().toString()))) {

                            onLoginFailed(PASSWORD_ERR);
                            return;

                        }

                        else {

                            mProgressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Congratulations: Login success!", Toast.LENGTH_LONG).show();

                            // Jump to SetUserAgeActivity
                            Intent intent = new Intent(LoginActivity.this, SetUserGenderActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                }, 3000);

        mProgressDialog.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_SIGNUP) {

            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
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

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        mLoginBtn.setEnabled(true);
        finish();
    }

    public void onLoginFailed(int error) {

        if (mLoginCnt > RETRY_LIMIT) {

            mProgressDialog.dismiss();
            mLoginBtn.setEnabled(false);

            // Pop up one alert to ask user if he or she wants to create a new user.
            //Toast.makeText(getBaseContext(), "Login failed over 3 times!", Toast.LENGTH_LONG).show();


            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(LoginActivity.this);

            alertBuilder.setTitle(R.string.login_alert_title);
            alertBuilder.setMessage(R.string.login_alert_message);
            String posText = getString(android.R.string.ok);
            alertBuilder.setPositiveButton(posText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    // Go to retrieve password, implement in next release.

                }

            });

            String negText = getString(android.R.string.cancel);


            alertBuilder.setNegativeButton(negText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Stay here, user may want to create the new account.
                    dialog.dismiss();

                }

            });

            AlertDialog dialog = alertBuilder.create();
            dialog.show();

        }

        else {

            switch (error) {

                case USERNAME_ERR:

                    mLoginBtn.setEnabled(true);
                    mProgressDialog.dismiss();
                    Toast.makeText(getBaseContext(), "Incorrect username, please try again!", Toast.LENGTH_LONG).show();

                    break;

                case PASSWORD_ERR:

                    mLoginBtn.setEnabled(true);
                    mProgressDialog.dismiss();
                    Toast.makeText(getBaseContext(), "Incorrect password, please try again!", Toast.LENGTH_LONG).show();
                    break;

                default:

                    break;

            }

        }

        //Intent intent = new Intent(this, SignupActivity.class);
        //startActivity(intent);
    }

    public boolean validate() {
        boolean valid = true;

        String name = mName.getText().toString();
        String password = mPassword.getText().toString();

        if (name.isEmpty() ) {
            mName.setError("enter a valid email address");
            valid = false;
        } else {
            mName.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            mPassword.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            mPassword.setError(null);
        }

        return valid;
    }

    protected void onDestroy(){
        super.onDestroy();
        mUserDatabaseAdapter.close();
    }
}
