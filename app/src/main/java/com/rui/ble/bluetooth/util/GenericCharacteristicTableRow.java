package com.rui.ble.bluetooth.util;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;

import com.rui.ble.R;
import com.rui.ble.bluetooth.common.GattInfo;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Created by rhuang on 8/7/16.
 */

public class GenericCharacteristicTableRow extends TableRow implements View.OnClickListener, Animation.AnimationListener, SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener{

    //Normal cell operation : Show data contents
    protected final Context context;

    public final SparkLineView x, y, z;

    public final TextView value;

    public final ImageView icon;
    public final TextView title;
    public final TextView uuidLabel;
    private final Paint linePaint;
    protected final RelativeLayout rowLayout;
    public int iconSize = 150;
    public boolean config;

    // Configuration operation : Show configuration contents
    public final Switch onOff;
    public final SeekBar periodBar;
    public final TextView onOffLegend;
    public final TextView periodLegend;
    public final Button calibrateButton;

    public final static String ACTION_PERIOD_UPDATE = "com.rui.ble.bluetooth.util.ACTION_PERIOD_UPDATE";
    public final static String ACTION_ONOFF_UPDATE = "com.rui.ble.bluetooth.util.ACTION_ONOFF_UPDATE";
    public final static String ACTION_CALIBRATE = "com.rui.ble.bluetooth.util.ACTION_CALIBRATE";
    public final static String EXTRA_SERVICE_UUID = "com.rui.ble.bluetooth.util.EXTRA_SERVICE_UUID";
    public final static String EXTRA_PERIOD = "com.rui.ble.bluetooth.util.EXTRA_PERIOD";
    public final static String EXTRA_ONOFF = "com.rui.ble.bluetooth.util.EXTRA_ONOFF";
    public int periodMinVal;

    public static boolean isCorrectService(String uuidString) {
        return true;
    }

