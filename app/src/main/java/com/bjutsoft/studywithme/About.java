package com.bjutsoft.studywithme;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class About extends AppCompatActivity {

    public static boolean testerOpen;//专供测试使用
    //private Switch aSwitch;
    private CheckBox checkBox;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

//使用Switch控件实现同样的效果
//        aSwitch = (Switch) findViewById(R.id.tester_switch);
//        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                // 通过这个方法，来监听当前的checkbox是否被选中
//                if (isChecked) {
//                    testerOpen = true;
//                    aSwitch.setChecked(true);
//                    MainActivity.CustomToast.showToast(About.this, "已开启测试者模式");
//                } else {
//                    testerOpen = false;
//                    aSwitch.setChecked(false);
//                    MainActivity.CustomToast.showToast(About.this, "已关闭测试者模式");
//                }
//            }
//        });

        checkBox = (CheckBox) findViewById(R.id.tester_checkbox);

        // 要想回显CheckBox的状态 我们需要取得数据
        // 还需要获得SharedPreferences
        sharedPreferences = getSharedPreferences("isChecked", 0);
        boolean result = sharedPreferences.getBoolean("choose", false);//这里就是开始取值了，false代表的就是如果没有得到对应数据我们默认显示为false
        // 把得到的状态设置给CheckBox组件
        checkBox.setChecked(result);


        //通过设置checkbox的监听事件，判断checkbox是否被选中
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 通过这个方法，来监听当前的checkbox是否被选中
                if (isChecked) {
                    testerOpen = true;
                    checkBox.setChecked(true);

                    sharedPreferences = getSharedPreferences("isChecked", 0);//使用编辑器来进行操作
                    SharedPreferences.Editor edit = sharedPreferences.edit();//将勾选的状态保存起来
                    edit.putBoolean("choose", true); //这里的choose就是一个key 通过这个key我们就可以得到对应的值
                    edit.commit();//最好我们别忘记提交一下

                    MainActivity.CustomToast.showToast(About.this, "已开启测试者模式");
                } else {
                    testerOpen = false;
                    checkBox.setChecked(false);

                    sharedPreferences = getSharedPreferences("isChecked", 0);//使用编辑器来进行操作
                    SharedPreferences.Editor edit = sharedPreferences.edit();//将勾选的状态保存起来
                    edit.putBoolean("choose", false); //这里的choose就是一个key 通过这个key我们就可以得到对应的值
                    edit.commit();//最好我们别忘记提交一下

                    MainActivity.CustomToast.showToast(About.this, "已关闭测试者模式");
                }
            }
        });


        Button btnSettings = (Button) findViewById(R.id.tester_settings);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(About.this)
                        .setTitle("测试说明")
                        .setMessage("1.长按测试模式按钮以查看久坐提醒\n" +
                                "   设置时间间隔。              \n" +
                                "2.勾选按钮上方选框进入测试者模式\n" +
                                "   以快速测试久坐提醒功能。       \n" +
                                "3.取消勾选按钮上方对勾即可退出测\n" +
                                "   试者模式。")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
            }
        });
        btnSettings.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (testerOpen) {
                    new AlertDialog.Builder(About.this)
                            .setTitle("久坐提醒间隔为" + MainActivity.myInterval / 1000 + "秒。")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();
                } else {
                    new AlertDialog.Builder(About.this)
                            .setTitle("久坐提醒间隔为" + MainActivity.myInterval / 1000 / 60 + "分钟。")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
