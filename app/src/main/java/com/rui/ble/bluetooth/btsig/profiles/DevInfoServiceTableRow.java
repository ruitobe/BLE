package com.rui.ble.bluetooth.btsig.profiles;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rui.ble.R;
import com.rui.ble.bluetooth.util.GenericCharacteristicTableRow;

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
    TextView ManufacturerNAMELabel;

    public DevInfoServiceTableRow(Context context) {

        super(context);
        this.SystemIDLabel = new TextView(context) {
            {
                setText("System ID: ");
                setId(R.id.system_id_tv);
            }
        };
        this.ModelNRLabel = new TextView(context) {
            {
                setText("Model NR: ");
                setId(R.id.model_nr_tv);
            }
        };
        this.SerialNRLabel = new TextView(context) {
            {
                setText("Serial NR: ");
                setId(R.id.serial_nr_tv);
            }
        };
        this.FirmwareREVLabel = new TextView(context) {
            {
                setText("Firmware Revision: ");
                setId(R.id.fw_revision_tv);
            }
        };
        this.HardwareREVLabel = new TextView(context) {
            {
                setText("Hardware Revision: ");
                setId(R.id.hw_revision_tv);
            }
        };
        this.SoftwareREVLabel = new TextView(context) {
            {
                setText("Software Revision: ");
                setId(R.id.sw_revision_tv);
            }
        };
        this.ManufacturerNAMELabel = new TextView(context) {
            {
                setText("Manifacturer Name: ");
                setId(R.id.manufacturer_name_tv);
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
        ManufacturerNAMELabel.setLayoutParams(tmpLayoutParams);


        rowLayout.addView(SystemIDLabel);
        rowLayout.addView(ModelNRLabel);
        rowLayout.addView(SerialNRLabel);
        rowLayout.addView(FirmwareREVLabel);
        rowLayout.addView(HardwareREVLabel);
        rowLayout.addView(SoftwareREVLabel);
        rowLayout.addView(ManufacturerNAMELabel);

    }

    @Override
    public void onClick(View v) {
        //Do nothing
    }
}
