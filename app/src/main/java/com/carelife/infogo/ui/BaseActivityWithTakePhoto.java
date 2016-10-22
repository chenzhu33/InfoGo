package com.carelife.infogo.ui;

import android.support.v7.app.AppCompatActivity;

import com.carelife.infogo.utils.LocationProducer;

/**
 * Created by wangrh on 2016/10/21.
 */
public abstract class BaseActivityWithTakePhoto extends AppCompatActivity{

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocationProducer.getInstance(this).stop();
    }
}
