package com.bjutsoft.studywithme.AlarmClock;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.Toast;

import com.bjutsoft.studywithme.R;

public class AlarmToast extends AppCompatActivity {

    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏锁定
        setContentView(R.layout.activity_alarm_toast);
        vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        vibrator.vibrate(2000);
        final AlertDialog alert = new AlertDialog.Builder(this).create();
        alert.setIcon(R.drawable.alarm);//设置对话框的图标
        alert.setTitle("逝者如斯");//设置对话框的标题
        alert.setMessage("效苏秦之刺股折桂还需奋战；\n学陶侃之惜时付出必有回报! ");//设置要显示的内容

        //添加确定按钮
        alert.setButton(DialogInterface.BUTTON_POSITIVE, "善哉", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(AlarmToast.this, "闹钟已经关闭！", Toast.LENGTH_LONG).show();
            }
        });
        //添加取消按钮
        alert.setButton(DialogInterface.BUTTON_NEGATIVE, "顷之", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(AlarmToast.this, "已延时五百分钟！", Toast.LENGTH_LONG).show();
            }
        });
        alert.show(); // 显示对话框

    }
}
