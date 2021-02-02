package io.invertase.firebase.messaging;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.common.internal.Preconditions;
import com.google.android.gms.internal.firebase_messaging.zzg;
import com.google.android.gms.internal.firebase_messaging.zzh;
import com.google.android.gms.internal.firebase_messaging.zzo;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Executor;

public class ReactNativeFirebaseImageDownload implements Closeable {
  private final URL url;
  @Nullable
  private Task<Bitmap> task;
  @Nullable
  private volatile InputStream connectionInputStream;

  @Nullable
  public static ReactNativeFirebaseImageDownload create(String var0) {
    if (TextUtils.isEmpty(var0)) {
      return null;
    } else {
      try {
        URL var2 = new URL(var0);
        ReactNativeFirebaseImageDownload var4 = new ReactNativeFirebaseImageDownload(var2);
        return var4;
      } catch (MalformedURLException var3) {
        var0 = String.valueOf(var0);
        String var1 = "Not downloading image, bad URL: ";
        if (var0.length() != 0) {
          var0 = var1.concat(var0);
        } else {
          var0 = new String(var1);
        }

        Log.w("FirebaseMessaging", var0);
        return null;
      }
    }
  }

  private ReactNativeFirebaseImageDownload(URL var1) {
    this.url = var1;
  }

  public void start(Executor var1) {
    ReactNativeFirebaseImageDownloadLambda var2 = new ReactNativeFirebaseImageDownloadLambda(this);
    this.task = Tasks.call(var1, var2);
  }

  public Task<Bitmap> getTask() {
    return (Task) Preconditions.checkNotNull(this.task);
  }

  public Bitmap blockingDownload() throws IOException {
    String var1 = String.valueOf(this.url);
    String var2 = String.valueOf(var1);
    int var8 = var2.length();
    StringBuilder var3 = new StringBuilder(var8 + 22);
    var3.append("Starting download of: ");
    var3.append(var1);
    Log.i("FirebaseMessaging", var3.toString());
    byte[] var5 = this.blockingDownloadBytes();
    Bitmap var6 = BitmapFactory.decodeByteArray(var5, 0, var5.length);
    String var4;
    if (var6 == null) {
      var4 = String.valueOf(this.url);
      var2 = String.valueOf(var4);
      var8 = var2.length();
      var3 = new StringBuilder(var8 + 24);
      var3.append("Failed to decode image: ");
      var3.append(var4);
      IOException var7 = new IOException(var3.toString());
      throw var7;
    } else {
      if (Log.isLoggable("FirebaseMessaging", 3)) {
        var4 = String.valueOf(this.url);
        var2 = String.valueOf(var4);
        var8 = var2.length();
        var3 = new StringBuilder(var8 + 31);
        var3.append("Successfully downloaded image: ");
        var3.append(var4);
        Log.d("FirebaseMessaging", var3.toString());
      }

      return var6;
    }
  }

  private byte[] blockingDownloadBytes() throws IOException {
    URLConnection var1 = this.url.openConnection();
    IOException var11;
    if (var1.getContentLength() > 1048576) {
      var11 = new IOException("Content-Length exceeds max size of 1048576");
      throw var11;
    } else {
      InputStream var12 = var1.getInputStream();

      byte[] var2;
      try {
        this.connectionInputStream = var12;
        var2 = zzg.zza(zzg.zzb(var12, 1048577L));
      } catch (Throwable var9) {
        if (var12 != null) {
          try {
            var12.close();
          } catch (Throwable var8) {
            zzo.zza(var9, var8);
            throw var9;
          }
        }

        throw var9;
      }

      if (var12 != null) {
        var12.close();
      }

      if (Log.isLoggable("FirebaseMessaging", 2)) {
        String var10 = String.valueOf(this.url);
        String var13 = String.valueOf(var10);
        int var14 = var13.length();
        StringBuilder var3 = new StringBuilder(var14 + 34);
        var3.append("Downloaded ");
        var3.append(var2.length);
        var3.append(" bytes from ");
        var3.append(var10);
        Log.v("FirebaseMessaging", var3.toString());
      }

      if (var2.length > 1048576) {
        var11 = new IOException("Image exceeds max size of 1048576");
        throw var11;
      } else {
        return var2;
      }
    }
  }

  public void close() {
    try {
      InputStream var2 = this.connectionInputStream;
      zzh.zza(var2);
    } catch (NullPointerException var1) {
      Log.e("FirebaseMessaging", "Failed to close the image download stream.", var1);
    }
  }
}
