package com.rui.ble.user.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.rui.ble.R;
import com.rui.ble.user.util.UserDatabaseAdapter;

/**
 * Created by rhuang on 9/15/16.
 */
public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";

    private EditText mNameText;
    private EditText mEmailText;
    private EditText mPassword;
    private Button mSignupBtn;
    private TextView mLoginLink;

    private UserDatabaseAdapter mUserDatabaseAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mNameText = (EditText) findViewById(R.id.user_name_et);
        mEmailText = (EditText) findViewById(R.id.user_email_et);
        mPassword = (EditText) findViewById(R.id.input_password_et);
        mSignupBtn = (Button) findViewById(R.id.signup_btn);
        mLoginLink = (TextView) findViewById(R.id.link_login_tv);

        mUserDatabaseAdapter = new UserDatabaseAdapter(this).open();

        //Log.e(TAG, "mUserDatabaseAdapter = " + mUserDatabaseAdapter);

        mSignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        mLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the MainActivity
                finish();
            }
        });
    }

    public void signup() {

        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        mSignupBtn.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating account...");
        progressDialog.show();

        String name = mNameText.getText().toString();
        String email = mEmailText.getText().toString();
        String password = mPassword.getText().toString();


        // TODO: Implement your own signup logic here.

        mUserDatabaseAdapter.insertEntry(UserDatabaseAdapter.DATABASE_SIGNEDUP_TABLE, name, email, password);
        mUserDatabaseAdapter.insertEntry(UserDatabaseAdapter.DATABASE_CURRENT_TABLE, name, email, password);

        Toast.makeText(getApplicationContext(), "Account has been successfully created!", Toast.LENGTH_LONG).show();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onSignupSuccess or onSignupFailed
                        // depending on success
                        onSignupSuccess();
                        // onSignupFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);
    }


    public void onSignupSuccess() {
        mSignupBtn.setEnabled(true);
        setResult(RESULT_OK, null);

        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Account created failed!", Toast.LENGTH_LONG).show();

        mSignupBtn.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = mNameText.getText().toString();
        String email = mEmailText.getText().toString();
        String password = mPassword.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            mNameText.setError("at least 3 characters");
            valid = false;
        } else {
            mNameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailText.setError("enter a valid email address");
            valid = false;
        } else {
            mEmailText.setError(null);
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
