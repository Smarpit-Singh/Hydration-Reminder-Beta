package com.example.android.hydrationreminderbeta;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.hydrationreminderbeta.sync.ReminderTasks;
import com.example.android.hydrationreminderbeta.sync.ReminderUtils;
import com.example.android.hydrationreminderbeta.sync.WaterReminderIntentService;
import com.example.android.hydrationreminderbeta.utilities.NotificationUtils;
import com.example.android.hydrationreminderbeta.utilities.PreferenceUtilities;

public class MainActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private TextView mWaterCountDisplay;
    private TextView mChargingCountDisplay;
    private ImageView mChargingImageView;

    private Toast mToast;

    ChargingBroadcastReceiver mChargingReceiver;
    IntentFilter mChargingIntentFilter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mWaterCountDisplay =  findViewById(R.id.tv_water_count);
        mChargingCountDisplay =  findViewById(R.id.tv_charging_reminder_count);
        mChargingImageView =  findViewById(R.id.iv_power_increment);

        updateWaterCount();
        updateChargingReminderCount();

        ReminderUtils.scheduleChargingReminder(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        mChargingIntentFilter = new IntentFilter();
        mChargingReceiver = new ChargingBroadcastReceiver();

        mChargingIntentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        mChargingIntentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
    }


    private void updateWaterCount() {
        int waterCount = PreferenceUtilities.getWaterCount(this);
        mWaterCountDisplay.setText(waterCount+"");
    }


    private void updateChargingReminderCount() {
        int chargingReminders = PreferenceUtilities.getChargingReminderCount(this);
        String formattedChargingReminders = getResources().getQuantityString(
                R.plurals.charge_notification_count, chargingReminders, chargingReminders);
        mChargingCountDisplay.setText(formattedChargingReminders);

    }


    public void incrementWater(View view) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(this, R.string.water_chug_toast, Toast.LENGTH_SHORT);
        mToast.show();

        Intent incrementWaterCountIntent = new Intent(this, WaterReminderIntentService.class);
        incrementWaterCountIntent.setAction(ReminderTasks.ACTION_INCREMENT_WATER_COUNT);
        startService(incrementWaterCountIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PreferenceUtilities.KEY_WATER_COUNT.equals(key)) {
            updateWaterCount();
        } else if (PreferenceUtilities.KEY_CHARGING_REMINDER_COUNT.equals(key)) {
            updateChargingReminderCount();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mChargingReceiver, mChargingIntentFilter);
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mChargingReceiver);
    }

    private class ChargingBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean isCharging = (action.equals(Intent.ACTION_POWER_CONNECTED));

            showCharging(isCharging);
        }
    }


    private void showCharging(boolean isCharging){
        if (isCharging) {
            mChargingImageView.setImageResource(R.drawable.ic_power_pink_80px);

        } else {
            mChargingImageView.setImageResource(R.drawable.ic_power_grey_80px);
        }
    }
}