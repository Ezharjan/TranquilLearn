package com.bjutsoft.studywithme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AppComponentFactory;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bjutsoft.studywithme.AlarmClock.AlarmClock;
import com.bjutsoft.studywithme.NotePad.NotePad;
import com.bjutsoft.studywithme.SoundMeter.SoundMeter;
import com.bjutsoft.studywithme.Surfer.SurfOnline;

import androidx.appcompat.app.AlertDialog;

import android.app.Service;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;

import org.litepal.LitePal;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    AlertDialog alert;
    Vibrator vibrator;
    public static long myInterval = 10000;//设置一个默认的久坐提醒时长，设置超长使其需要用户手动打开？设置开关？

    private SensorManager mSensorManager; //传感器管理
    public static float SENSITIVITY = 8; // SENSITIVITY灵敏度
    private float mLastValues[] = new float[3 * 2];
    private float mScale[] = new float[2];
    private float mYOffset;//位移
    private static long mEnd = 0;//运动相隔时间
    private static long mStart = 0;//运动开始时间
    private static long mResult = 0;//临时存储运动状态值

    private Handler handler = new Handler();
    private Runnable runnable;
    private static boolean dialogExist = false;

    private static long startTime = System.currentTimeMillis();//记录运行时的时间

    private static boolean isVibrating = false;
    private static boolean unlocked = false;//设置久坐提醒功能锁
    private static boolean keyPressed = true;

    // 最后加速度方向
    private float mLastDirections[] = new float[3 * 2];//最后的方向
    private float mLastExtremes[][] = {new float[3 * 2], new float[3 * 2]};
    private float mLastDiff[] = new float[3 * 2];
    private int mLastMatch = -1;

    private static RelativeLayout mBgLayout;
    private static String mBgId;

    /*自定义Toast*/
    public enum CustomToast {
        INSTANCE;// 实现单例
        private static Toast mToast;
        private static TextView mTvToast;

        public static void showToast(Context ctx, String content) {
            if (mToast == null) {
                mToast = new Toast(ctx);
                mToast.setGravity(Gravity.CENTER, 0, 0);//设置toast显示的位置——居中
                mToast.setDuration(Toast.LENGTH_SHORT);//设置toast显示的时长
                View _root = LayoutInflater.from(ctx).inflate(R.layout.toast_custom_common, null);//自定义样式，自定义布局文件
                mTvToast = (TextView) _root.findViewById(R.id.tvCustomToast);
                mToast.setView(_root);//设置自定义的view
            }
            mTvToast.setText(content);//设置文本
            mToast.show();//展示toast
        }

        public void cancelToast() {
            if (mToast != null) {
                mToast.cancel();
                mToast = null;
                mTvToast = null;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏锁定，注意必须放在生成布局之前！
        setContentView(R.layout.activity_main);

        mBgLayout = (RelativeLayout) findViewById(R.id.main_bg);
        //设置布局背景图片，将图片放置在drawable目录下就会嵌套app运行，至少要有一张default作为默认图片
        Random rand = new Random();
        int imgID = rand.nextInt(35);//随机生成0-35个的数字
        Log.d("TheVaueOf-Random", "----" + imgID);
        mBgId = "moto" + imgID;//将随机数作为图片名后缀
        int id = getResources().getIdentifier(mBgId, "drawable", getPackageName());//加载图片
        Log.d("TheVaueOfGOT-ID", "---" + id);
        Log.d("TheVaueOfMBG-ID", "---" + mBgId);
        if (id != 0) {
            mBgLayout.setBackgroundDrawable(getResources().getDrawable(id));
        } else {//异常情况下需要加载默认的图片
            mBgLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.waterfall));
        }

        LitePal.initialize(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 获取传感器的服务，初始化传感器
        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        // 用于判断是否计步的值
        int h = 480;
        mYOffset = h * 0.5f;
        mScale[0] = -(h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));//重力加速度
        mScale[1] = -(h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));//地球最大磁场

        //一次创建，多次使用；拒绝多次创建！
        alert = new AlertDialog.Builder(MainActivity.this).create();

        //进入线程计时
        runnable = new Runnable() {
            public void run() {
                if (unlocked) {//如果久坐提醒功能锁被打开则运行下方代码
                    isVibrating = false;
                    //testerOpen = false;
                    long currentTime = System.currentTimeMillis();
                    Log.d("我在", "" + (mResult));
                    Log.d("我在", "TimeContract-The contract of time is---" + (currentTime - startTime));
                    if ((currentTime - startTime) > myInterval) {//当前时间减去先前运行时的时间看其是否大于指定时间
                        vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
                        long[] pattern = {0, 500, 700, 500, 700, 500, 700, 500, 700, 500, 700}; // 0/ON/OFF/ON/OFF/ON...
                        vibrator.vibrate(pattern, -1);//永不重复
                        /*repeat 振动重复的模式:-1 为不重复
                                                0 为一直重复振动
                                                1 则是指从数组中下标为1的地方开始重复振动（不是振动一次！！！）
                                                2 从数组中下标为2的地方开始重复振动。
                                                .....
                        //pattern[]可以理解为 开启振动后 等待0.1s振动 振动2s 等待1s 振动1s 等待3s
                        long pattern[] = {100, 2000, 1000, 1000,3000};
                         */
                        isVibrating = true;

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
                                    CustomToast.showToast(MainActivity.this, "运动健康");
                                    //Toast.makeText(MainActivity.this, "运动健康", Toast.LENGTH_LONG).show();
                                }
                            });
                            Log.d("DialogShowed???", "----" + alert);
                            alert.show();
                            Log.d("DialogShowed!!!", "----" + alert);
                            dialogExist = true;
                        }
                    }
                }
                handler.postDelayed(this, myInterval);//设置提醒时间
                //postDelayed()方法安排一个Runnable对象到主线程队列中
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
        Log.d("我在", "我被销毁");
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
                            if (mEnd - mStart > 500) {
                                startTime = System.currentTimeMillis();
                                if (unlocked && isVibrating) {//判定锁是否为开且是否在振动
                                    vibrator.cancel();
                                    Log.d("DialogDissmissed???", "Dialog waiting to be dissmissed.");
                                    alert.dismiss();
                                }
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

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.to_long_seat_reminder:
                final EditText setDuration = new EditText(this);
                //下方设置布局的方法其实可以在XML中进行，这里直接在代码中运行时设置
                if (About.testerOpen) {
                    setDuration.setHint("输入测试秒数");
                } else {
                    setDuration.setHint("输入分钟数");
                }
                setDuration.setGravity(Gravity.CENTER_HORIZONTAL);//设置为居中位置
                setDuration.setInputType(InputType.TYPE_CLASS_NUMBER);//设置输入的方式为数字，弹出数字键盘
                InputFilter[] filters = new InputFilter[1];
                filters[0] = new InputFilter.LengthFilter(4) {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        //获取字符个数(一个中文算2个字符)
                        int destLen = MainActivity.getCharacterNum(dest.toString());
                        int sourceLen = MainActivity.getCharacterNum(source.toString());
                        if (destLen + sourceLen > 4) {
                            //使用自定义Toast
                            CustomToast.showToast(MainActivity.this, "最多输入四位");

                            //使用系统的Toast时，每次有新Toast被创建它就会进入队列中直到Toast队列为空才结束
                            //Toast toastRemindMax = Toast.makeText(getApplicationContext(), "最多输入四位", Toast.LENGTH_SHORT);
                            //toastRemindMax.setGravity(Gravity.CENTER, 0, 0);
                            //toastRemindMax.show();
                            return "";
                        }
                        return source;
                    }
                };
                setDuration.setFilters(filters);
                //setDuration.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});//限制输入的最多字符数
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    setDuration.setBackground(null);
                }
                //setDuration.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                //设置如果字符输入太长时以走马灯的方式显示

                new AlertDialog.Builder(this)
                        .setTitle("请输入久坐提醒时长:")
                        .setIcon(R.mipmap.long_seat_reminder).setView(setDuration)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String checker = setDuration.getText().toString();//一定要记得先getText()然后再转
                                boolean matchesInt = checker.matches("[0-9]+");//用正则的方式检测输入是否为整数
                                Log.d("matches和checker", "MatchesInt---" + matchesInt + ";" + checker);
                                if (matchesInt) {
                                    int getDuration = Integer.parseInt(setDuration.getText().toString());
                                    Log.d("用户", "具体的转换值" + getDuration);
                                    Log.d("用户", "具体的获取myIntetrval数值" + myInterval);
                                    if (getDuration != 0 && About.testerOpen == false) {
                                        myInterval = getDuration * 1000 * 60;//将所获得的时间转化成分钟
                                        CustomToast.showToast(MainActivity.this, "已设置久坐提醒时长为" + getDuration + "分钟");

                                        //Toast.makeText(MainActivity.this, "已设置久坐提醒时长为" + getDuration + "分钟", Toast.LENGTH_SHORT).show();
                                        unlocked = true;
                                    } else if (getDuration != 0 && About.testerOpen == true) {
                                        myInterval = getDuration * 1000;//将所获得的时间转化成秒
                                        CustomToast.showToast(MainActivity.this, "已设置久坐提醒时长为" + getDuration + "秒");
                                        //Toast.makeText(MainActivity.this, "已设置久坐提醒时长为" + getDuration + "秒", Toast.LENGTH_SHORT).show();
                                        unlocked = true;
                                    } else if (getDuration == 0) {
                                        CustomToast.showToast(MainActivity.this, "请输入大于0的整数！");
                                        //Toast.makeText(MainActivity.this, "请输入大于0的整数！", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    if (checker.equals("版权")) {//在InputType_NUMBER存在的情况下不会运行这个判断
                                        Toast.makeText(MainActivity.this, "本应用由艾孜尔江设计开发", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MainActivity.this, "输入值为空，久坐提醒未激活！", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            }
                        }).setNegativeButton("取消", null).show();
                break;
            case R.id.to_sound_meter:
                Intent intent_soundMeter = new Intent(MainActivity.this, SoundMeter.class);
                startActivity(intent_soundMeter);
                break;
            case R.id.to_surfer:
                Intent intent_surfOnline = new Intent(MainActivity.this, SurfOnline.class);
                startActivity(intent_surfOnline);
                break;
            case R.id.to_alarm_clock:
                Intent intent_alarmClock = new Intent(MainActivity.this, AlarmClock.class);
                startActivity(intent_alarmClock);
                break;
            case R.id.to_about:
                Intent intent_about = new Intent(MainActivity.this, About.class);
                startActivity(intent_about);
                break;
            case R.id.my_note_pad:
                Intent intent_NotePad = new Intent(MainActivity.this, NotePad.class);
                startActivity(intent_NotePad);
                break;
            default:
                break;
        }
        return true;
    }

    public static int getCharacterNum(String content) {
        if (content.equals("") || content == null) {
            return 0;
        } else {
            return content.length() + getChineseNum(content);
        }
    }

    public static int getChineseNum(String s) {
        int num = 0;
        char[] myChar = s.toCharArray();
        for (int i = 0; i < myChar.length; i++) {
            if ((char) (byte) myChar[i] != myChar[i]) {
                num++;
            }
        }
        return num;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //判断按下的键是否是返回键
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (keyPressed) {
                Toast.makeText(MainActivity.this, "再按一次退出应用", Toast.LENGTH_SHORT).show();
                keyPressed = false;

                //使用定时器修改keyPress的值，按下两秒后将keyPress设为true
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        keyPressed = true;
                    }
                }, 2000);

            } else {
                //关闭页面
                finish();
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
