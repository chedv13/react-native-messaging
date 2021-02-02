package io.invertase.firebase.messaging;

import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;
import java.util.Iterator;
import java.util.MissingFormatArgumentException;

public class ReactNativeFirebaseNotificationParams {
  @NonNull
  private final Bundle data;

  public ReactNativeFirebaseNotificationParams(@NonNull Bundle var1) {
    if (var1 == null) {
      NullPointerException var3 = new NullPointerException("data");
      throw var3;
    } else {
      Bundle var2 = new Bundle(var1);
      this.data = var2;
    }
  }

  @Nullable
  Integer getNotificationCount() {
    Integer var3 = this.getInteger("gcm.n.notification_count");
    if (var3 == null) {
      return null;
    } else if (var3 < 0) {
      String var4 = String.valueOf(var3);
      String var1 = String.valueOf(var4);
      int var5 = var1.length();
      StringBuilder var2 = new StringBuilder(var5 + 67);
      var2.append("notificationCount is invalid: ");
      var2.append(var4);
      var2.append(". Skipping setting notificationCount.");
      Log.w("FirebaseMessaging", var2.toString());
      return null;
    } else {
      return var3;
    }
  }

  @Nullable
  Integer getNotificationPriority() {
    Integer var3 = this.getInteger("gcm.n.notification_priority");
    if (var3 == null) {
      return null;
    } else if (var3 >= -2 && var3 <= 2) {
      return var3;
    } else {
      String var4 = String.valueOf(var3);
      String var1 = String.valueOf(var4);
      int var5 = var1.length();
      StringBuilder var2 = new StringBuilder(var5 + 72);
      var2.append("notificationPriority is invalid ");
      var2.append(var4);
      var2.append(". Skipping setting notificationPriority.");
      Log.w("FirebaseMessaging", var2.toString());
      return null;
    }
  }

  Integer getVisibility() {
    Integer var3 = this.getInteger("gcm.n.visibility");
    if (var3 == null) {
      return null;
    } else if (var3 >= -1 && var3 <= 1) {
      return var3;
    } else {
      String var4 = String.valueOf(var3);
      String var1 = String.valueOf(var4);
      int var5 = var1.length();
      StringBuilder var2 = new StringBuilder(var5 + 53);
      var2.append("visibility is invalid: ");
      var2.append(var4);
      var2.append(". Skipping setting visibility.");
      Log.w("NotificationParams", var2.toString());
      return null;
    }
  }

  public String getString(String var1) {
    return this.data.getString(this.normalizePrefix(var1));
  }

  private String normalizePrefix(String var1) {
    if (!this.data.containsKey(var1) && var1.startsWith("gcm.n.")) {
      String var2 = keyWithOldPrefix(var1);
      if (this.data.containsKey(var2)) {
        return var2;
      }
    }

    return var1;
  }

  public boolean getBoolean(String var1) {
    String var2 = this.getString(var1);
    return "1".equals(var2) || Boolean.parseBoolean(var2);
  }

  public Integer getInteger(String var1) {
    String var6 = this.getString(var1);
    if (!TextUtils.isEmpty(var6)) {
      try {
        Integer var8 = Integer.parseInt(var6);
        return var8;
      } catch (NumberFormatException var5) {
        var1 = userFriendlyKey(var1);
        String var2 = String.valueOf(var1);
        int var7 = var2.length();
        String var3 = String.valueOf(var6);
        int var9 = var3.length();
        StringBuilder var4 = new StringBuilder(var7 + 38 + var9);
        var4.append("Couldn't parse value of ");
        var4.append(var1);
        var4.append("(");
        var4.append(var6);
        var4.append(") into an int");
        Log.w("NotificationParams", var4.toString());
      }
    }

    return null;
  }

  public Long getLong(String var1) {
    String var6 = this.getString(var1);
    if (!TextUtils.isEmpty(var6)) {
      try {
        Long var8 = Long.parseLong(var6);
        return var8;
      } catch (NumberFormatException var5) {
        var1 = userFriendlyKey(var1);
        String var2 = String.valueOf(var1);
        int var7 = var2.length();
        String var3 = String.valueOf(var6);
        int var9 = var3.length();
        StringBuilder var4 = new StringBuilder(var7 + 38 + var9);
        var4.append("Couldn't parse value of ");
        var4.append(var1);
        var4.append("(");
        var4.append(var6);
        var4.append(") into a long");
        Log.w("NotificationParams", var4.toString());
      }
    }

    return null;
  }

  @Nullable
  public String getLocalizationResourceForKey(String var1) {
    return this.getString(String.valueOf(var1).concat("_loc_key"));
  }

