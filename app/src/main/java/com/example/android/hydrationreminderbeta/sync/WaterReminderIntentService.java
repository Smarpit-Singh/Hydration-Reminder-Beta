
package com.example.android.hydrationreminderbeta.sync;

import android.app.IntentService;
import android.content.Intent;


public class WaterReminderIntentService extends IntentService {

    public WaterReminderIntentService() {
        super("WaterReminderIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();

        ReminderTasks.executeTask(this, action);
    }
}