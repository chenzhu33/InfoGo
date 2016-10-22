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
import com.carelife.infogo.dom.BluetoothModel;

import java.util.List;

/**
 * Created by wangrh on 2016/10/22.
 */
public class BluetoothHistroyFragment extends BaseInfoFragment {

    private ExpandableListView expandableListView;
    private List<BluetoothModel> bluetoothModelList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bluetoothModelList = new Select().from(BluetoothModel.class).execute();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bluetooth_history_page, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        expandableListView = (ExpandableListView)view.findViewById(R.id.bluetooth_history_list);
        expandableListView.setAdapter(new MyAdapter());
    }


    class MyAdapter extends BaseExpandableListAdapter{

        @Override
        public int getGroupCount() {
            return bluetoothModelList.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return bluetoothModelList.get(groupPosition).getData().split("\\|").length;
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
            textView.append("          Timestamp: "+bluetoothModelList.get(groupPosition).getTimestamp());
            textView.append("\n");
            textView.append("          Latitude: "+bluetoothModelList.get(groupPosition).getLatitude());
            textView.append("    ");
            textView.append("Longitude: "+bluetoothModelList.get(groupPosition).getLongitude());
            textView.append("\n");
            return textView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            String child = bluetoothModelList.get(groupPosition).getData().split("\\|")[childPosition];
            String [] datas = child.split("&&");
            TextView textView = new TextView(getContext());
            textView.append("    Name: "+datas[0]);
            textView.append("    ");
            textView.append("Address: "+datas[1]);
            textView.append("\n");
            return textView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

    }
}
