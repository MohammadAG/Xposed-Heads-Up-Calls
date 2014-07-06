package com.mohammadag.headsupcallanswering;

import java.lang.reflect.Method;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

public class CallHandlingService extends IntentService {

	private static final String ANSWER_CALL_INTENT = "com.mohammadag.headsupcallanswering.action.ANSWER_CALL";
	private static final String END_CALL_INTENT = "com.mohammadag.headsupcallanswering.action.END_CALL";

	public CallHandlingService() {
		super("CallHandlingService");
	}

	public static PendingIntent getAnswerCallIntent(Context context) {
		Intent intent = new Intent(ANSWER_CALL_INTENT);
		intent.setClassName("com.mohammadag.headsupcallanswering",
				"com.mohammadag.headsupcallanswering.CallHandlingService");
		return PendingIntent.getService(context, 1, intent, 0);
	}

	public static PendingIntent getEndCallIntent(Context context) {
		Intent intent = new Intent(END_CALL_INTENT);
		intent.setClassName("com.mohammadag.headsupcallanswering",
				"com.mohammadag.headsupcallanswering.CallHandlingService");
		return PendingIntent.getService(context, 1, intent, 0);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (ANSWER_CALL_INTENT.equals(intent.getAction())) {
			answerCall();
		} else if (END_CALL_INTENT.equals(intent.getAction())) {
			endCall();
		}
	}

	private void answerCall() {
		try {
			TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			Class<?> clazz = Class.forName(telephonyManager.getClass().getName());
			Method method = clazz.getDeclaredMethod("getITelephony");
			method.setAccessible(true);
			ITelephony telephonyService = (ITelephony) method.invoke(telephonyManager);
			telephonyService.answerRingingCall();
		} catch (Exception e) {
			Log.d("HeadsUp", e.getMessage());
		}
	}

	private void endCall() {
		try {
			TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			Class<?> clazz = Class.forName(telephonyManager.getClass().getName());
			Method method = clazz.getDeclaredMethod("getITelephony");
			method.setAccessible(true);
			ITelephony telephonyService = (ITelephony) method.invoke(telephonyManager);
			telephonyService.endCall();
		} catch (Exception e) {
			Log.d("HeadsUp", e.getMessage());
		}
	}
}
