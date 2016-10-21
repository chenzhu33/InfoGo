package com.carelife.infogo.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

public class LinkWifi {

	private WifiManager wifiManager;

	public enum WifiCipherType {
		WIFI_CIPHER_WEP, WIFI_CIPHER_WPA_EAP, WIFI_CIPHER_WPA_PSK, WIFI_CIPHER_WPA2_PSK, WIFI_CIPHER_NOPASS
	}

	public LinkWifi(Context context) {
		wifiManager = (WifiManager) context
				.getSystemService(Service.WIFI_SERVICE);
	}
	
	public boolean checkWifiState() {
		boolean isOpen = true;
		int wifiState = wifiManager.getWifiState();

		if (wifiState == WifiManager.WIFI_STATE_DISABLED
				|| wifiState == WifiManager.WIFI_STATE_DISABLING
				|| wifiState == WifiManager.WIFI_STATE_UNKNOWN
				|| wifiState == WifiManager.WIFI_STATE_ENABLING) {
			isOpen = false;
		}

		return isOpen;
	}


	public boolean ConnectToNetID(int netID) {
		return wifiManager.enableNetwork(netID, true);
	}

	public WifiConfiguration IsExsits(String SSID) {
		List<WifiConfiguration> existingConfigs = wifiManager
				.getConfiguredNetworks();

		for (WifiConfiguration existingConfig : existingConfigs) {

			if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
				return existingConfig;
			}
		}
		return null;
	}

	public int CreateWifiInfo2(ScanResult wifiinfo, String pwd) {
		WifiCipherType type;

		if (wifiinfo.capabilities.contains("WPA2-PSK")) {
			type = WifiCipherType.WIFI_CIPHER_WPA2_PSK;
		} else if (wifiinfo.capabilities.contains("WPA-PSK")) {
			type = WifiCipherType.WIFI_CIPHER_WPA_PSK;
		} else if (wifiinfo.capabilities.contains("WPA-EAP")) {
			type = WifiCipherType.WIFI_CIPHER_WPA_EAP;
		} else if (wifiinfo.capabilities.contains("WEP")) {
			type = WifiCipherType.WIFI_CIPHER_WEP;
		} else {
			type = WifiCipherType.WIFI_CIPHER_NOPASS;
		}

		WifiConfiguration config = CreateWifiInfo(wifiinfo.SSID,
				wifiinfo.BSSID, pwd, type);
		if (config != null) {
			return wifiManager.addNetwork(config);
		} else {
			return -1;
		}
	}

	public WifiConfiguration setMaxPriority(WifiConfiguration config) {
		int priority = getMaxPriority() + 1;
		if (priority > 99999) {
			priority = shiftPriorityAndSave();
		}

		config.priority = priority;
		System.out.println("priority=" + priority);

		wifiManager.updateNetwork(config);

		return config;
	}

	public WifiConfiguration CreateWifiInfo(String SSID, String BSSID,
			String password, WifiCipherType type) {

		int priority;

		WifiConfiguration config = this.IsExsits(SSID);
		if (config != null) {
			return setMaxPriority(config);
		}

		config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		config.SSID = "\"" + SSID + "\"";
		config.status = WifiConfiguration.Status.ENABLED;

		priority = getMaxPriority() + 1;
		if (priority > 99999) {
			priority = shiftPriorityAndSave();
		}

		config.priority = priority;
		if (type == WifiCipherType.WIFI_CIPHER_NOPASS) {
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		} else if (type == WifiCipherType.WIFI_CIPHER_WEP) {
			config.preSharedKey = "\"" + password + "\"";

			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.SHARED);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedGroupCiphers
					.set(WifiConfiguration.GroupCipher.WEP104);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		} else if (type == WifiCipherType.WIFI_CIPHER_WPA_EAP) {

			config.preSharedKey = "\"" + password + "\"";
			config.hiddenSSID = true;
			config.status = WifiConfiguration.Status.ENABLED;
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);

			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.TKIP);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.CCMP);
			config.allowedProtocols.set(WifiConfiguration.Protocol.RSN
					| WifiConfiguration.Protocol.WPA);

		} else if (type == WifiCipherType.WIFI_CIPHER_WPA_PSK) {

			config.preSharedKey = "\"" + password + "\"";
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.TKIP);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.CCMP);
			config.allowedProtocols.set(WifiConfiguration.Protocol.RSN
					| WifiConfiguration.Protocol.WPA);

		} else if (type == WifiCipherType.WIFI_CIPHER_WPA2_PSK) {

			config.preSharedKey = "\"" + password + "\"";
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);

			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.TKIP);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.CCMP);
			config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
			config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

		} else {
			return null;
		}

		return config;
	}

	private int getMaxPriority() {
		List<WifiConfiguration> localList = this.wifiManager
				.getConfiguredNetworks();
		int i = 0;
		Iterator<WifiConfiguration> localIterator = localList.iterator();
		while (true) {
			if (!localIterator.hasNext())
				return i;
			WifiConfiguration localWifiConfiguration = localIterator
					.next();
			if (localWifiConfiguration.priority <= i)
				continue;
			i = localWifiConfiguration.priority;
		}
	}

	private int shiftPriorityAndSave() {
		List<WifiConfiguration> localList = this.wifiManager
				.getConfiguredNetworks();
		sortByPriority(localList);
		int i = localList.size();
		for (int j = 0;; ++j) {
			if (j >= i) {
				this.wifiManager.saveConfiguration();
				return i;
			}
			WifiConfiguration localWifiConfiguration =  localList.get(j);
			localWifiConfiguration.priority = j;
			this.wifiManager.updateNetwork(localWifiConfiguration);
		}
	}

	private void sortByPriority(List<WifiConfiguration> paramList) {
		Collections.sort(paramList, new WifiManagerCompare());
	}

	class WifiManagerCompare implements Comparator<WifiConfiguration> {
		public int compare(WifiConfiguration paramWifiConfiguration1,
				WifiConfiguration paramWifiConfiguration2) {
			return paramWifiConfiguration1.priority
					- paramWifiConfiguration2.priority;
		}
	}
}
