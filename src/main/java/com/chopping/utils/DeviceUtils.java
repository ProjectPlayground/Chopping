package com.chopping.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build.VERSION_CODES;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.hardware.display.DisplayManagerCompat;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;

import com.android.internal.telephony.ITelephony;
import com.chopping.application.LL;
import com.chopping.exceptions.OperationFailException;

import static com.chopping.utils.Consts.hdpi;
import static com.chopping.utils.Consts.ldpi;
import static com.chopping.utils.Consts.mdpi;
import static com.chopping.utils.Consts.tv;
import static com.chopping.utils.Consts.xhdpi;
import static com.chopping.utils.Consts.xxhdpi;
import static com.chopping.utils.Consts.xxxhdpi;

/**
 * Utils for device i.e access a logical of device-id, get screen size etc.
 */
public final class DeviceUtils {

	/**
	 * Get resolution of screen which kind of dpi will be detected.
	 *
	 * @param cxt
	 * 		{@link  android.content.Context} .
	 *
	 * @return {@link com.chopping.utils.Consts} .
	 */
	public static Consts getDeviceResolution(Context cxt) {
		int density = cxt.getResources().getDisplayMetrics().densityDpi;
		switch (density) {
		case DisplayMetrics.DENSITY_MEDIUM:
			return mdpi;
		case DisplayMetrics.DENSITY_HIGH:
			return hdpi;
		case DisplayMetrics.DENSITY_LOW:
			return ldpi;
		case DisplayMetrics.DENSITY_XHIGH:
			return xhdpi;
		case DisplayMetrics.DENSITY_TV:
			return tv;
		case DisplayMetrics.DENSITY_XXHIGH:
			return xxhdpi;
		case DisplayMetrics.DENSITY_XXXHIGH:
			return xxxhdpi;
		default:
			return Consts.UNKNOWN;
		}
	}

	/**
	 * Get {@link com.chopping.utils.DeviceUtils.ScreenSize} of default/first display.
	 *
	 * @param cxt
	 * 		{@link  android.content.Context} .
	 *
	 * @return A {@link com.chopping.utils.DeviceUtils.ScreenSize}.
	 */
	public static ScreenSize getScreenSize(Context cxt) {
		return getScreenSize(cxt, 0);
	}

	/**
	 * Get {@link com.chopping.utils.DeviceUtils.ScreenSize} with different {@code displayIndex} .
	 *
	 * @param cxt
	 * 		{@link android.content.Context} .
	 * @param displayIndex
	 * 		The index of display.
	 *
	 * @return A {@link com.chopping.utils.DeviceUtils.ScreenSize}.
	 */
	public static ScreenSize getScreenSize(Context cxt, int displayIndex) {
		DisplayMetrics displaymetrics = new DisplayMetrics();
		Display[] displays = DisplayManagerCompat.getInstance(cxt).getDisplays();
		Display display = displays[displayIndex];
		display.getMetrics(displaymetrics);
		return new ScreenSize(displaymetrics.widthPixels, displaymetrics.heightPixels);
	}

	/**
	 * Screen-size in pixels.
	 */
	public static class ScreenSize {
		public int Width;
		public int Height;

		public ScreenSize(int _width, int _height) {
			Width = _width;
			Height = _height;
		}
	}

	/**
	 * Turn on/off the mobile data.
	 * <p/>
	 * <b>Unofficial implementation.</b>
	 * <p/>
	 * See. <a href="http://stackoverflow.com/questions/12535101/how-can-i-turn-off-3g-data-programmatically-on-android">StackOverflow</a>
	 *
	 * @param context
	 * 		{@link android.content.Context}.
	 * @param enabled
	 * 		{@code true} Turn on, {@code false} turn off.
	 *
	 * @return {@code true} if attempted change is success, {@code false} if already on or off.
	 *
	 * @throws OperationFailException
	 * 		Error fires when the operation is not success.
	 */
	public static boolean setMobileDataEnabled(Context context, boolean enabled) throws OperationFailException {
		boolean success;
		Boolean isMobileDataEnabled = isMobileDataEnabled(context);
		if (isMobileDataEnabled == null) {
			throw new OperationFailException();
		} else if ((isMobileDataEnabled && enabled) || (!isMobileDataEnabled && !enabled)) {
			success = false;
		} else {
			try {
				final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(
						Context.CONNECTIVITY_SERVICE);
				final Class conmanClass = Class.forName(conman.getClass().getName());
				final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
				iConnectivityManagerField.setAccessible(true);
				final Object iConnectivityManager = iConnectivityManagerField.get(conman);
				final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
				final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod(
						"setMobileDataEnabled", Boolean.TYPE);
				setMobileDataEnabledMethod.setAccessible(true);

				setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
				success = true;
			} catch (Exception ex) {
				LL.w(ex.toString());
				throw new OperationFailException();
			}
		}
		return success;
	}

