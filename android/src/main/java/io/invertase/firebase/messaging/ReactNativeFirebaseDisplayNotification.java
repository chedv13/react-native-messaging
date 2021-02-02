package io.invertase.firebase.messaging;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat.BigPictureStyle;
import androidx.core.app.NotificationCompat.Builder;

import com.google.android.gms.common.util.PlatformVersion;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ReactNativeFirebaseDisplayNotification {
  private final Executor networkIoExecutor;
  private final Context context;
  private final ReactNativeFirebaseNotificationParams params;

  public ReactNativeFirebaseDisplayNotification(Context var1, ReactNativeFirebaseNotificationParams var2, Executor var3) {
    this.networkIoExecutor = var3;
    this.context = var1;
    this.params = var2;
  }

  private boolean isAppForeground() {
    if (((KeyguardManager) this.context.getSystemService("keyguard")).inKeyguardRestrictedInputMode()) {
      return false;
    } else {
      if (!PlatformVersion.isAtLeastLollipop()) {
        SystemClock.sleep(10L);
      }

      int var1 = Process.myPid();
      List var3 = ((ActivityManager) this.context.getSystemService("activity")).getRunningAppProcesses();
      if (var3 != null) {
        Iterator var4 = var3.iterator();

        while (var4.hasNext()) {
          RunningAppProcessInfo var2 = (RunningAppProcessInfo) var4.next();
          if (var2.pid == var1) {
            if (var2.importance == 100) {
              return true;
            }

            return false;
          }
        }
      }

      return false;
    }
  }

  boolean handleNotification() {
    if (this.params.getBoolean("gcm.n.noui")) {
      return true;
    } else if (this.isAppForeground()) {
      return false;
    } else {
      ReactNativeFirebaseImageDownload var1 = this.startImageDownloadInBackground();
      ReactNativeFirebaseCommonNotificationBuilder.DisplayNotificationInfo var2 = ReactNativeFirebaseCommonNotificationBuilder.createNotificationInfo(this.context, this.params);
      this.waitForAndApplyImageDownload(var2.notificationBuilder, var1);
      this.showNotification(var2);
      return true;
    }
  }

  @Nullable
  private ReactNativeFirebaseImageDownload startImageDownloadInBackground() {
    ReactNativeFirebaseImageDownload var1 = ReactNativeFirebaseImageDownload.create(this.params.getString("gcm.n.image"));
    if (var1 != null) {
      var1.start(this.networkIoExecutor);
    }

    return var1;
  }

  private void waitForAndApplyImageDownload(Builder var1, @Nullable ReactNativeFirebaseImageDownload var2) {
    if (var2 != null) {
      try {
        Task var8 = var2.getTask();
        TimeUnit var3 = TimeUnit.SECONDS;
        Bitmap var9 = (Bitmap) Tasks.await(var8, 5L, var3);
        var1.setLargeIcon(var9);
        BigPictureStyle var13 = new BigPictureStyle();
        var1.setStyle(var13.bigPicture(var9).bigLargeIcon((Bitmap) null));
      } catch (ExecutionException var4) {
        String var7 = String.valueOf(var4.getCause());
        String var10 = String.valueOf(var7);
        int var11 = var10.length();
        StringBuilder var12 = new StringBuilder(var11 + 26);
        var12.append("Failed to download image: ");
        var12.append(var7);
        Log.w("FirebaseMessaging", var12.toString());
      } catch (InterruptedException var5) {
        Log.w("FirebaseMessaging", "Interrupted while downloading image, showing notification without it");
        var2.close();
        Thread.currentThread().interrupt();
      } catch (TimeoutException var6) {
        Log.w("FirebaseMessaging", "Failed to download image in time, showing notification without it");
        var2.close();
      }
    }
  }

  private void showNotification(ReactNativeFirebaseCommonNotificationBuilder.DisplayNotificationInfo var1) {
    if (Log.isLoggable("FirebaseMessaging", 3)) {
      Log.d("FirebaseMessaging", "Showing notification");
    }

    ((NotificationManager) this.context.getSystemService("notification")).notify(var1.tag, var1.id, var1.notificationBuilder.build());
  }
}
