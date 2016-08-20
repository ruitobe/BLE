package com.rui.ble.bluetooth.btsig.profiles;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rui.ble.util.GenericCharacteristicTableRow;

/**
 * Created by rhuang on 8/20/16.
 */
public class DevInfoServiceTableRow extends GenericCharacteristicTableRow {

    TextView SystemIDLabel;
    TextView ModelNRLabel;
    TextView SerialNRLabel;
    TextView FirmwareREVLabel;
    TextView HardwareREVLabel;
    TextView SoftwareREVLabel;
    TextView ManifacturerNAMELabel;

    public DevInfoServiceTableRow(Context context) {

        super(context);
        this.SystemIDLabel = new TextView(context) {
            {
                setText("System ID: -");
                setId(200);
            }
        };
        this.ModelNRLabel = new TextView(context) {
            {
                setText("Model NR: -");
                setId(201);
            }
        };
        this.SerialNRLabel = new TextView(context) {
            {
                setText("Serial NR: -");
                setId(202);
            }
        };
        this.FirmwareREVLabel = new TextView(context) {
            {
                setText("Firmware Revision: -");
                setId(203);
            }
        };
        this.HardwareREVLabel = new TextView(context) {
            {
                setText("Hardware Revision: -");
                setId(204);
            }
        };
        this.SoftwareREVLabel = new TextView(context) {
            {
                setText("Software Revision: -");
                setId(205);
            }
        };
        this.ManifacturerNAMELabel = new TextView(context) {
            {
                setText("Manifacturer Name: -");
                setId(206);
            }
        };

        RelativeLayout.LayoutParams tmpLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        tmpLayoutParams.addRule(RelativeLayout.BELOW,
                this.value.getId());
        tmpLayoutParams.addRule(RelativeLayout.RIGHT_OF,icon.getId());
        SystemIDLabel.setLayoutParams(tmpLayoutParams);

        tmpLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        tmpLayoutParams.addRule(RelativeLayout.BELOW,
                this.SystemIDLabel.getId());
        tmpLayoutParams.addRule(RelativeLayout.RIGHT_OF,icon.getId());
        ModelNRLabel.setLayoutParams(tmpLayoutParams);

        tmpLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        tmpLayoutParams.addRule(RelativeLayout.BELOW,
                this.ModelNRLabel.getId());
        tmpLayoutParams.addRule(RelativeLayout.RIGHT_OF,icon.getId());
        SerialNRLabel.setLayoutParams(tmpLayoutParams);

        tmpLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        tmpLayoutParams.addRule(RelativeLayout.BELOW,
                this.SerialNRLabel.getId());
        tmpLayoutParams.addRule(RelativeLayout.RIGHT_OF,icon.getId());
        FirmwareREVLabel.setLayoutParams(tmpLayoutParams);

        tmpLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        tmpLayoutParams.addRule(RelativeLayout.BELOW,
                this.FirmwareREVLabel.getId());
        tmpLayoutParams.addRule(RelativeLayout.RIGHT_OF,icon.getId());
        HardwareREVLabel.setLayoutParams(tmpLayoutParams);

        tmpLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        tmpLayoutParams.addRule(RelativeLayout.BELOW,
                this.HardwareREVLabel.getId());
        tmpLayoutParams.addRule(RelativeLayout.RIGHT_OF,icon.getId());
        SoftwareREVLabel.setLayoutParams(tmpLayoutParams);

        tmpLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        tmpLayoutParams.addRule(RelativeLayout.BELOW,
                this.SoftwareREVLabel.getId());
        tmpLayoutParams.addRule(RelativeLayout.RIGHT_OF,icon.getId());
        ManifacturerNAMELabel.setLayoutParams(tmpLayoutParams);


        rowLayout.addView(SystemIDLabel);
        rowLayout.addView(ModelNRLabel);
        rowLayout.addView(SerialNRLabel);
        rowLayout.addView(FirmwareREVLabel);
        rowLayout.addView(HardwareREVLabel);
        rowLayout.addView(SoftwareREVLabel);
        rowLayout.addView(ManifacturerNAMELabel);

    }

    @Override
    public void onClick(View v) {
        //Do nothing
    }
}
