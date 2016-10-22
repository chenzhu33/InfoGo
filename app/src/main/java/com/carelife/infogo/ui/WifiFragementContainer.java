package com.carelife.infogo.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.carelife.infogo.R;

/**
 * Created by wangrh on 2016/10/22.
 */
public class WifiFragementContainer extends BaseInfoFragment implements View.OnClickListener{

    private TextView manager;
    private TextView history;
    private FragmentManager fragmentManager;
    private static final int MANAGER = 0;
    private static final int HISTORY = 1;
    private int flag;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getChildFragmentManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.wifi_fragment_container, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        manager = (TextView)view.findViewById(R.id.wifi_manager);
        manager.setOnClickListener(this);
        history = (TextView)view.findViewById(R.id.wifi_history);
        history.setOnClickListener(this);
        manager.setTextColor(Color.RED);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, new WifiDetailFragment());
        transaction.commit();
        flag = MANAGER;
    }

    @Override
    public void onClick(View v) {
        if(v == manager){
            if(flag != MANAGER){
                clearColor();
                manager.setTextColor(Color.RED);
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.fragment_container, new WifiDetailFragment());
                transaction.commit();
                flag = MANAGER;
            }
        }else if(v == history){
            if(flag != HISTORY){
                clearColor();
                history.setTextColor(Color.RED);
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.fragment_container, new WifiHistoryFragment());
                transaction.commit();
                flag = HISTORY;
            }
        }
    }

    private void clearColor(){
        manager.setTextColor(Color.BLACK);
        history.setTextColor(Color.BLACK);
    }
}
