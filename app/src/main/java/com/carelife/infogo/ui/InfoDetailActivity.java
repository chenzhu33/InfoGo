package com.carelife.infogo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.ActionBar;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.carelife.infogo.R;

/**
 * An activity representing a single Position detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link InfoListActivity}.
 */
public class InfoDetailActivity extends BaseActivityWithTakePhoto {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            BaseInfoFragment fragment;
            int id = getIntent().getIntExtra(InfoListActivity.ARG_ITEM_ID, 1);
            switch (id) {
                case 1:
                    fragment = new LocationInfoFragment();
                    break;
                case 2:
                    fragment = new WifiFragementContainer();
                    break;
                case 3:
                    fragment = new BluetoothFragementContainer();
                    break;
                case 4:
                    fragment = new TakePhotoFragment();
                    break;
                case 5:
                    fragment = new IndoorPositionFragment();
                    break;
                default:
                    fragment = new LocationInfoFragment();
                    break;
            }
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.position_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, InfoListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