    public GenericCharacteristicTableRow(Context context) {

        super(context);
        this.context = context;
        this.config = false;
        this.setLayoutParams(new TableRow.LayoutParams(1));

        this.setBackgroundColor(Color.TRANSPARENT);

        this.setOnClickListener(this);
        this.periodMinVal = 100;

        // GATT database

        Resources res = getResources();
        XmlResourceParser xpp = res.getXml(R.xml.gatt_uuid);
        new GattInfo(xpp);

        this.rowLayout = new RelativeLayout(this.context);

        this.linePaint = new Paint() {
            {
                setStrokeWidth(1);
                setARGB(255, 0, 0, 0);
            }
        };


        //Add all views for the default cell

        // Service icon
        this.icon = new ImageView(context) {
            {
                setId(R.id.icon_iv);
                setPadding(30, 30, 30, 30);
            }
        };

        // Service title
        this.title = new TextView(context) {
            {
                setTextSize(TypedValue.COMPLEX_UNIT_PT, 10.0f);
                setTypeface(null, Typeface.BOLD);
                setId(R.id.title_tv);
            }
        };

        // Service UUID, hidden by default
        this.uuidLabel = new TextView(context) {
            {
                setTextSize(TypedValue.COMPLEX_UNIT_PT, 8.0f);
                setId(R.id.uuid_tv);
                setVisibility(View.INVISIBLE);
            }
        };

        // One Value
        this.value = new TextView(context) {
            {
                setTextSize(TypedValue.COMPLEX_UNIT_PT, 8.0f);
                setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
                setId(R.id.value_tv);
                setVisibility(View.VISIBLE);
            }
        };

        // One Sparkline showing trends
        this.x = new SparkLineView(context) {
            {
                setVisibility(View.VISIBLE);
                setId(R.id.spark_line_v);
            }
        };

        this.y = new SparkLineView(context) {
            {
                setVisibility(View.INVISIBLE);
                setId(R.id.spark_line_v);
                setEnabled(false);
            }
        };

        this.z = new SparkLineView(context) {
            {
                setVisibility(View.INVISIBLE);
                setId(R.id.spark_line_v);
                setEnabled(false);
            }
        };

        this.onOff = new Switch(context) {
            {
                setVisibility(View.INVISIBLE);
                setId(R.id.switch_v);
                //setChecked(false);
            }
        };

        this.periodBar = new SeekBar(context) {
            {
                setVisibility(View.INVISIBLE);
                setId(R.id.period_bar_v);
                setMax(245);
            }
        };

        this.onOffLegend = new TextView(context) {
            {
                setVisibility(View.INVISIBLE);
                setId(R.id.sensor_state_tv);
                setText("Sensor state");
            }
        };

        this.periodLegend = new TextView(context) {
            {
                setVisibility(View.INVISIBLE);
                setId(R.id.sensor_period_tv);
                setText("Sensor period");
            }
        };

        this.calibrateButton = new Button(context) {
            {
                setVisibility(View.INVISIBLE);
                setId(R.id.cal_btn_tv);
                setText("Calibrate");
            }
        };


        this.periodBar.setOnSeekBarChangeListener(this);
        this.onOff.setOnCheckedChangeListener(this);

        //Setup content of the fields

        //Setup layout for all cell elements
        RelativeLayout.LayoutParams iconItemParams = new RelativeLayout.LayoutParams(iconSize, iconSize) {
            {
                addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
                addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            }

        };

        icon.setLayoutParams(iconItemParams);

        RelativeLayout.LayoutParams tmpLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        tmpLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        tmpLayoutParams.addRule(RelativeLayout.RIGHT_OF, icon.getId());

        title.setLayoutParams(tmpLayoutParams);
        tmpLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        tmpLayoutParams.addRule(RelativeLayout.BELOW, title.getId());
        tmpLayoutParams.addRule(RelativeLayout.RIGHT_OF, icon.getId());

        uuidLabel.setLayoutParams(tmpLayoutParams);
        tmpLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        tmpLayoutParams.addRule(RelativeLayout.BELOW, title.getId());
        tmpLayoutParams.addRule(RelativeLayout.RIGHT_OF,icon.getId());

        value.setLayoutParams(tmpLayoutParams);
        tmpLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        tmpLayoutParams.addRule(RelativeLayout.BELOW, value.getId());
        tmpLayoutParams.addRule(RelativeLayout.RIGHT_OF, icon.getId());

        this.x.setLayoutParams(tmpLayoutParams);
        this.y.setLayoutParams(tmpLayoutParams);
        this.z.setLayoutParams(tmpLayoutParams);


        tmpLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        tmpLayoutParams.addRule(RelativeLayout.BELOW, value.getId());
        tmpLayoutParams.addRule(RelativeLayout.RIGHT_OF,icon.getId());
        onOffLegend.setLayoutParams(tmpLayoutParams);

        tmpLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        tmpLayoutParams.addRule(RelativeLayout.BELOW, onOffLegend.getId());
        tmpLayoutParams.addRule(RelativeLayout.RIGHT_OF,icon.getId());
        onOff.setLayoutParams(tmpLayoutParams);

        tmpLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        tmpLayoutParams.addRule(RelativeLayout.BELOW, value.getId());
        tmpLayoutParams.addRule(RelativeLayout.RIGHT_OF, onOff.getId());
        calibrateButton.setLayoutParams(tmpLayoutParams);

        tmpLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        tmpLayoutParams.addRule(RelativeLayout.BELOW, onOff.getId());
        tmpLayoutParams.addRule(RelativeLayout.RIGHT_OF,icon.getId());
        periodLegend.setLayoutParams(tmpLayoutParams);

        tmpLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        tmpLayoutParams.addRule(RelativeLayout.BELOW, periodLegend.getId());
        tmpLayoutParams.rightMargin = 50;
        tmpLayoutParams.addRule(RelativeLayout.RIGHT_OF, icon.getId());
        this.periodBar.setLayoutParams(tmpLayoutParams);

        // Add all views to cell
        rowLayout.addView(icon);
        rowLayout.addView(title);
        rowLayout.addView(uuidLabel);
        rowLayout.addView(value);
        rowLayout.addView(this.x);
        rowLayout.addView(this.y);
        rowLayout.addView(this.z);
        rowLayout.addView(this.onOffLegend);
        rowLayout.addView(this.onOff);
        rowLayout.addView(this.periodLegend);
        rowLayout.addView(this.periodBar);
        rowLayout.addView(this.calibrateButton);

        this.addView(rowLayout);
    }

