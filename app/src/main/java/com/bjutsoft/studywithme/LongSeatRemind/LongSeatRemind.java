package com.bjutsoft.studywithme.LongSeatRemind;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import com.bjutsoft.studywithme.R;

public class LongSeatRemind extends AppCompatActivity implements SensorEventListener {
    AlertDialog alert;

    Vibrator vibrator;
    public long myInterval = 5000;

    private SensorManager mSensorManager; //传感器管理
    public static float SENSITIVITY = 8; // SENSITIVITY灵敏度
    private float mLastValues[] = new float[3 * 2];
    private float mScale[] = new float[2];
    private float mYOffset;//位移
    private static long mEnd = 0;//运动相隔时间
    private static long mStart = 0;//运动开始时间
    private static long mResult = 0;//临时存储运动状态值

    private static final int called = 1;
    private static boolean shouldLoop = true;
    //private static boolean longStaying = true;
    private static int flag = 1;
    private Handler handler = new Handler();
    private Runnable runnable;
    private static boolean dialogExist = false;

    private static long startTime = System.currentTimeMillis();//记录运行时的时间

    /**
     * 最后加速度方向
     */
    private float mLastDirections[] = new float[3 * 2];//最后的方向
    private float mLastExtremes[][] = {new float[3 * 2], new float[3 * 2]};
    private float mLastDiff[] = new float[3 * 2];
    private int mLastMatch = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("我在！", "onCreate");
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏锁定

        // 获取传感器的服务，初始化传感器
        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        // 用于判断是否计步的值
        int h = 480;
        mYOffset = h * 0.5f;
        mScale[0] = -(h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));//重力加速度
        mScale[1] = -(h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));//地球最大磁场

        //setTimer();

        alert = new AlertDialog.Builder(com.bjutsoft.studywithme.LongSeatRemind.LongSeatRemind.this).create();


        runnable = new Runnable() {
            public void run() {
                long currentTime = System.currentTimeMillis();
                Log.d("我在", "" + (mResult));
                Log.d("我在", "TimeContract-The contract of time is---" + (currentTime - startTime));
                if ((currentTime - startTime) > myInterval) {//当前时间减去先前运行时的时间看其是否大于指定时间
                    vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
//                    long[] pattern = {500, 500, 2000, 50};
                    vibrator.vibrate(1000);

                    if (!dialogExist) {
                        //弹窗提示
                        alert.setIcon(R.drawable.alarm);//设置对话框的图标
                        alert.setTitle("运动运动");//设置对话框的标题
                        alert.setMessage("久坐对身体不好；\n身体是幸福的本钱! ");//设置要显示的内容
                        alert.setButton(DialogInterface.BUTTON_POSITIVE, "善哉", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                vibrator.cancel();
                                dialogExist = false;
                                Toast.makeText(com.bjutsoft.studywithme.LongSeatRemind.LongSeatRemind.this, "运动健康", Toast.LENGTH_LONG).show();
                            }
                        });
                        Log.d("DialogShowed???", "----" + alert);
                        alert.show();
                        Log.d("DialogShowed!!!", "----" + alert);
                        dialogExist = true;
                    }
                }
                handler.postDelayed(this, myInterval);//设置提醒时间
                //postDelayed(this,myInterval)方法安排一个Runnable对象到主线程队列中
            }
        };
        runnable.run();
    }

    @Override
    protected void onResume() {
        // 注册传感器，注册监听器
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        //三个参数是：监听器SensorListener对象，传感器类型，频度
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        //取消注册监听器
        mSensorManager.unregisterListener(this);
        //stopTimer();
        Log.d("我在", "是我是我是我是我是我在捣鬼！！！！！！！！！！！");
        super.onDestroy();

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        synchronized (this) {
            // 判断传感器的类型是否为重力传感器(加速度传感器)
            int j = (sensor.getType() == Sensor.TYPE_ACCELEROMETER) ? 1 : 0;
            if (j == 1) {
                float vSum = 0;
                // 获取x轴、y轴、z轴的加速度
                for (int i = 0; i < 3; i++) {
                    final float v = mYOffset + event.values[i] * mScale[j];
                    vSum += v;
                }
                int k = 0;
                float v = vSum / 3;//获取三轴加速度的平均值
                float direction = (v > mLastValues[k] ? 1 : (v < mLastValues[k] ? -1 : 0));
                if (direction == -mLastDirections[k]) {
                    int extType = (direction > 0 ? 0 : 1);
                    mLastExtremes[extType][k] = mLastValues[k];
                    //设置灵敏度
                    float diff = Math.abs(mLastExtremes[extType][k] - mLastExtremes[1 - extType][k]);

                    if (diff > SENSITIVITY) {
                        boolean isAlmostAsLargeAsPrevious = diff > (mLastDiff[k] * 2 / 3);
                        boolean isPreviousLargeEnough = mLastDiff[k] > (diff / 3);
                        boolean isNotContra = (mLastMatch != 1 - extType);
                        if (isAlmostAsLargeAsPrevious && isPreviousLargeEnough && isNotContra) {
                            mEnd = System.currentTimeMillis();//获取结束时间并赋给mEnd
                            // 测试用：通过判断两次运动间隔判断是否运动一下
                            if (mEnd - mStart > 500) {
                                startTime = System.currentTimeMillis();
                                vibrator.cancel();
                                Log.d("DialogDissmissed???", "Dialog waiting to be dissmissed.");
                                alert.dismiss();
                                dialogExist = false;
                                mResult = mEnd - mStart;
                                mEnd = mStart;
                            }
                        } else {
                            mLastMatch = -1;
                        }
                    }
                    mLastDiff[k] = diff;
                }
                mLastDirections[k] = direction;
                mLastValues[k] = v;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