  @Nullable
  public Object[] getLocalizationArgsForKey(String var1) {
    JSONArray var4 = this.getJSONArray(String.valueOf(var1).concat("_loc_args"));
    if (var4 == null) {
      return null;
    } else {
      String[] var2 = new String[var4.length()];

      for (int var3 = 0; var3 < var2.length; ++var3) {
        var2[var3] = var4.optString(var3);
      }

      return var2;
    }
  }

  @Nullable
  public JSONArray getJSONArray(String var1) {
    String var6 = this.getString(var1);
    if (!TextUtils.isEmpty(var6)) {
      try {
        JSONArray var8 = new JSONArray(var6);
        return var8;
      } catch (JSONException var5) {
        var1 = userFriendlyKey(var1);
        String var2 = String.valueOf(var1);
        int var7 = var2.length();
        String var3 = String.valueOf(var6);
        int var9 = var3.length();
        StringBuilder var4 = new StringBuilder(var7 + 50 + var9);
        var4.append("Malformed JSON for key ");
        var4.append(var1);
        var4.append(": ");
        var4.append(var6);
        var4.append(", falling back to default");
        Log.w("NotificationParams", var4.toString());
      }
    }

    return null;
  }

  private static String userFriendlyKey(String var0) {
    return var0.startsWith("gcm.n.") ? var0.substring(6) : var0;
  }

  @Nullable
  public Uri getLink() {
    String var1 = this.getString("gcm.n.link_android");
    String var2;
    if (TextUtils.isEmpty(var1)) {
      var2 = this.getString("gcm.n.link");
    } else {
      var2 = var1;
    }

    return !TextUtils.isEmpty(var2) ? Uri.parse(var2) : null;
  }

  @Nullable
  public String getSoundResourceName() {
    String var1 = this.getString("gcm.n.sound2");
    return TextUtils.isEmpty(var1) ? this.getString("gcm.n.sound") : var1;
  }

  @Nullable
  public long[] getVibrateTimings() {
    JSONArray var1 = this.getJSONArray("gcm.n.vibrate_timings");
    if (var1 == null) {
      return null;
    } else {
      label45:
      {
        boolean var10001;
        int var9;
        try {
          var9 = var1.length();
        } catch (NumberFormatException var8) {
          var10001 = false;
          break label45;
        }

        if (var9 <= 1) {
          try {
            JSONException var10 = new JSONException("vibrateTimings have invalid length");
            throw var10;
          } catch (NumberFormatException | JSONException var4) {
            var10001 = false;
          }
        } else {
          label50:
          {
            long[] var2;
            try {
              var2 = new long[var1.length()];
            } catch (NumberFormatException var7) {
              var10001 = false;
              break label50;
            }

            var9 = 0;

            while (true) {
              int var3;
              try {
                var3 = var2.length;
              } catch (NumberFormatException var6) {
                var10001 = false;
                break;
              }

              if (var9 >= var3) {
                return var2;
              }

              try {
                var2[var9] = var1.optLong(var9);
              } catch (NumberFormatException var5) {
                var10001 = false;
                break;
              }

              ++var9;
            }
          }
        }
      }

      String var11 = String.valueOf(var1);
      String var12 = String.valueOf(var11);
      int var13 = var12.length();
      StringBuilder var14 = new StringBuilder(var13 + 74);
      var14.append("User defined vibrateTimings is invalid: ");
      var14.append(var11);
      var14.append(". Skipping setting vibrateTimings.");
      Log.w("NotificationParams", var14.toString());
      return null;
    }
  }

  @Nullable
  int[] getLightSettings() {
    JSONArray var11 = this.getJSONArray("gcm.n.light_settings");
    if (var11 == null) {
      return null;
    } else {
      int[] var1 = new int[3];

      int var2;
      String var12;
      String var15;
      IllegalArgumentException var10000;
      label49:
      {
        label36:
        {
          boolean var10001;
          try {
            var2 = var11.length();
          } catch (IllegalArgumentException var10) {
            var10000 = var10;
            var10001 = false;
            break label49;
          }

          if (var2 != 3) {
            try {
              JSONException var13 = new JSONException("lightSettings don't have all three fields");
              throw var13;
            } catch (JSONException var5) {
              var10001 = false;
            } catch (IllegalArgumentException var6) {
              var10000 = var6;
              var10001 = false;
              break label49;
            }
          } else {
            try {
              var1[0] = getLightColor(var11.optString(0));
              var1[1] = var11.optInt(1);
              var1[2] = var11.optInt(2);
              return var1;
            } catch (IllegalArgumentException var8) {
              var10000 = var8;
              var10001 = false;
              break label49;
            }
          }
        }

        var12 = String.valueOf(var11);
        var15 = String.valueOf(var12);
        int var17 = var15.length();
        StringBuilder var18 = new StringBuilder(var17 + 58);
        var18.append("LightSettings is invalid: ");
        var18.append(var12);
        var18.append(". Skipping setting LightSettings");
        Log.w("NotificationParams", var18.toString());
        return null;
      }

      IllegalArgumentException var14 = var10000;
      var12 = String.valueOf(var11);
      var15 = var14.getMessage();
      String var16 = String.valueOf(var12);
      var2 = var16.length();
      String var3 = String.valueOf(var15);
      int var19 = var3.length();
      StringBuilder var4 = new StringBuilder(var2 + 60 + var19);
      var4.append("LightSettings is invalid: ");
      var4.append(var12);
      var4.append(". ");
      var4.append(var15);
      var4.append(". Skipping setting LightSettings");
      Log.w("NotificationParams", var4.toString());
      return null;
    }
  }

