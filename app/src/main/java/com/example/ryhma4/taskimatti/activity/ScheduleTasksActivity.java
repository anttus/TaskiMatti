package com.example.ryhma4.taskimatti.activity;

import android.content.Intent;
import android.os.Bundle;

import com.example.ryhma4.taskimatti.R;

public class ScheduleTasksActivity extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_tasks);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
    }

}
