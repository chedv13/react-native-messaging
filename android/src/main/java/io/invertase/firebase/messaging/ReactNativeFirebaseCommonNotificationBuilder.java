package io.invertase.firebase.messaging;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.util.concurrent.atomic.AtomicInteger;

public final class ReactNativeFirebaseCommonNotificationBuilder {
  public static final String METADATA_DEFAULT_COLOR = "com.google.firebase.messaging.default_notification_color";
  public static final String METADATA_DEFAULT_ICON = "com.google.firebase.messaging.default_notification_icon";
  public static final String METADATA_DEFAULT_CHANNEL_ID = "com.google.firebase.messaging.default_notification_channel_id";
  public static final String FCM_FALLBACK_NOTIFICATION_CHANNEL = "fcm_fallback_notification_channel";
  public static final String FCM_FALLBACK_NOTIFICATION_CHANNEL_LABEL = "fcm_fallback_notification_channel_label";
  private static final AtomicInteger requestCodeProvider;

  private ReactNativeFirebaseCommonNotificationBuilder() {
  }

  static ReactNativeFirebaseCommonNotificationBuilder.DisplayNotificationInfo createNotificationInfo(Context var0, ReactNativeFirebaseNotificationParams var1) {
    Bundle var2 = getManifestMetadata(var0.getPackageManager(), var0.getPackageName());
    return createNotificationInfo(var0, var0.getPackageName(), var1, getOrCreateChannel(var0, var1.getNotificationChannelId(), var2), var0.getResources(), var0.getPackageManager(), var2);
  }

  public static ReactNativeFirebaseCommonNotificationBuilder.DisplayNotificationInfo createNotificationInfo(Context var0, String var1, ReactNativeFirebaseNotificationParams var2, String var3, Resources var4, PackageManager var5, Bundle var6) {
    NotificationCompat.Builder var7 = new NotificationCompat.Builder(var0, var3);
    var3 = var2.getPossiblyLocalizedString(var4, var1, "gcm.n.title");
    if (!TextUtils.isEmpty(var3)) {
      var7.setContentTitle("my title");
    }

    var3 = var2.getPossiblyLocalizedString(var4, var1, "gcm.n.body");
    if (!TextUtils.isEmpty(var3)) {
      var7.setContentText(var3);
      NotificationCompat.BigTextStyle var8 = new NotificationCompat.BigTextStyle();
      var7.setStyle(var8.bigText(var3));
    }

    var7.setSmallIcon(getSmallIcon(var5, var4, var1, var2.getString("gcm.n.icon"), var6));
    Uri var16 = getSound(var1, var2, var4);
    if (var16 != null) {
      var7.setSound(var16);
    }

    var7.setContentIntent(createContentIntent(var0, var2, var1, var5));
    PendingIntent var11 = createDeleteIntent(var0, var2);
    if (var11 != null) {
      var7.setDeleteIntent(var11);
    }

    Integer var9 = getColor(var0, var2.getString("gcm.n.color"), var6);
    if (var9 != null) {
      var7.setColor(var9);
    }

    var7.setAutoCancel(var2.getBoolean("gcm.n.sticky") ^ true);
    var7.setLocalOnly(var2.getBoolean("gcm.n.local_only"));
    String var10 = var2.getString("gcm.n.ticker");
    if (var10 != null) {
      var7.setTicker(var10);
    }

    var9 = var2.getNotificationPriority();
    if (var9 != null) {
      var7.setPriority(var9);
    }

    var9 = var2.getVisibility();
    if (var9 != null) {
      var7.setVisibility(var9);
    }

    var9 = var2.getNotificationCount();
    if (var9 != null) {
      var7.setNumber(var9);
    }

    Long var12 = var2.getLong("gcm.n.event_time");
    if (var12 != null) {
      var7.setShowWhen(true);
      var7.setWhen(var12);
    }

    long[] var13 = var2.getVibrateTimings();
    if (var13 != null) {
      var7.setVibrate(var13);
    }

    int[] var14 = var2.getLightSettings();
    if (var14 != null) {
      var7.setLights(var14[0], var14[1], var14[2]);
    }

    var7.setDefaults(getConsolidatedDefaults(var2));
    ReactNativeFirebaseCommonNotificationBuilder.DisplayNotificationInfo var15 = new ReactNativeFirebaseCommonNotificationBuilder.DisplayNotificationInfo(var7, getTag(var2), 0);
    return var15;
  }

