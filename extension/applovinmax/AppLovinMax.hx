package extension.applovinmax;

import nme.JNI;
import nme.Lib;

class AppLovinMax {

#if ios
	private static var __ISads_set_event_handle = Lib.load("applovinmaxex","ISads_set_event_handle", 1);
#elseif android
	private static var instance:AppLovinMax = new AppLovinMax();
#end

	private static var initialized:Bool = false;
	public static var __init:AppLovinMax->String->Void = JNI.createStaticMethod("org/haxe/extension/applovinmax/AppLovinMaxEx", "init", "(Lorg/haxe/lime/HaxeObject;Ljava/lang/String;)V");
	public static var __showRewarded:Void->Void = JNI.createStaticMethod("org/haxe/extension/applovinmax/AppLovinMaxEx", "showRewarded", "()V");
	private static var completeCB:Void->Void;
	private static var skipCB:Void->Void;
	private static var viewCB:Void->Void;
	private static var clickCB:Void->Void;
	private static var canshow:Bool = false;
	public static var onRewardedEvent:String->Void = null;
	public static var onStatsEvent:String->Void = null;



	public static function init(appkey:String) {
		if (initialized) return;
		initialized = true;
#if android
		try {
			__init(instance, appkey);
		} catch(e:Dynamic) {
			trace("AppLovinMax REWARDED Error: "+e);
		}
#elseif ios
		try{
			var __init:String->Void = cpp.Lib.load("AppLovinMaxEx","applovinmaxex_init",1);
			__showRewarded = cpp.Lib.load("AppLovinMaxEx","applovinmaxex_show_rewarded",0);
			__init(appkey);

			__ISads_set_event_handle(ISnotifyListeners);
		}catch(e:Dynamic){
			trace("IS REWARDED iOS INIT Exception: "+e);
		}
#end
	}

	public static function canShowAds():Bool {
		trace("applovinmax canShowAds", canshow);
		return canshow;
	}

	private static function clearCB() {
		completeCB = null;
		skipCB = null;
		viewCB = null;
		clickCB = null;
	}

	public static function showRewarded(cb, skip, displaying, click) {
		
		canshow = false;

		trace("try... AppLovinMax showRewarded");

		completeCB = cb;
		skipCB = skip;
		viewCB = displaying;
		clickCB = click;

		try {
			__showRewarded();
		} catch(e:Dynamic) {
			trace("AppLovinMax ShowRewarded Exception: " + e);
		}
	}

#if ios
	private static function ISnotifyListeners(inEvent:Dynamic)
	{
		var event:String = Std.string(Reflect.field(inEvent, "type"));

		if (event == "IS_rewardedcanshow")
		{
			canshow = true;
			trace("IS REWARDED CAN SHOW");
			return;
		}

		if (event == "IS_rewardedcompleted")
		{
			trace("IS REWARDED COMPLETED");
			dispatchEventIfPossibleIS("CLOSED");
			if (completeCB != null)
			{
				completeCB();
				clearCB();
			}
		}
		else if (event == "IS_rewardedskip")
		{
			trace("IS REWARDED VIDEO IS SKIPPED");
			dispatchEventIfPossibleIS("CLOSED");
			if (skipCB != null)
			{
				skipCB();
				clearCB();
			}
		}
		else if (event == "IS_rewarded_displaying")
		{
			trace("IS REWARDED VIDEO Displaying");
			dispatchEventIfPossibleIS("DISPLAY");
			if (viewCB != null) viewCB();
		}
		else if (event == "IS_rewarded_click")
		{
			trace("IS REWARDED VIDEO clicked");
			dispatchEventIfPossibleIS("CLICK");
			if (clickCB != null) clickCB();
		}
		
		//if (event.charAt(0) == "{")
		//	dispatchStatEvent(event);
		
	}
#elseif android

	private function new() {}

	public function onRewardedCanShow()
	{
		canshow = true;
		trace("AppLovinMax REWARDED CAN SHOW");
	}

	public function onRewardedImpressionData(data:String)
	{
		trace("AppLovinMax REWARDED ImpressionData");
		dispatchStatsEvent(data);
	}

	public function onRewardedDisplaying()
	{
		trace("AppLovinMax REWARDED Displaying");
		dispatchEventIfPossibleIS("DISPLAY");
		if (viewCB != null) viewCB();
	}

	public function onRewardedClick()
	{
		trace("AppLovinMax REWARDED click");
		dispatchEventIfPossibleIS("CLICK");
		if (clickCB != null) clickCB();
	}

	public function onRewardedCompleted()
	{
		trace("AppLovinMax REWARDED COMPLETED");
		dispatchEventIfPossibleIS("CLOSED");
		if (completeCB != null)
		{
			completeCB();
			clearCB();
		}
	}
	public function onVideoSkipped()
	{
		trace("AppLovinMax REWARDED VIDEO IS SKIPPED");
		dispatchEventIfPossibleIS("CLOSED");
		if (skipCB != null)
		{
			skipCB();
			clearCB();
		}
	}
	
#end

	private static function dispatchStatsEvent(data:String):Void
	{
		if (onStatsEvent != null) {
			onStatsEvent(data);
		}
		else {
			trace('no stats event handler');
		}
	}

	private static function dispatchStatEvent(e:String):Void
	{
		if (onStatsEvent != null) {
			onStatsEvent(e);
		}
		else {
			trace('no event handler');
		}
	}

	private static function dispatchEventIfPossibleIS(e:String):Void
	{
		if (onRewardedEvent != null) {
			onRewardedEvent(e);
		}
		else {
			trace('no event handler');
		}
	}
	
}
