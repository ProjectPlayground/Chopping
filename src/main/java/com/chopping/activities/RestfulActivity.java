package com.chopping.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.chopping.bus.RestApiResponseEvent;
import com.chopping.bus.UpdateNetworkStatusEvent;
import com.chopping.utils.RestUtils;

import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * A common base {@link android.app.Activity} that calls REST.
 *
 * @author Xinyue Zhao
 */
public abstract class RestfulActivity extends AppCompatActivity {

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link UpdateNetworkStatusEvent}.
	 *
	 * @param e
	 * 		Event {@link UpdateNetworkStatusEvent}.
	 */
	public void onEventMainThread( UpdateNetworkStatusEvent e ) {
		if( e.isConnected() ) {
			onNetworkConnected();
		}
	}

	/**
	 * Handler for {@link RestApiResponseEvent}.
	 *
	 * @param e
	 * 		Event {@link RestApiResponseEvent}.
	 */
	public void onEventMainThread( RestApiResponseEvent e ) {
		if(e.isSuccess()) {
			onRestApiSuccess();
		}
	}

	//------------------------------------------------


	private Realm                               mRealm;
	private RealmResults<? extends RealmObject> mRealmData;
	private RealmChangeListener mListListener = new RealmChangeListener() {
		@Override
		public void onChange() {
			buildViews();
		}
	};


	private void load() {
		sendPending();
		if( !RestUtils.shouldLoadLocal( getApplication()) ) {
			loadList();
		}
	}


	protected abstract void initDataBinding();


	protected void queryLocalData() {
		RealmQuery<? extends RealmObject> query = mRealm.where( getDataClazz() );
		buildQuery(query);
		mRealmData = query.findAllSortedAsync(
								   "reqTime",
								   Sort.DESCENDING
						   );
		mRealmData.removeChangeListeners();
		mRealmData.addChangeListener( mListListener );
		if( RestUtils.shouldLoadLocal( getApplication() ) ) {
			buildViews();
		}
	}

	protected void buildQuery(RealmQuery<? extends RealmObject> q) {
		//No default impl.
	}

	protected abstract Class<? extends RealmObject> getDataClazz();

	protected abstract void sendPending();

	protected abstract void loadList();

	protected abstract void buildViews();

	/**
	 * Callback when event of network status changed and connection is connected.
	 */
	protected void onNetworkConnected() {
		sendPending();
	}

	protected RealmResults<? extends RealmObject> getData() {
		return mRealmData;
	}



	protected boolean isDataLoaded() {
		return mRealmData.isLoaded();
	}

	protected void onRestApiSuccess(){

	}

	@Override
	protected void onResume() {
		super.onResume();
		EventBus.getDefault()
				.registerSticky( this );

	}

	@Override
	protected void onPause() {
		EventBus.getDefault()
				.unregister( this );
		super.onPause();
	}

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		mRealm = Realm.getDefaultInstance();
		initDataBinding();
		queryLocalData();
		load();
	}

	@Override
	protected void onDestroy() {
		if( mRealmData != null   ) {
			mRealmData.removeChangeListener( mListListener );
		}
		if( mRealm != null  ) {
			mRealm.close();
		}
		super.onDestroy();
	}
}
