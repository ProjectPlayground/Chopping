package com.chopping.utils;

import android.content.Context;
import android.support.annotation.StringRes;
import android.widget.Toast;

/**
 * Utils for tools used in common Apps.
 *
 * @author Xinyue Zhao
 */
public final class Utils {
	/**
	 * Show long time toast.
	 *
	 * @param context
	 * 		{@link android.content.Context}.
	 * @param messageId
	 * 		{@link android.support.annotation.StringRes}. Message to show.
	 */
	public static void showLongToast(Context context, @StringRes int messageId) {
		Toast.makeText(context, context.getString(messageId), Toast.LENGTH_LONG).show();
	}

	/**
	 * Show short time toast.
	 *
	 * @param context
	 * 		{@link android.content.Context}.
	 * @param messageId
	 * 		{@link android.support.annotation.StringRes}. Message to show.
	 */
	public static void showShortToast(Context context, @StringRes int messageId) {
		Toast.makeText(context, context.getString(messageId), Toast.LENGTH_SHORT).show();
	}

	/**
	 * Show short time toast.
	 *
	 * @param context
	 * 		{@link android.content.Context}.
	 * @param message
	 * 		{@link java.lang.String}. Message to show.
	 */
	public static void showLongToast(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}

	/**
	 * Show short time toast.
	 *
	 * @param context
	 * 		{@link android.content.Context}.
	 * @param message
	 * 		{@link java.lang.String}. Message to show.
	 */
	public static void showShortToast(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
}