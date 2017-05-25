package com.example.hw.fmseekbar2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private FmSeekbar mHeightWheelView;
    private TextView mHeightValue;

    private float mHeight = 870;//默认初始化值


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {

        mHeightWheelView = (FmSeekbar) findViewById(R.id.scaleWheelView_height);
        mHeightValue = (TextView) findViewById(R.id.tv_user_height_value);

        mHeightValue.setText(mhzCastStr((int) mHeight) + "");

        float maxHeight = 1080;
        float minHeight = 870;
        mHeightWheelView.initViewParam(mHeight, maxHeight, minHeight);
        mHeightWheelView.setValueChangeListener(new FmSeekbar.OnValueChangeListener() {
            @Override
            public void onValueChange(float value) {
                mHeightValue.setText(mhzCastStr((int) value) + "");
                mHeight = value;
            }
        });


        /**
         *测试：设置进度条值
         */
        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHeightWheelView.setValue(1000);
                mHeightValue.setText("1000");
            }
        });

    }

    //频率值转换成String
    public static String mhzCastStr(int mhz) {
        DecimalFormat df = new DecimalFormat("00");
        return df.format(mhz / 10) + "." + String.valueOf(mhz % 10);
    }
}
