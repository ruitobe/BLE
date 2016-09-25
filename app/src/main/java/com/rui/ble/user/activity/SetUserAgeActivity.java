package com.rui.ble.user.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

import cn.carbswang.android.numberpickerview.library.NumberPickerView;

import com.rui.ble.R;

/**
 * Created by ruihuan on 9/24/16.
 */

public class SetUserAgeActivity extends AppCompatActivity implements NumberPickerView.OnScrollListener, NumberPickerView.OnValueChangeListener {

    private Button mNextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_age);

        //Get the widgets reference from XML layout
        NumberPickerView np = (NumberPickerView) findViewById(R.id.age_np);
        mNextBtn = (Button) findViewById(R.id.age_next_btn);

        np.setOnScrollListener(this);
        np.setOnValueChangedListener(this);

        //Populate NumberPicker values from minimum and maximum value range
        //Set the minimum value of NumberPicker
        //np.setMinValue(0);
        //Specify the maximum value/number of NumberPicker
        //np.setMaxValue(10);
        //np.setValue(5);
        String[] display = new String[101];
        for (Integer index = 0; index <= 100; index++) {
            display[index] = index.toString();
        }

        np.refreshByNewDisplayedValues(display);

        mNextBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // TODO: update the database
                // Jump to SetUserAgeActivity
                Intent intent = new Intent(SetUserAgeActivity.this, SetUserHeightActivity.class);
                startActivity(intent);

            }
        });
    }

    @Override
    public void onScrollStateChange(NumberPickerView view, int scrollState) {

    }

    @Override
    public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {

    }
}
