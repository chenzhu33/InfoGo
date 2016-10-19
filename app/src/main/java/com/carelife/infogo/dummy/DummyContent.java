package com.carelife.infogo.dummy;

import com.carelife.infogo.dom.BaseInfo;
import com.carelife.infogo.dom.BlueToothInfo;
import com.carelife.infogo.dom.LocationInfo;
import com.carelife.infogo.dom.WifiInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<BaseInfo> ITEMS = new ArrayList<>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<Integer, BaseInfo> ITEM_MAP = new HashMap<>();

    static {
        // Add some sample items.
        addItem(new LocationInfo());
        addItem(new WifiInfo());
        addItem(new BlueToothInfo());
    }

    private static void addItem(BaseInfo item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.getId(), item);
    }
}