  private static int getConsolidatedDefaults(ReactNativeFirebaseNotificationParams var0) {
    int var1 = var0.getBoolean("gcm.n.default_sound") ? 1 : 0;
    if (var0.getBoolean("gcm.n.default_vibrate_timings")) {
      var1 |= 2;
    }

    return var0.getBoolean("gcm.n.default_light_settings") ? var1 | 4 : var1;
  }

  @TargetApi(26)
  private static boolean isValidIcon(Resources var0, int var1) {
    if (Build.VERSION.SDK_INT != 26) {
      return true;
    } else {
      StringBuilder var5;
      label29:
      {
        boolean var10001;
        boolean var4;
        try {
          var4 = var0.getDrawable(var1, (Resources.Theme) null) instanceof AdaptiveIconDrawable;
        } catch (Resources.NotFoundException var3) {
          var10001 = false;
          break label29;
        }

        if (!var4) {
          return true;
        }

        try {
          var5 = new StringBuilder(77);
          var5.append("Adaptive icons cannot be used in notifications. Ignoring icon id: ");
          var5.append(var1);
          Log.e("FirebaseMessaging", var5.toString());
          return false;
        } catch (Resources.NotFoundException var2) {
          var10001 = false;
        }
      }

      var5 = new StringBuilder(66);
      var5.append("Couldn't find resource ");
      var5.append(var1);
      var5.append(", treating it as an invalid icon");
      Log.e("FirebaseMessaging", var5.toString());
      return false;
    }
  }

  private static int getSmallIcon(PackageManager var0, Resources var1, String var2, String var3, Bundle var4) {
    if (!TextUtils.isEmpty(var3)) {
      int var5 = var1.getIdentifier(var3, "drawable", var2);
      if (var5 != 0 && isValidIcon(var1, var5)) {
        return var5;
      }

      var5 = var1.getIdentifier(var3, "mipmap", var2);
      if (var5 != 0 && isValidIcon(var1, var5)) {
        return var5;
      }

      String var13 = String.valueOf(var3);
      var5 = var13.length();
      StringBuilder var6 = new StringBuilder(var5 + 61);
      var6.append("Icon resource ");
      var6.append(var3);
      var6.append(" not found. Notification will use default icon.");
      Log.w("FirebaseMessaging", var6.toString());
    }

    int var11 = var4.getInt("com.google.firebase.messaging.default_notification_icon", 0);
    int var9;
    if (var11 != 0 && isValidIcon(var1, var11)) {
      var9 = var11;
    } else {
      try {
        var9 = var0.getApplicationInfo(var2, 0).icon;
      } catch (PackageManager.NameNotFoundException var7) {
        String var8 = String.valueOf(var7);
        var2 = String.valueOf(var8);
        int var10 = var2.length();
        StringBuilder var12 = new StringBuilder(var10 + 35);
        var12.append("Couldn't get own application info: ");
        var12.append(var8);
        Log.w("FirebaseMessaging", var12.toString());
        var9 = var11;
      }
    }

    if (var9 != 0) {
      if (!isValidIcon(var1, var9)) {
        return 17301651;
      }
    } else {
      var9 = 17301651;
    }

    return var9;
  }

  private static Integer getColor(Context var0, String var1, Bundle var2) {
    if (Build.VERSION.SDK_INT < 21) {
      return null;
    } else {
      if (!TextUtils.isEmpty(var1)) {
        try {
          Integer var10 = Color.parseColor(var1);
          return var10;
        } catch (IllegalArgumentException var6) {
          String var3 = String.valueOf(var1);
          int var9 = var3.length();
          StringBuilder var4 = new StringBuilder(var9 + 56);
          var4.append("Color is invalid: ");
          var4.append(var1);
          var4.append(". Notification will use default color.");
          Log.w("FirebaseMessaging", var4.toString());
        }
      }

      int var8 = var2.getInt("com.google.firebase.messaging.default_notification_color", 0);
      if (var8 != 0) {
        try {
          Integer var7 = ContextCompat.getColor(var0, var8);
          return var7;
        } catch (Resources.NotFoundException var5) {
          Log.w("FirebaseMessaging", "Cannot find the color resource referenced in AndroidManifest.");
        }
      }

      return null;
    }
  }

