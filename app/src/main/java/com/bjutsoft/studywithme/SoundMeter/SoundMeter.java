package com.bjutsoft.studywithme.SoundMeter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bjutsoft.studywithme.R;

import java.io.File;

public class SoundMeter extends AppCompatActivity {
    float volume = 10000;
    private SoundDiskView soundDiscView;
    private MyMediaRecorder mRecorder;
    private static final int msgWhat = 0x1001;
    private static final int refreshTime = 100;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_meter);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏锁定
        mRecorder = new MyMediaRecorder();
        imageView = findViewById(R.id.noise_info);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (this.hasMessages(msgWhat)) {
                return;
            }
            volume = mRecorder.getMaxAmplitude();  //获取声压值
            if (volume > 0 && volume < 1000000) {
                float dbCount = 20 * (float) (Math.log10(volume));

                if (dbCount > 80) {
                    imageView.setImageResource(R.drawable.noise);
                } else if (dbCount > 60 && dbCount <= 80) {
                    imageView.setImageResource(R.drawable.reading);
                }
                if (dbCount > 40 && dbCount <= 60) {
                    imageView.setImageResource(R.drawable.learning);
                } else if (dbCount > 20 && dbCount <= 40) {
                    imageView.setImageResource(R.drawable.study);
                } else if (dbCount > 0 && dbCount <= 20) {
                    imageView.setImageResource(R.drawable.thinking);
                }
                World.setDbCount(dbCount);  //将声压值转为分贝值
                soundDiscView.refresh();
            }
            handler.sendEmptyMessageDelayed(msgWhat, refreshTime);
        }
    };

    private void startListenAudio() {
        handler.sendEmptyMessageDelayed(msgWhat, refreshTime);
    }

    //开始记录
    public void startRecord(File fFile) {
        try {
            mRecorder.setMyRecAudioFile(fFile);
            if (mRecorder.startRecorder()) {
                startListenAudio();
            } else {
                Toast.makeText(this, "启动录音失败", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "录音机已被占用或录音权限被禁止", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        soundDiscView = (SoundDiskView) findViewById(R.id.soundDiscView);
        File file = FileUtil.createFile("temp.amr"); // Put recorded audios into file
        if (file != null) {
            Log.v("file", "file =" + file.getAbsolutePath());
            startRecord(file);
        } else {
            Toast.makeText(getApplicationContext(), "创建文件失败", Toast.LENGTH_LONG).show();
        }
    }

    //停止记录
    @Override
    protected void onPause() {
        super.onPause();
        mRecorder.delete(); //停止记录并删除录音文件
        handler.removeMessages(msgWhat);
    }

    @Override
    protected void onDestroy() {
        handler.removeMessages(msgWhat);
        mRecorder.delete();
        super.onDestroy();
    }
}
