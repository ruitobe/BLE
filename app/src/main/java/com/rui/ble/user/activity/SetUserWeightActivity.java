package com.rui.ble.user.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.rui.ble.R;
import com.rui.ble.bluetooth.sensor.MainActivity;

import cn.carbswang.android.numberpickerview.library.NumberPickerView;

/**
 * Created by ruihuan on 9/24/16.
 */

public class SetUserWeightActivity extends AppCompatActivity implements NumberPickerView.OnScrollListener, NumberPickerView.OnValueChangeListener {

    private Button mNextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_weight);

        //Get the widgets reference from XML layout
        NumberPickerView np = (NumberPickerView) findViewById(R.id.weight_np);

        mNextBtn = (Button) findViewById(R.id.weight_next_btn);

        np.setOnScrollListener(this);
        np.setOnValueChangedListener(this);

        //Populate NumberPicker values from minimum and maximum value range
        //Set the minimum value of NumberPicker
        //np.setMinValue(0);
        //Specify the maximum value/number of NumberPicker
        //np.setMaxValue(10);
        //np.setValue(5);
        String[] display = new String[201];
        Integer temp;
        for (Integer index = 0; index <= 200; index++) {
            temp = index + 5;
            display[index] = temp.toString();
        }
        np.refreshByNewDisplayedValues(display);

        mNextBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // TODO: update the database
                // Jump to SetUserAgeActivity
                Intent intent = new Intent(SetUserWeightActivity.this, MainActivity.class);
                startActivity(intent);
                finish();

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
