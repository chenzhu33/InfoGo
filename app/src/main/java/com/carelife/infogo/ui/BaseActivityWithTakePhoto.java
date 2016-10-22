package com.carelife.infogo.ui;

import android.support.v7.app.AppCompatActivity;

import com.carelife.infogo.utils.LocationProducer;

public abstract class BaseActivityWithTakePhoto extends AppCompatActivity{

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocationProducer.getInstance(this).stop();
    }
}
