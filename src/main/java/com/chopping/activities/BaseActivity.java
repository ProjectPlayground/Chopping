package com.chopping.activities;

import com.android.volley.VolleyError;
import com.chopping.R;
import com.chopping.application.BasicPrefs;
import com.chopping.application.ErrorHandler;
import com.chopping.bus.AirplaneModeOnEvent;
import com.chopping.bus.ApplicationConfigurationDownloadedEvent;
import com.chopping.bus.ReloadEvent;
import com.chopping.exceptions.CanNotOpenOrFindAppPropertiesException;
import com.chopping.exceptions.InvalidAppPropertiesException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import de.greenrobot.event.EventBus;

/**
 * General base activity, with error-handling, loading configuration etc.
 */
public abstract class BaseActivity extends ActionBarActivity {
	/**
	 * Basic layout that contains an error-handling(a sticky).
	 */
	private static final int LAYOUT_BASE = R.layout.activity_b;
	/**
	 * EXTRAS. Status of available of error-handling. Default is {@code true}
	 * <p/>
	 * See {@link #mErrorHandlerAvailable}.
	 */
	private static final String EXTRAS_ERR_AVA = "err.ava";
	/**
	 * A logical that contains controlling over all network-errors.
	 */
	private ErrorHandler mErrorHandler;
	/**
	 * {@code true} if {@link #mErrorHandler} works and shows associated {@link com.chopping.activities.ErrorHandlerActivity}.
	 */
	private boolean mErrorHandlerAvailable = true;

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link com.chopping.bus.ApplicationConfigurationDownloadedEvent}
	 *
	 * @param e
	 * 		Event {@link  com.chopping.bus.ApplicationConfigurationDownloadedEvent}.
	 */
	public void onEvent(ApplicationConfigurationDownloadedEvent e) {
		onAppConfigLoaded();
	}

	/**
	 * Handler for {@link com.android.volley.VolleyError}
	 *
	 * @param e
	 * 		Event {@link  com.android.volley.VolleyError}.
	 */
	public void onEvent(VolleyError e) {
		onNetworkError();
	}

	/**
	 * Handler for {@link com.chopping.bus.ReloadEvent}
	 *
	 * @param e
	 * 		Event {@link  com.chopping.bus.ReloadEvent}.
	 */
	public void onEvent(ReloadEvent e) {
		onReload();
		EventBus.getDefault().removeStickyEvent(e);
	}

	/**
	 * Handler for {@link com.chopping.bus.AirplaneModeOnEvent}
	 *
	 * @param e
	 * 		Event {@link  com.chopping.bus.AirplaneModeOnEvent}.
	 */
	public void onEvent(AirplaneModeOnEvent e) {
		if (mErrorHandlerAvailable) {
			mErrorHandler.openStickyBanner(this, true);
			mErrorHandler.setText(null, true);
			EventBus.getDefault().removeStickyEvent(AirplaneModeOnEvent.class);
		}
	}

	//------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mErrorHandlerAvailable = savedInstanceState.getBoolean(EXTRAS_ERR_AVA, true);
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		initErrorHandler();
	}

	@Override
	protected void onResume() {
		if (isStickyAvailable()) {
			EventBus.getDefault().registerSticky(this);
		} else {
			EventBus.getDefault().register(this);
		}
		EventBus.getDefault().register(mErrorHandler);
		super.onResume();

		String mightError = null;
		try {
			getPrefs().downloadApplicationConfiguration();
		} catch (InvalidAppPropertiesException _e) {
			mightError = _e.getMessage();
		} catch (CanNotOpenOrFindAppPropertiesException _e) {
			mightError = _e.getMessage();
		}
		if (mightError != null) {
			new AlertDialog.Builder(this).setTitle(R.string.app_name).setMessage(mightError).setCancelable(false)
					.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					}).create().show();
		}
	}

	@Override
	protected void onPause() {
		EventBus.getDefault().unregister(this);
		EventBus.getDefault().unregister(mErrorHandler);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mErrorHandler.onDestroy();
		mErrorHandler = null;
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(LAYOUT_BASE);
		ViewGroup content = (ViewGroup) findViewById(R.id.content_fl);
		content.addView(getLayoutInflater().inflate(layoutResID, null),
				new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));
	}

	/**
	 * Set {@code true} if {@link #mErrorHandler} works and shows associated {@link
	 * com.chopping.activities.ErrorHandlerActivity}.
	 *
	 * @throws NullPointerException
	 * 		must be thrown if it is called at lest after {@link android.app.Activity#onPostCreate(android.os.Bundle)}.
	 */
	protected void setErrorHandlerAvailable(boolean _isErrorHandlerAvailable) {
		if (mErrorHandler == null) {
			throw new NullPointerException(
					"BaseActivity#setErrorHandlerAvailable must be call at least after onPostCreate().");
		}
		mErrorHandlerAvailable = _isErrorHandlerAvailable;
		mErrorHandler.setErrorHandlerAvailable(mErrorHandlerAvailable);
	}

	/**
	 * Initialize {@link com.chopping.application.ErrorHandler}.
	 */
	private void initErrorHandler() {
		if (mErrorHandler == null) {
			mErrorHandler = new ErrorHandler();
		}
		mErrorHandler.onCreate(this, null);
		mErrorHandler.setErrorHandlerAvailable(mErrorHandlerAvailable);
	}

	/**
	 * App that use this Chopping should know the preference-storage.
	 *
	 * @return An instance of {@link com.chopping.application.BasicPrefs}.
	 */
	protected abstract BasicPrefs getPrefs();

	/**
	 * Is the {@link android.app.Activity}({@link android.support.v4.app.FragmentActivity}) ready to subscribe a
	 * sticky-event or not.
	 *
	 * @return {@code true} if the {@link android.app.Activity}({@link android.support.v4.app.FragmentActivity})
	 * available for sticky-events inc. normal events.
	 * <p/>
	 * <b>Default is {@code true}</b>.
	 */
	protected boolean isStickyAvailable() {
		return true;
	}

	/**
	 * Callback after App's config loaded.
	 */
	protected void onAppConfigLoaded() {

	}

	/**
	 * Callback when {@link com.android.volley.VolleyError} occurred.
	 */
	protected void onNetworkError() {

	}

	/**
	 * Callback when a reload({@link com.chopping.bus.ReloadEvent}) is required.
	 */
	protected void onReload() {

	}

	/**
	 * Call this method to decide whether a sticky on top which animates to bottom to show information about network
	 * error or an {@link com.chopping.activities.ErrorHandlerActivity}({@link com.chopping.fragments.ErrorHandlerFragment}).
	 * <p/>
	 * <p/>
	 * <b>Default shows an {@link com.chopping.activities.ErrorHandlerActivity}({@link
	 * com.chopping.fragments.ErrorHandlerFragment})</b>
	 *
	 * @param shownDataOnUI
	 * 		Set {@code true}, then a sticky will always show when network error happens.
	 */
	protected void setHasShownDataOnUI(boolean shownDataOnUI) {
		mErrorHandler.setHasDataOnUI(shownDataOnUI);
	}
}
