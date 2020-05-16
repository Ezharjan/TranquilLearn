package com.bjutsoft.studywithme.SoundMeter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.bjutsoft.studywithme.R;

public class SoundDiskView extends View {

    public static final int UPDATE_IMG = 1;
    private float scaleWidth, scaleHeight;
    private int newWidth, newHeight;
    private Matrix mMatrix = new Matrix();
    private Bitmap indicatorBitmap;
    private Paint paint = new Paint();
    static final long ANIMATION_INTERVAL = 20;

    public SoundDiskView(Context context) {
        super(context);
    }

    public SoundDiskView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void init() {
        Bitmap myBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.noise_index);
        int bitmapWidth = myBitmap.getWidth();
        int bitmapHeight = myBitmap.getHeight();
        newWidth = getWidth();
        newHeight = getHeight();
        scaleWidth = ((float) newWidth) / (float) bitmapWidth;  // 获取缩放比例
        scaleHeight = ((float) newHeight) / (float) bitmapHeight;  //获取缩放比例
        mMatrix.postScale(scaleWidth, scaleHeight);   //设置mMatrix的缩放比例
        indicatorBitmap = Bitmap.createBitmap(myBitmap, 0, 0, bitmapWidth, bitmapHeight, mMatrix, true);  //获取同等和背景宽高的指针图的bitmap

        paint = new Paint();
        paint.setTextSize(22 * ScreenUtil.getDensity(getContext()));
        paint.setAntiAlias(true);  //抗锯齿
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.WHITE);
    }

    public void refresh() {
        postInvalidateDelayed(ANIMATION_INTERVAL); //子线程刷新view
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (indicatorBitmap == null) {
            init();
        }
        mMatrix.setRotate(getAngle(World.dbCount), newWidth / 2, newHeight * 215 / 460);   //片相对位置
        canvas.drawBitmap(indicatorBitmap, mMatrix, paint);
        canvas.drawText((int) World.dbCount + " DB", newWidth / 2, newHeight * 36 / 46, paint); //图片相对位置

        //ShowComment(canvas);
        //postInvalidateDelayed(ANIMATION_INTERVAL*100000);
        canvas.drawText(EnvironmentRate((int) World.dbCount), 215, newHeight * 36 / 37, TextColor((int) World.dbCount)); //图片相对位置
    }

    private float getAngle(float db) {
        return (db - 85) * 5 / 3;
    }

    private String EnvironmentRate(int ratio) {
        if (ratio >= 120) {
            return "极度喧嚣，注意安全！";
//            return "Dangerous! Escape right now!";
        } else if (ratio >= 100 && ratio < 120) {
            return "中度喧嚣，捂住耳朵！";
//            return "Horrifying! Cover ears tightly!";
        } else if (ratio >= 90 && ratio < 100) {
            return "中度喧嚣，捂住耳朵！";
//            return "Unbearable! Long stay harmful!";
        } else if (ratio >= 80 && ratio < 90) {
            return "中度喧嚣，捂住耳朵！";
            // return "比较吵闹，长驻有害！";
//            return "Noisy! Calls inaudible!";
        } else if (ratio >= 60 && ratio < 80) {
            return "比较吵闹，长驻有害！";
//            return "Loud! Normal speaking acceptable!";
        } else if (ratio >= 50 && ratio < 60) {
            return "较为安静，适合学习！";
//            return "Normal! Reading suggested!";
        } else if (ratio >= 40 && ratio < 50) {
            return "较为安静，适合学习！";
//            return "Peaceful! Studying recommended!";
        } else if (ratio >= 30 && ratio < 40) {
            return "相对宁静，适合钻研！";
//            return "Quiet! Suitable for sleep!";
        } else if (ratio >= 20 && ratio < 30) {
            return "相对宁静，适合钻研！";
//            return "Silent! Good for meditation.";
        } else if (ratio >= 10 && ratio < 20) {
            return "非常宁静，有助冥想！";
//            return "Still! In extreme silence!";
        } else if (ratio >= 0 && ratio < 10) {
            return "非常宁静，有助冥想！";
//            return "Baby-hearing! Auditory threshold！";
        }
        System.out.println("ddddddddddddddddddddddddddddddddddd" + (int) World.dbCount);
        return "  ";
    }

    private Paint TextColor(int ratio) {
        Paint aPaint = new Paint();
        Paint bPaint = new Paint();
        Paint cPaint = new Paint();
        Paint dPaint = new Paint();

        aPaint.setColor(Color.RED);
        bPaint.setColor(Color.BLUE);
        cPaint.setColor(Color.YELLOW);
        dPaint.setColor(Color.GREEN);

        aPaint.setTypeface(Typeface.SERIF);
        bPaint.setTypeface(Typeface.SERIF);
        cPaint.setTypeface(Typeface.SERIF);
        dPaint.setTypeface(Typeface.SERIF);

        aPaint.setTextSize(65);
        bPaint.setTextSize(65);
        cPaint.setTextSize(65);
        dPaint.setTextSize(65);

        if (ratio >= 120) {
            return aPaint;
        } else if (ratio >= 100 && ratio < 120) {
            return aPaint;
        } else if (ratio >= 90 && ratio < 100) {
            return bPaint;
        } else if (ratio >= 80 && ratio < 90) {
            return bPaint;
        } else if (ratio >= 60 && ratio < 80) {
            return bPaint;
        } else if (ratio >= 50 && ratio < 60) {
            return cPaint;
        } else if (ratio >= 40 && ratio < 50) {
            return cPaint;
        } else if (ratio >= 30 && ratio < 40) {
            return cPaint;
        } else if (ratio >= 20 && ratio < 30) {
            return dPaint;
        } else if (ratio >= 10 && ratio < 20) {
            return dPaint;
        } else if (ratio >= 0 && ratio < 10) {
            return dPaint;
        }
        return paint;
    }
}

