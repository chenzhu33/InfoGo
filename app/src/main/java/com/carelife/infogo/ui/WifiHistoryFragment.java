package com.carelife.infogo.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.carelife.infogo.R;
import com.carelife.infogo.dom.WifiModel;

import java.util.List;

/**
 * Created by wangrh on 2016/10/22.
 */
public class WifiHistoryFragment extends BaseInfoFragment {

    private ExpandableListView expandableListView;
    private List<WifiModel> wifiModelList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wifiModelList = new Select().from(WifiModel.class).execute();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.wifi_history_page, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        expandableListView = (ExpandableListView)view.findViewById(R.id.wifi_history_list);
        expandableListView.setAdapter(new MyAdapter());
    }


    class MyAdapter extends BaseExpandableListAdapter{

        @Override
        public int getGroupCount() {
            return wifiModelList.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return wifiModelList.get(groupPosition).getData().split("\\|").length;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return null;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return null;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

            TextView textView = new TextView(getContext());
            textView.setTextColor(Color.RED);
            textView.append("\n");
            textView.append("          Timestamp: "+wifiModelList.get(groupPosition).getTimestamp());
            textView.append("\n");
            textView.append("          Latitude: "+wifiModelList.get(groupPosition).getLatitude());
            textView.append("    ");
            textView.append("Longitude: "+wifiModelList.get(groupPosition).getLongitude());
            textView.append("\n");
            return textView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            String child = wifiModelList.get(groupPosition).getData().split("\\|")[childPosition];
            String [] datas = child.split("&&");
            TextView textView = new TextView(getContext());
            textView.append("    BSSID: "+datas[0]);
            textView.append("    ");
            textView.append("SSID: "+datas[1]);
            textView.append("\n");
            textView.append("    Level: "+datas[2]);
            textView.append("    ");
            textView.append("Capabilities: "+datas[3]);
            textView.append("\n");
            return textView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

    }
}
