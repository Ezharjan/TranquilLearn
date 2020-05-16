package com.bjutsoft.studywithme.NotePad;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bjutsoft.studywithme.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EditNote extends AppCompatActivity {
    //定义控件
    private TextView editview;
    private Button cancel;

    //右上角保存按钮
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_note, menu);
        return true;
    }

    //保存按钮的点击事件,点击保存便签
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //创建Note对象
        Note note = new Note();
        note.setContent(editview.getText().toString());
        //获取当前时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");// HH:mm:ss
        Date date = new Date(System.currentTimeMillis());
        note.setTime(simpleDateFormat.format(date));
        //更新内容和时间
        Intent edit_page = getIntent();
        //获取主页面(活动)传递到的值
        String ss = edit_page.getStringExtra("data_edit");
        //从数据库匹配没修改之前的item值，然后更新新的数据
        note.updateAll("mcontent = ?", ss);//里面的字符串指定的是数据库内部的内容，一定注意要一致
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        Toolbar edit_note_toolbar = findViewById(R.id.edit_note_toolbar);
        setSupportActionBar(edit_note_toolbar);

        //初始化
        editview = (TextView) findViewById(R.id.edit_view);
        cancel = (Button) findViewById(R.id.cancel);

        //获取到用于启动当前活动的Intent
        Intent edit_page = getIntent();
        //获取主页面(活动)传递到的值,加入TextView
        String ss = edit_page.getStringExtra("data_edit");
        editview.setText(ss);

        //cancel的点击事件
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
