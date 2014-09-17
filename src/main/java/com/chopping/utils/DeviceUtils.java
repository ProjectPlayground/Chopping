package com.chopping.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.v4.hardware.display.DisplayManagerCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

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
	 * @return {code null} if unconfirmed, some errors happened, {@code true} if change is success, {@code false} if
	 * already on or off.
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
	 * @return {@code true} if change is success. {@code false} if wifi is already enable or disable.
	 *
	 * @throws OperationFailException
	 * 		Error fires when the operation is not success.
	 */
	public static boolean setWifiEnabled(Context context, boolean enabled) throws OperationFailException {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if ((enabled && wifiManager.isWifiEnabled()) || (!enabled && !wifiManager.isWifiEnabled())) {
			return false;
		}
		if (wifiManager.setWifiEnabled(enabled)) {
			return true;
		} else {
			throw new OperationFailException();
		}
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

	/**@hide*/
	/**
	 * Set system brightness.
	 *
	 * @param cxt
	 * 		{@link android.content.Context}.
	 * @param window
	 * 		{@link android.view.Window}.
	 * @param brightness
	 * 		From 0 to 1(max).
	 *
	 * @throws OperationFailException
	 * 		Error fires when the operation is not success.
	 */
	public static void setBrightness(Context cxt, Window window, float brightness) throws OperationFailException {
		try {
			//Get the content resolver
			ContentResolver cr = cxt.getContentResolver();
			// To handle the auto.
			Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS_MODE,
					Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
			//Get the current system brightness
			int currentBrightness = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS);
			//Set the system brightness using the brightness variable value.
			Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS, currentBrightness);
			//Get the current window attributes.
			WindowManager.LayoutParams params = window.getAttributes();
			//Set the brightness of this window.
			params.screenBrightness = brightness;
			//Apply attribute changes to this window.
			window.setAttributes(params);
		} catch (SettingNotFoundException e) {
			Log.e("Error", "Cannot access system brightness");
			throw new OperationFailException();
		}
	}
}
