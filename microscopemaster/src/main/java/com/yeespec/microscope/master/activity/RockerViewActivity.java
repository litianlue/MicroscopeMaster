package com.yeespec.microscope.master.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.yeespec.R;
import com.yeespec.microscope.widget.rocker.RockerView;

public class RockerViewActivity extends Activity {

    private TextView mLogLeft;
    private TextView mLogRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rocker);
        mLogLeft = (TextView) findViewById(R.id.log_left);
        mLogRight = (TextView) findViewById(R.id.log_right);

        RockerView rockerViewLeft = (RockerView) findViewById(R.id.rockerView_left);
        if (rockerViewLeft != null) {
            rockerViewLeft.setCallBackMode(RockerView.CallBackMode.CALL_BACK_MODE_STATE_CHANGE);
            rockerViewLeft.setOnShakeListener(RockerView.DirectionMode.DIRECTION_8, new RockerView.OnShakeListener() {
                @Override
                public void onStart() {
                    mLogLeft.setText(null);
                }

                @Override
                public void direction(RockerView.Direction direction) {
                    mLogLeft.setText("摇动方向 : " + getDirection(direction));
                }

                @Override
                public void onFinish() {
                    mLogLeft.setText(null);
                }
            });
        }

        RockerView rockerViewRight = (RockerView) findViewById(R.id.rockerView_right);
        if (rockerViewRight != null) {
            rockerViewRight.setOnAngleChangeListener(new RockerView.OnAngleChangeListener() {
                @Override
                public void onStart() {
                    mLogRight.setText(null);
                }

                @Override
                public void angle(double angle, float lenXY, float lenX, float lenY) {
                    mLogRight.setText("摇动角度 : " + angle + " 比例力度 : " + lenXY);
                }

                @Override
                public void onFinish() {
                    mLogRight.setText(null);
                }
            });
        }
    }

    private String getDirection(RockerView.Direction direction) {
        String message = null;
        switch (direction) {
            case DIRECTION_LEFT:
                message = "左";
                break;
            case DIRECTION_RIGHT:
                message = "右";
                break;
            case DIRECTION_UP:
                message = "上";
                break;
            case DIRECTION_DOWN:
                message = "下";
                break;
            case DIRECTION_UP_LEFT:
                message = "左上";
                break;
            case DIRECTION_UP_RIGHT:
                message = "右上";
                break;
            case DIRECTION_DOWN_LEFT:
                message = "左下";
                break;
            case DIRECTION_DOWN_RIGHT:
                message = "右下";
                break;
            default:
                break;
        }
        return message;
    }
}