  public Bundle paramsWithReservedKeysRemoved() {
    Bundle var1 = new Bundle(this.data);
    Iterator var3 = this.data.keySet().iterator();

    while (var3.hasNext()) {
      String var2 = (String) var3.next();
      if (isReservedKey(var2)) {
        var1.remove(var2);
      }
    }

    return var1;
  }

  public Bundle paramsForAnalyticsIntent() {
    Bundle var1 = new Bundle(this.data);
    Iterator var3 = this.data.keySet().iterator();

    while (var3.hasNext()) {
      String var2 = (String) var3.next();
      if (!isAnalyticsKey(var2)) {
        var1.remove(var2);
      }
    }

    return var1;
  }

  @Nullable
  public String getLocalizedString(Resources var1, String var2, String var3) {
    String var4 = this.getLocalizationResourceForKey(var3);
    if (TextUtils.isEmpty(var4)) {
      return null;
    } else {
      int var11 = var1.getIdentifier(var4, "string", var2);
      String var8;
      String var9;
      if (var11 == 0) {
        var8 = userFriendlyKey(String.valueOf(var3).concat("_loc_key"));
        var9 = String.valueOf(var8);
        int var10 = var9.length();
        var2 = String.valueOf(var3);
        var11 = var2.length();
        StringBuilder var14 = new StringBuilder(var10 + 49 + var11);
        var14.append(var8);
        var14.append(" resource not found: ");
        var14.append(var3);
        var14.append(" Default value will be used.");
        Log.w("NotificationParams", var14.toString());
        return null;
      } else {
        Object[] var7 = this.getLocalizationArgsForKey(var3);
        if (var7 == null) {
          return var1.getString(var11);
        } else {
          try {
            var9 = var1.getString(var11, var7);
            return var9;
          } catch (MissingFormatArgumentException var6) {
            var2 = userFriendlyKey(var3);
            var8 = Arrays.toString(var7);
            var3 = String.valueOf(var2);
            int var12 = var3.length();
            var4 = String.valueOf(var8);
            int var13 = var4.length();
            StringBuilder var5 = new StringBuilder(var12 + 58 + var13);
            var5.append("Missing format argument for ");
            var5.append(var2);
            var5.append(": ");
            var5.append(var8);
            var5.append(" Default value will be used.");
            Log.w("NotificationParams", var5.toString(), var6);
            return null;
          }
        }
      }
    }
  }

  public String getPossiblyLocalizedString(Resources var1, String var2, String var3) {
    String var4 = this.getString(var3);
    return !TextUtils.isEmpty(var4) ? var4 : this.getLocalizedString(var1, var2, var3);
  }

  public boolean hasImage() {
    return !TextUtils.isEmpty(this.getString("gcm.n.image"));
  }

  public String getNotificationChannelId() {
    return this.getString("gcm.n.android_channel_id");
  }

  private static boolean isAnalyticsKey(String var0) {
    return var0.startsWith("google.c.a.") || var0.equals("from");
  }

  private static boolean isReservedKey(String var0) {
    return var0.startsWith("google.c.") || var0.startsWith("gcm.n.") || var0.startsWith("gcm.notification.");
  }

  private static int getLightColor(String var0) {
    int var1 = Color.parseColor(var0);
    if (var1 == -16777216) {
      IllegalArgumentException var2 = new IllegalArgumentException("Transparent color is invalid");
      throw var2;
    } else {
      return var1;
    }
  }

  public boolean isNotification() {
    return this.getBoolean("gcm.n.e");
  }

  public static boolean isNotification(Bundle var0) {
    return "1".equals(var0.getString("gcm.n.e")) || "1".equals(var0.getString(keyWithOldPrefix("gcm.n.e")));
  }

  private static String keyWithOldPrefix(String var0) {
    return !var0.startsWith("gcm.n.") ? var0 : var0.replace("gcm.n.", "gcm.notification.");
  }
}
