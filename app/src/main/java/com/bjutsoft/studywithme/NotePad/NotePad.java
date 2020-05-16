package com.bjutsoft.studywithme.NotePad;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.bjutsoft.studywithme.R;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotePad extends AppCompatActivity {
    //声明
    private SimpleAdapter adapter;
    private List<Map<String, Object>> list;     //String为key值，Object为value值
    private Map<String, Object> map;
    private ListView listview;

    //右上角新建按钮
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //MenuInflater inflater = getMenuInflater();
        getMenuInflater().inflate(R.menu.note_pad_main, menu);
        //inflater.inflate(R.menu.note_pad_main, menu);
        return true;
    }

    //新建按钮的点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent new_note = new Intent(NotePad.this, NewNote.class);
        startActivity(new_note);  //跳转到新建便签页面
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_pad);

        Toolbar test_new_toolbar = findViewById(R.id.note_pad_toolbar);
        setSupportActionBar(test_new_toolbar);

        //初始化
        showListView();
        //长按删除便签
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //获取当前item的值
                TextView tv = view.findViewById(R.id.note_content);
                String s = tv.getText().toString();
                ShowDialog(s, position);   //将当前item的值和位置传给对话框。再通过对话框传给DeleteNote()方法
                Log.d("对话框被调用", "成功");
                return true;  //为true则表示长按时不响应单击事件
            }
        });

        //单击进入编辑便签页面
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //创建意图对象
                Intent edit_intent = new Intent(NotePad.this, EditNote.class);
                //获取当前item的值
                TextView tv2 = view.findViewById(R.id.note_content);
                String edit_data = tv2.getText().toString();
                //将item值传给编辑页面(活动)
                edit_intent.putExtra("data_edit", edit_data);  //data_edit为键
                startActivity(edit_intent);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        //adapter.notifyDataSetChanged();
        //刷新ListView
        adapter.notifyDataSetChanged();
        showListView();

    }

    //准备数据源的方法
    public void createData() {
        //从数据库获取数据
        List<Note> notes = LitePal.order("id desc").find(Note.class);  //指定按id倒序排列
        //遍历
        for (Note note : notes) {
            map = new HashMap<String, Object>();
            map.put("mcontent", note.getContent());
            map.put("mtime", note.getTime());
            list.add(map);
            Log.d("MainActivity", "note content is" + note.getContent());
        }
        String[] key_content = {"mcontent", "mtime"};
        int[] key_time = {R.id.note_content, R.id.note_time};
        adapter = new SimpleAdapter(NotePad.this, list, R.layout.note_list, key_content, key_time);
    }

    //显示ListView的方法
    public void showListView() {
        listview = findViewById(R.id.list_view);
        list = new ArrayList<Map<String, Object>>();
        //获取数据源
        createData();
        listview.setAdapter(adapter);

    }

    //删除便签的方法
    public void DeleteNote(String value, int position) {     //value为当前item的值，position为当前item的位置
        //从数据库匹配当前item值执行删除
        LitePal.deleteAll(Note.class, "mcontent=?", value);
        list.remove(position);
        //通知适配器ListView改变
        adapter.notifyDataSetChanged();
    }

    //弹出对话框的方法
    private void ShowDialog(final String value, final int position) {    //value为当前item的值，position为当前item的位置
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(NotePad.this);
        normalDialog.setIcon(R.drawable.warn);
        normalDialog.setTitle("是否确认删除");
        normalDialog.setMessage("点击确定将删除当前便签");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //调用DeletaNote删除便签
                        DeleteNote(value, position);
                        //关闭对话框
                        dialog.dismiss();
                    }
                });
        normalDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //关闭对话框
                        dialog.dismiss();
                    }
                });
        // 显示
        normalDialog.show();
    }

}
