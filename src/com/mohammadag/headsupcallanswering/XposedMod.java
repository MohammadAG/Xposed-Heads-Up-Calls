package com.mohammadag.headsupcallanswering;

import java.util.ArrayList;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.os.PowerManager;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XposedMod implements IXposedHookZygoteInit, IXposedHookLoadPackage {
	private void log(String text) {
		XposedBridge.log("HeadsUpCalls: " + text);
	}

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		PermissionGranter.initZygote();

		XposedHelpers.findAndHookMethod(Notification.Builder.class, "build", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				Notification.Builder builder = (Notification.Builder) param.thisObject;
				Context context = (Context) XposedHelpers.getObjectField(builder, "mContext");
				if (!"com.android.phone".equals(context.getPackageName())
						&& !"com.android.dialer".equals(context.getPackageName())
						&& !"com.google.android.dialer".equals(context.getPackageName()))
					return;

				KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
				if (keyguardManager.isKeyguardLocked())
					return;

				PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
				if (!powerManager.isScreenOn())
					return;

				boolean mUseChronometer = XposedHelpers.getBooleanField(builder, "mUseChronometer");
				if (mUseChronometer)
					return;

				ArrayList<?> mActions = (ArrayList<?>) XposedHelpers.getObjectField(builder, "mActions");
				if (mActions != null && mActions.size() > 0)
					return;

				int flags = XposedHelpers.getIntField(builder, "mFlags");
				if (!isBitwiseOred(flags, Notification.FLAG_ONGOING_EVENT))
					return;

				PendingIntent mFullScreenIntent = (PendingIntent) XposedHelpers.getObjectField(param.thisObject, "mFullScreenIntent");
				builder.addAction(0, "End", CallHandlingService.getEndCallIntent(context));
				builder.addAction(0, "Answer", CallHandlingService.getAnswerCallIntent(context));
				builder.setContentIntent(mFullScreenIntent);
				builder.setFullScreenIntent(null, true);
			}
		});
	}

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if ("com.android.dialer".equals(lpparam.packageName)
				|| "com.google.android.dialer".equals(lpparam.packageName)) {
			// AOSP
			try {
				XposedHelpers.findAndHookMethod("com.android.incallui.CallCommandClient",
						lpparam.classLoader, "setSystemBarNavigationEnabled", boolean.class,
						XC_MethodReplacement.DO_NOTHING);
			} catch (Throwable t) {

			}
		} else if ("com.android.phone".equals(lpparam.packageName)) {
			// Xperia devices
			try {
				XposedHelpers.findAndHookMethod("com.android.phone.NotificationMgr$StatusBarHelper",
						lpparam.classLoader, "updateStatusBar", boolean.class, int.class, XC_MethodReplacement.DO_NOTHING);
			} catch (Throwable t) {

			}
		}
	}

	private static final boolean isBitwiseOred(int toCheck, int flagToCheck) {
		return ((toCheck & flagToCheck) == flagToCheck);
	}
}