  private static Uri getSound(String var0, ReactNativeFirebaseNotificationParams var1, Resources var2) {
    String var5 = var1.getSoundResourceName();
    if (TextUtils.isEmpty(var5)) {
      return null;
    } else if (!"default".equals(var5) && var2.getIdentifier(var5, "raw", var0) != 0) {
      String var6 = String.valueOf(var0);
      int var7 = var6.length();
      String var3 = String.valueOf(var5);
      int var8 = var3.length();
      StringBuilder var4 = new StringBuilder(var7 + 24 + var8);
      var4.append("android.resource://");
      var4.append(var0);
      var4.append("/raw/");
      var4.append(var5);
      return Uri.parse(var4.toString());
    } else {
      return RingtoneManager.getDefaultUri(2);
    }
  }

  @Nullable
  private static PendingIntent createContentIntent(Context var0, ReactNativeFirebaseNotificationParams var1, String var2, PackageManager var3) {
    Intent var4 = createTargetIntent(var2, var1, var3);
    if (var4 == null) {
      return null;
    } else {
      var4.addFlags(67108864);
      var4.putExtras(var1.paramsWithReservedKeysRemoved());
      PendingIntent var5 = PendingIntent.getActivity(var0, generatePendingIntentRequestCode(), var4, 1073741824);
      return shouldUploadMetrics(var1) ? wrapContentIntent(var0, var1, var5) : var5;
    }
  }

  private static Intent createTargetIntent(String var0, ReactNativeFirebaseNotificationParams var1, PackageManager var2) {
    String var3 = var1.getString("gcm.n.click_action");
    if (!TextUtils.isEmpty(var3)) {
      Intent var6 = new Intent(var3);
      var6.setPackage(var0);
      var6.setFlags(268435456);
      return var6;
    } else {
      Uri var5 = var1.getLink();
      if (var5 != null) {
        Intent var7 = new Intent("android.intent.action.VIEW");
        var7.setPackage(var0);
        var7.setData(var5);
        return var7;
      } else {
        Intent var4 = var2.getLaunchIntentForPackage(var0);
        if (var4 == null) {
          Log.w("FirebaseMessaging", "No activity found to launch app");
        }

        return var4;
      }
    }
  }

  private static Bundle getManifestMetadata(PackageManager var0, String var1) {
    PackageManager.NameNotFoundException var10000;
    label34:
    {
      ApplicationInfo var6;
      boolean var10001;
      try {
        var6 = var0.getApplicationInfo(var1, 128);
      } catch (PackageManager.NameNotFoundException var5) {
        var10000 = var5;
        var10001 = false;
        break label34;
      }

      if (var6 == null) {
        return Bundle.EMPTY;
      }

      Bundle var9;
      var9 = var6.metaData;

      if (var9 == null) {
        return Bundle.EMPTY;
      }

      Bundle var10 = var6.metaData;
      return var10;
    }

    PackageManager.NameNotFoundException var7 = var10000;
    String var8 = String.valueOf(var7);
    var1 = String.valueOf(var8);
    int var11 = var1.length();
    StringBuilder var2 = new StringBuilder(var11 + 35);
    var2.append("Couldn't get own application info: ");
    var2.append(var8);
    Log.w("FirebaseMessaging", var2.toString());
    return Bundle.EMPTY;
  }

