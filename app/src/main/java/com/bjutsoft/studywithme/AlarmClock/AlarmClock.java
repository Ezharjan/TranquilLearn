package com.bjutsoft.studywithme.AlarmClock;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bjutsoft.studywithme.R;

import java.util.Calendar;

public class AlarmClock extends AppCompatActivity implements View.OnClickListener {


    TimePicker timepicker;          // 时间拾取器
    public static Calendar c;      // 日历对象
    public static AlarmManager alarm;//闹钟对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏锁定
        setContentView(R.layout.activity_alarm_clock);
        c = Calendar.getInstance();                  //获取日历对象
        timepicker = (TimePicker) findViewById(R.id.timePicker1);    // 获取时间拾取组件
        timepicker.setIs24HourView(true);                            // 设置使用24小时制
        Button settingAlarm = (Button) findViewById(R.id.setting_alarm);   // 获取“设置闹钟”按钮
        settingAlarm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setting_alarm:
                Intent intent = new Intent(AlarmClock.this, AlarmToast.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(AlarmClock.this, 0, intent, 0);
                alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                c.set(Calendar.HOUR_OF_DAY, timepicker.getCurrentHour());  // 设置闹钟的小时数
                c.set(Calendar.MINUTE, timepicker.getCurrentMinute());     // 设置闹钟的分钟数
                c.set(Calendar.SECOND, 0);                                 // 设置闹钟的秒数
                alarm.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);  // 设置一个闹钟
                Toast.makeText(AlarmClock.this, "闹钟设置成功", Toast.LENGTH_SHORT).show();
        }

    }
}
