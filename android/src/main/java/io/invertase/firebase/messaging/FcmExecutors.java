//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.invertase.firebase.messaging;

import com.google.android.gms.common.util.concurrent.NamedThreadFactory;
import com.google.android.gms.internal.firebase_messaging.zzd;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FcmExecutors {
  static Executor newTopicsSyncTriggerExecutor() {
    return newCachedSingleThreadExecutor("Firebase-Messaging-Trigger-Topics-Io");
  }

  private static Executor newCachedSingleThreadExecutor(String var0) {
    TimeUnit var1 = TimeUnit.SECONDS;
    LinkedBlockingQueue var2 = new LinkedBlockingQueue();
    NamedThreadFactory var3 = new NamedThreadFactory("Firebase-Messaging-Trigger-Topics-Io");
    ThreadPoolExecutor var4 = new ThreadPoolExecutor(0, 1, 30L, var1, var2, var3);
    return var4;
  }

  static ScheduledExecutorService newTopicsSyncExecutor() {
    NamedThreadFactory var1 = new NamedThreadFactory("Firebase-Messaging-Topics-Io");
    ScheduledThreadPoolExecutor var0 = new ScheduledThreadPoolExecutor(1, var1);
    return var0;
  }

  static ExecutorService newNetworkIOExecutor() {
    NamedThreadFactory var0 = new NamedThreadFactory("Firebase-Messaging-Network-Io");
    return Executors.newSingleThreadExecutor(var0);
  }

  static ExecutorService newIntentHandleExecutor() {
    zzd.zza();
    NamedThreadFactory var0 = new NamedThreadFactory("Firebase-Messaging-Intent-Handle");
    TimeUnit var2 = TimeUnit.SECONDS;
    LinkedBlockingQueue var3 = new LinkedBlockingQueue();
    ThreadPoolExecutor var1 = new ThreadPoolExecutor(1, 1, 60L, var2, var3, var0);
    var1.allowCoreThreadTimeOut(true);
    return Executors.unconfigurableExecutorService(var1);
  }

  static ScheduledExecutorService newInitExecutor() {
    NamedThreadFactory var1 = new NamedThreadFactory("Firebase-Messaging-Init");
    ScheduledThreadPoolExecutor var0 = new ScheduledThreadPoolExecutor(1, var1);
    return var0;
  }
}