  @TargetApi(26)
  @VisibleForTesting
  public static String getOrCreateChannel(Context var0, String var1, Bundle var2) {
    if (Build.VERSION.SDK_INT < 26) {
      return null;
    } else {
      int var3;
      try {
        var3 = var0.getPackageManager().getApplicationInfo(var0.getPackageName(), 0).targetSdkVersion;
      } catch (PackageManager.NameNotFoundException var6) {
        return null;
      }

      if (var3 >= 26) {
        NotificationManager var10 = (NotificationManager) var0.getSystemService(NotificationManager.class);
        if (!TextUtils.isEmpty(var1)) {
          if (var10.getNotificationChannel(var1) != null) {
            return var1;
          }

          String var4 = String.valueOf(var1);
          int var11 = var4.length();
          StringBuilder var5 = new StringBuilder(var11 + 122);
          var5.append("Notification Channel requested (");
          var5.append(var1);
          var5.append(") has not been created by the app. Manifest configuration, or default, value will be used.");
          Log.w("FirebaseMessaging", var5.toString());
        }

        var1 = var2.getString("com.google.firebase.messaging.default_notification_channel_id");
        if (!TextUtils.isEmpty(var1)) {
          if (var10.getNotificationChannel(var1) != null) {
            return var1;
          }

          Log.w("FirebaseMessaging", "Notification Channel set in AndroidManifest.xml has not been created by the app. Default value will be used.");
        } else {
          Log.w("FirebaseMessaging", "Missing Default Notification Channel metadata in AndroidManifest. Default value will be used.");
        }

        if (var10.getNotificationChannel("fcm_fallback_notification_channel") == null) {
          int var8 = var0.getResources().getIdentifier("fcm_fallback_notification_channel_label", "string", var0.getPackageName());
          String var7;
          if (var8 == 0) {
            Log.e("FirebaseMessaging", "String resource \"fcm_fallback_notification_channel_label\" is not found. Using default string channel name.");
            var7 = "Misc";
          } else {
            var7 = var0.getString(var8);
          }

          NotificationChannel var9 = new NotificationChannel("fcm_fallback_notification_channel", var7, 3);
          var10.createNotificationChannel(var9);
        }

        return "fcm_fallback_notification_channel";
      } else {
        return null;
      }
    }
  }

  private static int generatePendingIntentRequestCode() {
    return requestCodeProvider.incrementAndGet();
  }

  private static PendingIntent wrapContentIntent(Context var0, ReactNativeFirebaseNotificationParams var1, PendingIntent var2) {
    Intent var3 = new Intent("com.google.firebase.messaging.NOTIFICATION_OPEN");
    return createMessagingPendingIntent(var0, var3.putExtras(var1.paramsForAnalyticsIntent()).putExtra("pending_intent", var2));
  }

  @Nullable
  private static PendingIntent createDeleteIntent(Context var0, ReactNativeFirebaseNotificationParams var1) {
    if (!shouldUploadMetrics(var1)) {
      return null;
    } else {
      Intent var2 = new Intent("com.google.firebase.messaging.NOTIFICATION_DISMISS");
      return createMessagingPendingIntent(var0, var2.putExtras(var1.paramsForAnalyticsIntent()));
    }
  }

  private static PendingIntent createMessagingPendingIntent(Context var0, Intent var1) {
    int var2 = generatePendingIntentRequestCode();
    Intent var3 = new Intent("com.google.firebase.MESSAGING_EVENT");
    ComponentName var4 = new ComponentName(var0, "com.google.firebase.iid.FirebaseInstanceIdReceiver");
    return PendingIntent.getBroadcast(var0, var2, var3.setComponent(var4).putExtra("wrapped_intent", var1), 1073741824);
  }

  static boolean shouldUploadMetrics(@NonNull ReactNativeFirebaseNotificationParams var0) {
    return var0.getBoolean("google.c.a.e");
  }

  private static String getTag(ReactNativeFirebaseNotificationParams var0) {
    String var3 = var0.getString("gcm.n.tag");
    if (!TextUtils.isEmpty(var3)) {
      return var3;
    } else {
      long var1 = SystemClock.uptimeMillis();
      StringBuilder var4 = new StringBuilder(37);
      var4.append("FCM-Notification:");
      var4.append(var1);
      return var4.toString();
    }
  }

  static {
    AtomicInteger var0 = new AtomicInteger((int) SystemClock.elapsedRealtime());
    requestCodeProvider = var0;
  }

  public static class DisplayNotificationInfo {
    public final NotificationCompat.Builder notificationBuilder;
    public final String tag;
    public final int id;

    DisplayNotificationInfo(NotificationCompat.Builder var1, String var2, int var3) {
      this.notificationBuilder = var1;
      this.tag = var2;
      this.id = 0;
    }
  }
}

