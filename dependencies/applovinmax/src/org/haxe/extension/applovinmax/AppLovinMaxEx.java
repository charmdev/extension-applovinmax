package org.haxe.extension.applovinmax;

import org.json.JSONObject;
import android.util.Log;
import org.haxe.extension.Extension;
import org.haxe.lime.HaxeObject;
import android.opengl.GLSurfaceView;
import java.util.Arrays;


import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdRevenueListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.MaxRewardedAdListener;
import com.applovin.mediation.ads.MaxRewardedAd;

import com.applovin.sdk.AppLovinSdkConfiguration;
import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;




public class AppLovinMaxEx extends Extension
{
	protected AppLovinMaxEx() {}

	protected static HaxeObject _callback = null;
	protected static String TAG = "AppLovinMaxEx";

	protected static boolean giveReward = false;
	protected static boolean rewardSended = false;

	protected static int retryAttempt;
	protected static MaxRewardedAd rewardedAd;
	
	protected static String _appkey;


	public static void init(HaxeObject callback, String appkey, String[] testDeviceIds) {
		_callback = callback;
		_appkey = appkey;

		AppLovinSdk.getInstance(Extension.mainActivity).setMediationProvider("max");
		
		if (testDeviceIds != null)
			AppLovinSdk.getInstance(Extension.mainActivity).getSettings().setTestDeviceAdvertisingIds(Arrays.asList(testDeviceIds));

		AppLovinSdk.initializeSdk(Extension.mainActivity, new AppLovinSdk.SdkInitializationListener() {
			@Override
			public void onSdkInitialized(final AppLovinSdkConfiguration configuration)
			{
				AppLovinMaxEx.initMAX();
				
				//AppLovinSdk.getInstance(Extension.mainActivity).showMediationDebugger();
			}
		});
	}

	public static void initMAX() {

		Extension.mainActivity.runOnUiThread(new Runnable() {
			public void run() {
				
				rewardedAd = MaxRewardedAd.getInstance(AppLovinMaxEx._appkey, Extension.mainActivity );
				rewardedAd.setListener( new MaxRewardedAdListener() {

					@Override
					public void onAdLoaded(final MaxAd maxAd)
					{
						Log.d(TAG, "onAdLoaded");

						if (rewardedAd.isReady())
						{
							giveReward = false;
							rewardSended = false;

							if (Extension.mainView == null) return;
							GLSurfaceView view = (GLSurfaceView) Extension.mainView;
							view.queueEvent(new Runnable() {
								public void run() {
									_callback.call("onRewardedCanShow", new Object[] {});
							}});
						}
					}

					@Override
					public void onAdLoadFailed(final String adUnitId, final MaxError error)
					{
						Log.d(TAG, "onAdLoadFailed");

						giveReward = false;
						rewardSended = false;

						if (Extension.mainView == null) return;
						GLSurfaceView view = (GLSurfaceView) Extension.mainView;
						view.queueEvent(new Runnable() {
							public void run() {
								_callback.call("onVideoSkipped", new Object[] {});
						}});

						// Rewarded ad failed to load 
						// We recommend retrying with exponentially higher delays up to a maximum delay (in this case 64 seconds)

						/*retryAttempt++;
						long delayMillis = TimeUnit.SECONDS.toMillis( (long) Math.pow( 2, Math.min( 6, retryAttempt ) ) );

						new Handler().postDelayed( new Runnable()
						{
							@Override
							public void run()
							{
								AppLovinMaxEx.rewardedAd.loadAd();
							}
						}, delayMillis );*/

					}

					@Override
					public void onAdDisplayFailed(final MaxAd maxAd, final MaxError error)
					{
						Log.d(TAG, "onAdDisplayFailed");

						// Rewarded ad failed to display. We recommend loading the next ad
						AppLovinMaxEx.rewardedAd.loadAd();

						giveReward = false;
						rewardSended = false;

						if (Extension.mainView == null) return;
						GLSurfaceView view = (GLSurfaceView) Extension.mainView;
						view.queueEvent(new Runnable() {
							public void run() {
								_callback.call("onVideoSkipped", new Object[] {});
						}});
					}

					@Override
					public void onAdHidden(final MaxAd maxAd)
					{
						Log.d(TAG, "onAdHidden");

						if (giveReward && !rewardSended) {
							if (Extension.mainView == null) return;
							GLSurfaceView view = (GLSurfaceView) Extension.mainView;
							view.queueEvent(new Runnable() {
								public void run() {
									_callback.call("onRewardedCompleted", new Object[] {});
							}});

							Log.d(TAG, "onRewardedVideoAdClosed! giveReward");
						}
						else if (!giveReward && !rewardSended) {
							if (Extension.mainView == null) return;
							GLSurfaceView view = (GLSurfaceView) Extension.mainView;
							view.queueEvent(new Runnable() {
								public void run() {
									_callback.call("onVideoSkipped", new Object[] {});
							}});

							Log.d(TAG, "onRewardedVideoAdClosed! !giveReward");
						}

						giveReward = false;
						rewardSended = false;

						AppLovinMaxEx.rewardedAd.loadAd();
					}

					@Override
					public void onAdDisplayed(final MaxAd maxAd) {
						Log.d(TAG, "onAdDisplayed");
					}

					@Override
					public void onAdClicked(final MaxAd maxAd) {
						Log.d(TAG, "onAdClicked");
					}

					@Override
					public void onRewardedVideoStarted(final MaxAd maxAd) {
						Log.d(TAG, "onRewardedVideoStarted");
					}

					@Override
					public void onRewardedVideoCompleted(final MaxAd maxAd) {
						Log.d(TAG, "onRewardedVideoCompleted");
					}

					@Override
					public void onUserRewarded(final MaxAd maxAd, final MaxReward maxReward)
					{
						Log.d(TAG, "onUserRewarded");

						giveReward = true;
						rewardSended = false;
					}
				});
			
				AppLovinMaxEx.rewardedAd.loadAd();
			}
		});
	}
	
	public static void showRewarded() {

		Log.d(TAG, "showRewarded " + AppLovinMaxEx.rewardedAd.isReady());
		
		if (!AppLovinMaxEx.rewardedAd.isReady()) return;
		
		Extension.mainActivity.runOnUiThread(new Runnable() {
			public void run() {

				if (AppLovinMaxEx.rewardedAd.isReady()) {
					AppLovinMaxEx.rewardedAd.showAd();
				}
			}
		});
	}

}