    public void setIcon(String iconPrefix, String uuid) {
        WindowManager wm = (WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point dSize = new Point();
        display.getSize(dSize);
        Drawable image = null;


        if (dSize.x > 1100) {
            Uri uri = Uri.parse("android.resource://"+ this.context.getPackageName()+"/drawable/" + iconPrefix + GattInfo.uuidToIcon(UUID.fromString(uuid)) + "_300");
            try {
                InputStream inputStream = this.context.getContentResolver().openInputStream(uri);
                image = Drawable.createFromStream(inputStream, uri.toString() );
                iconSize = 360;
            }
            catch (FileNotFoundException e) {

            }
        }
        else {
            Uri uri = Uri.parse("android.resource://"+ this.context.getPackageName()+"/drawable/" + iconPrefix + GattInfo.uuidToIcon(UUID.fromString(uuid)));
            try {
                InputStream inputStream = this.context.getContentResolver().openInputStream(uri);
                image = Drawable.createFromStream(inputStream, uri.toString() );
                iconSize = 210;
            }
            catch (FileNotFoundException e) {

            }
        }
        icon.setImageDrawable(image);

        this.x.displayWidth = this.y.displayWidth = this.z.displayWidth = dSize.x - iconSize - 5;
        RelativeLayout.LayoutParams iconItemParams = new RelativeLayout.LayoutParams(
                iconSize,
                iconSize) {
            {
                addRule(RelativeLayout.CENTER_VERTICAL,
                        RelativeLayout.TRUE);
                addRule(RelativeLayout.ALIGN_PARENT_LEFT,RelativeLayout.TRUE);
            }

        };
        icon.setLayoutParams(iconItemParams);
    }

    public void setIcon(String iconPrefix, String uuid,String variantName) {

        WindowManager wm = (WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point dSize = new Point();
        display.getSize(dSize);
        Drawable image = null;



        if (dSize.x > 1100) {
            Uri uri = Uri.parse("android.resource://"+ this.context.getPackageName()+"/drawable/" + iconPrefix + variantName + "_300");
            try {
                InputStream inputStream = this.context.getContentResolver().openInputStream(uri);
                image = Drawable.createFromStream(inputStream, uri.toString() );
                iconSize = 360;
            }
            catch (FileNotFoundException e) {

            }
        }
        else {
            Uri uri = Uri.parse("android.resource://"+ this.context.getPackageName()+"/drawable/" + iconPrefix + variantName);
            try {
                InputStream inputStream = this.context.getContentResolver().openInputStream(uri);
                image = Drawable.createFromStream(inputStream, uri.toString() );
                iconSize = 210;
            }
            catch (FileNotFoundException e) {

            }
        }

        this.x.displayWidth = this.y.displayWidth = this.z.displayWidth = dSize.x - iconSize - 5;
        icon.setImageDrawable(image);

        RelativeLayout.LayoutParams iconItemParams = new RelativeLayout.LayoutParams(
                iconSize,
                iconSize) {
            {
                addRule(RelativeLayout.CENTER_VERTICAL,
                        RelativeLayout.TRUE);
                addRule(RelativeLayout.ALIGN_PARENT_LEFT,RelativeLayout.TRUE);
            }

        };
        icon.setLayoutParams(iconItemParams);

    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
        canvas.drawLine(0, canvas.getHeight() - this.linePaint.getStrokeWidth(), canvas.getWidth(), canvas.getHeight() - this.linePaint.getStrokeWidth(), this.linePaint);
    }

    @Override
    public void onConfigurationChanged (Configuration newConfig) {

        WindowManager wm = (WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point dSize = new Point();
        display.getSize(dSize);
        this.x.displayWidth = this.y.displayWidth = this.z.displayWidth = dSize.x - iconSize - 5;
        this.invalidate();
    }

    @Override
    public void onClick(View v) {

        this.config = !this.config;
        Log.d("onClick","Row ID" + v.getId());

        Animation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setAnimationListener(this);
        fadeOut.setDuration(500);
        fadeOut.setStartOffset(0);
        Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setAnimationListener(this);
        fadeIn.setDuration(500);
        fadeIn.setStartOffset(250);

        if (this.config == true) {

            this.x.startAnimation(fadeOut);

            if ((this.y.isEnabled()))this.y.startAnimation(fadeOut);

            if ((this.z.isEnabled()))this.z.startAnimation(fadeOut);

            this.value.startAnimation(fadeOut);
            this.onOffLegend.startAnimation(fadeIn);
            this.onOff.startAnimation(fadeIn);
            this.periodLegend.startAnimation(fadeIn);
            this.periodBar.startAnimation(fadeIn);
        }

        else {

            this.x.startAnimation(fadeIn);
            if ((this.y.isEnabled()))this.y.startAnimation(fadeIn);
            if ((this.z.isEnabled()))this.z.startAnimation(fadeIn);
            this.value.startAnimation(fadeIn);
            this.onOffLegend.startAnimation(fadeOut);
            this.onOff.startAnimation(fadeOut);
            this.periodLegend.startAnimation(fadeOut);
            this.periodBar.startAnimation(fadeOut);
        }
    }
    @Override
    public void onAnimationStart (Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {

        if (this.config == true) {
            this.x.setVisibility(View.INVISIBLE);

            if ((this.y.isEnabled()))this.y.setVisibility(View.INVISIBLE);
            if ((this.z.isEnabled()))this.z.setVisibility(View.INVISIBLE);
            this.onOff.setVisibility(View.VISIBLE);
            this.onOffLegend.setVisibility(View.VISIBLE);
            this.periodBar.setVisibility(View.VISIBLE);
            this.periodLegend.setVisibility(View.VISIBLE);
        }

        else {
            this.x.setVisibility(View.VISIBLE);
            if ((this.y.isEnabled()))this.y.setVisibility(View.VISIBLE);
            if ((this.z.isEnabled()))this.z.setVisibility(View.VISIBLE);
            this.onOff.setVisibility(View.INVISIBLE);
            this.onOffLegend.setVisibility(View.INVISIBLE);
            this.periodBar.setVisibility(View.INVISIBLE);
            this.periodLegend.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        Log.d("GenericBluetoothProfile", "Period changed : " + progress);
        this.periodLegend.setText("Sensor period (currently : " + ((progress * 10) + periodMinVal) + "ms)");
    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.d("GenericBluetoothProfile", "Period Start");
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.d("GenericBluetoothProfile", "Period Stop");
        final Intent intent = new Intent(ACTION_PERIOD_UPDATE);
        int period = periodMinVal + (seekBar.getProgress() * 10);
        intent.putExtra(EXTRA_SERVICE_UUID, this.uuidLabel.getText());
        intent.putExtra(EXTRA_PERIOD,period);
        this.context.sendBroadcast(intent);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d("GenericBluetoothProfile", "Switch changed : " + isChecked);
        final Intent intent = new Intent(ACTION_ONOFF_UPDATE);
        intent.putExtra(EXTRA_SERVICE_UUID, this.uuidLabel.getText());
        intent.putExtra(EXTRA_ONOFF,isChecked);
        this.context.sendBroadcast(intent);
    }

    public void grayedOut(boolean gray) {
        if (gray) {
            this.periodBar.setAlpha(0.4f);
            this.value.setAlpha(0.4f);
            this.title.setAlpha(0.4f);
            this.icon.setAlpha(0.4f);

            this.x.setAlpha(0.4f);
            this.y.setAlpha(0.4f);
            this.z.setAlpha(0.4f);
            this.periodLegend.setAlpha(0.4f);

        }
        else {
            this.periodBar.setAlpha(1.0f);
            this.value.setAlpha(1.0f);
            this.title.setAlpha(1.0f);
            this.icon.setAlpha(1.0f);
            this.x.setAlpha(1.0f);
            this.y.setAlpha(1.0f);
            this.z.setAlpha(1.0f);
            this.periodLegend.setAlpha(1.0f);
        }
    }

}

