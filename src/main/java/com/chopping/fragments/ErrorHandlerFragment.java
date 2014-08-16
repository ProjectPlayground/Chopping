package com.chopping.fragments;

import com.chopping.R;
import com.chopping.activities.ErrorHandlerActivity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.greenrobot.event.EventBus;

/**
 * {@link com.chopping.fragments.ErrorHandlerFragment} provides basic logical for an error happens when internet is not
 * available.
 * <p/>
 * <b>Relate class</b>
 * <p/>
 * {@link com.chopping.activities.ErrorHandlerActivity}
 */
public class ErrorHandlerFragment extends Fragment {
	private static int LAYOUT = R.layout.fragment_errorhandler;

	/**
	 * Extras. {@link java.lang.String} description of error.
	 * <p/>
	 * Equal to {@link com.chopping.activities.ErrorHandlerActivity#EXTRAS_ERR_MSG}.
	 */
	public static final String EXTRAS_ERR_MSG = ErrorHandlerActivity.EXTRAS_ERR_MSG;

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link }
	 *
	 * @param e
	 * 		Event {@link  }.
	 */
	public void onEvent(Object e) {

	}

	//------------------------------------------------

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		return inflater.inflate(LAYOUT, container, false);
	}


	@Override
	public void onResume() {
		EventBus.getDefault().register(this);
		super.onResume();
	}

	@Override
	public void onPause() {
		EventBus.getDefault().unregister(this);
		super.onPause();
	}

}
