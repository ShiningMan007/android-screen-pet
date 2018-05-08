package com.example.administrator.screenpet;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.view.View;

public class TimeDrawingView extends View {
    Typeface tf;
    int hours, minutes, seconds, weekday, date;
    float battery;

    Paint mBackgroudPaint, mTextPaint, mTextPaintBack;
    private Context mContext;


    public TimeDrawingView(Context context, int hours, int minutes, int seconds, int weekday, int date, float battery) {
        super(context);
        mContext = context;

        tf = Typeface.createFromAsset(mContext.getAssets(), "DS-DIGIB.TTF");
        mBackgroudPaint = new Paint();
        mBackgroudPaint.setColor(ContextCompat.getColor(mContext, R.color.background));

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(ContextCompat.getColor(mContext, R.color.text));
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(getResources().getDimension(R.dimen.text_size));
        mTextPaint.setTypeface(tf);

        mTextPaintBack = new Paint();
        mTextPaintBack.setAntiAlias(true);
        mTextPaintBack.setColor(ContextCompat.getColor(mContext, R.color.text_back));
        mTextPaintBack.setTextAlign(Paint.Align.CENTER);
        mTextPaintBack.setTextSize(getResources().getDimension(R.dimen.text_size));
        mTextPaintBack.setTypeface(tf);

        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.weekday = weekday;
        this.date = date;
        this.battery = battery;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float height = canvas.getHeight();
        float width = canvas.getWidth();
        canvas.drawRect(0,0, width, height, mBackgroudPaint);

        float CenterX = width/2;
        float CenterY = height/2;

        int cur_hour = hours;
        String AMPM = "AM";
        if (hours == 0){
            cur_hour = 12;
        }
        if (hours > 12){
            cur_hour = cur_hour - 12;
            AMPM = "PM";
        }

        String text = String.format("%02d:%02d:%02d", cur_hour, this.minutes, this.seconds);
        String day_of_week= "";
        switch (this.weekday){
            case 1:
                day_of_week = "MON";
                break;
            case 2:
                day_of_week = "TUE";
                break;
            case 3:
                day_of_week = "WED";
                break;
            case 4:
                day_of_week = "THU";
                break;
            case 5:
                day_of_week = "FRI";
                break;
            case 6:
                day_of_week = "SAT";
                break;
            case 7:
                day_of_week = "SUN";
                break;
        }
        String text2 = String.format("DATE: %s %d", day_of_week, date);
        String batteryLevel = "BATTERY: "+(int)battery+"%";



        mTextPaint.setColor(ContextCompat.getColor(mContext, R.color.text));
        mTextPaint.setTextSize(getResources().getDimension(R.dimen.text_size));
        canvas.drawText(text, CenterX, CenterY, mTextPaint);

        mTextPaint.setColor(ContextCompat.getColor(mContext, R.color.text));
        mTextPaint.setTextSize(getResources().getDimension(R.dimen.text_size_small));
        canvas.drawText(batteryLevel + " "+ text2,
                CenterX,
                CenterY + getResources().getDimension(R.dimen.text_size_small),
                mTextPaint);

    }
}