	/**
	 * Check whether mobile data is enable or not.
	 * <p/>
	 * <b>Unofficial implementation.</b>
	 * <p/>
	 * See. <a href="http://stackoverflow.com/questions/8224097/how-to-check-if-mobile-network-is-enabled-disabled">StackOverflow</a>
	 *
	 * @return {code null} if unconfirmed, some errors happened, {@code true} if already enabled, {@code false} disable.
	 */
	private static Boolean isMobileDataEnabled(Context cxt) {
		Object connectivityService = cxt.getSystemService(Context.CONNECTIVITY_SERVICE);
		ConnectivityManager cm = (ConnectivityManager) connectivityService;

		try {
			Class<?> c = Class.forName(cm.getClass().getName());
			Method m = c.getDeclaredMethod("getMobileDataEnabled");
			m.setAccessible(true);
			return (Boolean) m.invoke(cm);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Turn on/off the wifi. Call {@link android.net.wifi.WifiManager#setWifiEnabled(boolean)} directly.
	 *
	 * @param context
	 * 		{@link android.content.Context}.
	 * @param enabled
	 * 		{@code true} Turn on, {@code false} turn off.
	 *
	 * @return {@code true} if attempted change is success. {@code false} if wifi is already enable or disable.
	 *
	 * @throws OperationFailException
	 * 		Error fires when the operation is not success.
	 */
	public static boolean setWifiEnabled(Context context, boolean enabled) throws OperationFailException {
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "setWifiEnabled");
		wl.acquire();
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if ((enabled && wifiManager.isWifiEnabled()) || (!enabled && !wifiManager.isWifiEnabled())) {
			wl.release();
			return false;
		}
		if (wifiManager.setWifiEnabled(enabled)) {
			wl.release();
			return true;
		} else {
			wl.release();
			throw new OperationFailException();
		}
	}



	/**
	 * Reject incoming call.
	 * @param cxt {@link android.content.Context}.
	 */
	public static void rejectIncomingCall(Context cxt ) {
		TelephonyManager tm = (TelephonyManager) cxt.getSystemService(Context.TELEPHONY_SERVICE);
		try {
			Class c = Class.forName(tm.getClass().getName());
			Method m = c.getDeclaredMethod("getITelephony");
			m.setAccessible(true);
			ITelephony telephonyService = (ITelephony) m.invoke(tm);
			telephonyService.endCall();
			LL.i("HANG UP");
		} catch (Exception e) {
			LL.e("Error when reject incoming: " + e.getMessage());
		}
	}

	/**
	 * Set device bluetooth enable or not.
	 *
	 * @param enabled
	 * 		{@code true} if enable.
	 *
	 * @return {@code false} if no need to change state, bluetooth is already enable or disable. {@code true} if any
	 * attempted change is success.
	 *
	 * @throws OperationFailException
	 */
	public static boolean setBluetoothEnabled( boolean enabled) throws OperationFailException {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		boolean isEnabled = bluetoothAdapter.isEnabled();
		if (enabled && !isEnabled) {
			if (!bluetoothAdapter.enable()) {
				throw new OperationFailException();
			}
			return true;
		} else if (!enabled && isEnabled) {
			if (!bluetoothAdapter.disable()) {
				throw new OperationFailException();
			}
			return true;
		}
		// No need to change bluetooth state.
		return false;
	}

	/**
	 * Set different ring mode. Call {@link android.media.AudioManager#setRingerMode(int)}.
	 *
	 * @param cxt
	 * 		{@link android.content.Context}.
	 * @param mode
	 * 		Different mode:<li>{@link android.media.AudioManager#RINGER_MODE_SILENT} for mute.</li> <li>{@link android
	 * 		.media.AudioManager#RINGER_MODE_VIBRATE} for vibration.</li><li>{@link android.media
	 * 		.AudioManager#RINGER_MODE_NORMAL} for sound.</li>
	 *
	 * @return {@code false} if the mode that will be switched is already on.
	 */
	public static boolean setRingMode(Context cxt, int mode) {
		AudioManager audioManager = (AudioManager) cxt.getSystemService(Context.AUDIO_SERVICE);
		if (audioManager.getRingerMode() == mode) {
			return false;
		}
		audioManager.setRingerMode(mode);
		return true;
	}

	/**
	 * Brightness levels, only max, medium, min.
	 *
	 * @author Xinyue Zhao
	 */
	public enum Brightness {
		MAX(1f, 255), MEDIUM(0.5f, (255 + 10) / 2), MIN(0.1f, 10);

		public float valueF;
		public int valueI;

		Brightness(float valueF, int valueI) {
			this.valueF = valueF;
			this.valueI = valueI;
		}
	}

	/**
	 * Set system brightness and current window brightness.
	 *
	 * @param cxt
	 * 		{@link android.content.Context}.
	 * @param brightness
	 * 		The brightness level wanna.
	 */
	public static void setBrightness(Context cxt, Brightness brightness) {
		ContentResolver cr = cxt.getContentResolver();
		// To handle the auto.
		Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS_MODE,
				Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
		//Get the current system brightness.
		//int currentBrightness = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS);
		int currentBrightness = brightness.valueI;

		/*
		 * Set whole system brightness.
		 */

		//Set the system brightness using the brightness variable value.
		Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS, currentBrightness);

		Intent i = new Intent("com.chopping.brightness.action.REFRESH");
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		cxt.startActivity(i);
	}

	/**@hiden*/
	/**
	 * Turn on GPS, <b>it is unofficial method to do it.  Not working in latest android version 4.4</b>
	 * See. <a href="http://stackoverflow.com/questions/15426144/turning-on-and-off-gps-programmatically-in-android-4-0-and-above">StackOverflow</a>
	 * @param cxt {@link android.content.Context}.
	 */
	public static void turnGPSOn(Context  cxt) {
		String provider = Settings.Secure.getString(cxt.getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if(!provider.contains("gps")&&
				android.os.Build.VERSION.SDK_INT < VERSION_CODES.KITKAT) { //if gps is enabled
			Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
			intent.putExtra("enabled", true);
			cxt.sendBroadcast(intent);
		}

		if(!provider.contains("gps")){ //if gps is disabled
			final Intent poke = new Intent();
			poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
			poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
			poke.setData(Uri.parse("3"));
			cxt.sendBroadcast(poke);
		}
	}

	/**@hiden*/
	/**
	 * Turn off GPS, <b>it is unofficial method to do it.  Not working in latest android version 4.4</b>
	 * See. <a href="http://stackoverflow.com/questions/15426144/turning-on-and-off-gps-programmatically-in-android-4-0-and-above">StackOverflow</a>
	 * @param cxt {@link android.content.Context}.
	 */
	public static void turnGPSOff(Context  cxt) {
		String provider = Settings.Secure.getString(cxt.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if(provider.contains("gps")&&
				android.os.Build.VERSION.SDK_INT < VERSION_CODES.KITKAT) { //if gps is enabled
			Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
			intent.putExtra("enabled", false);
			cxt.sendBroadcast(intent);
		}

		if(provider.contains("gps")){ //if gps is enabled
			final Intent poke = new Intent();
			poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
			poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
			poke.setData(Uri.parse("3"));
			cxt.sendBroadcast(poke);
		}
	}


	/**
	 * This method converts device specific pixels to density independent pixels.
	 *
	 * @param context Context to get resources and device specific display metrics
	 * @param px A value in px (pixels) unit. Which we need to convert into db
	 * @return A float value to represent dp equivalent to px value
	 */
	public static float px2dp( Context context, float px){
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float dp = px / (metrics.densityDpi / 160f);
		return dp;
	}


	/**
	 * This method converts dp unit to equivalent pixels, depending on device density.
	 *
	 * @param context Context to get resources and device specific display metrics
	 * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
	 * @return A float value to represent px equivalent to dp depending on device density
	 */
	public static float dp2px(Context context, float dp ){
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float px = dp * (metrics.densityDpi / 160f);
		return px;
	}

	/**
	 *
	 * Is device plugged in or not.
	 */
	public static boolean isPlugged(Context context) {
		Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
	}
}
