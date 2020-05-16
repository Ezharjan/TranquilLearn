package com.bjutsoft.studywithme.NotePad;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.bjutsoft.studywithme.R;

import org.litepal.LitePal;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NewNote extends AppCompatActivity {
    private Button cancel;
    private String str = "";
    private EditText editText;

    //右上角保存按钮
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.new_note, menu);
        return true;
    }

    //保存按钮的点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //创建数据库
        LitePal.getDatabase();

        //获取当前时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日  HH:mm:ss");// HH:mm:ss
        Date date = new Date(System.currentTimeMillis());
        //将输入的数据保存至数据库
        str = editText.getText().toString();
        Note note = new Note();
        note.setContent(str);
        note.setTime(simpleDateFormat.format(date));
        note.save();
        //销毁活动
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);

        Toolbar new_note_toolbar = findViewById(R.id.new_note_toolbar);
        setSupportActionBar(new_note_toolbar);

        cancel = (Button) findViewById(R.id.cancel);
        editText = (EditText) findViewById(R.id.edit_view);

        //添加事件
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
