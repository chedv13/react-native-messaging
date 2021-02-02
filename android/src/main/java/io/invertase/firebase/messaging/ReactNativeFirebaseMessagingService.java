package io.invertase.firebase.messaging;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.MessagingAnalytics;
import com.google.firebase.messaging.NotificationParams;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.messaging.SendException;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ExecutorService;

import io.invertase.firebase.common.ReactNativeFirebaseEventEmitter;

public class ReactNativeFirebaseMessagingService extends FirebaseMessagingService {
  private static final Queue<String> overwrittenRecentlyReceivedMessageIds;

  @Override
  public void onSendError(String messageId, Exception sendError) {
    ReactNativeFirebaseEventEmitter emitter = ReactNativeFirebaseEventEmitter.getSharedInstance();
    emitter.sendEvent(ReactNativeFirebaseMessagingSerializer.messageSendErrorToEvent(messageId, sendError));
  }

  @Override
  public void onDeletedMessages() {
    ReactNativeFirebaseEventEmitter emitter = ReactNativeFirebaseEventEmitter.getSharedInstance();
    emitter.sendEvent(ReactNativeFirebaseMessagingSerializer.messagesDeletedToEvent());
  }

  @Override
  public void onMessageSent(String messageId) {
    ReactNativeFirebaseEventEmitter emitter = ReactNativeFirebaseEventEmitter.getSharedInstance();
    emitter.sendEvent(ReactNativeFirebaseMessagingSerializer.messageSentToEvent(messageId));
  }

  @Override
  public void onNewToken(String token) {
    ReactNativeFirebaseEventEmitter emitter = ReactNativeFirebaseEventEmitter.getSharedInstance();
    emitter.sendEvent(ReactNativeFirebaseMessagingSerializer.newTokenToTokenEvent(token));
  }

  @Override
  public void onMessageReceived(RemoteMessage remoteMessage) {

  }

  @Override
  public void handleIntent(Intent var1) {
    String var2 = var1.getAction();
    if (!"com.google.android.c2dm.intent.RECEIVE".equals(var2) && !"com.google.firebase.messaging.RECEIVE_DIRECT_BOOT".equals(var2)) {
      if ("com.google.firebase.messaging.NOTIFICATION_DISMISS".equals(var2)) {
        if (MessagingAnalytics.shouldUploadScionMetrics(var1)) {
          MessagingAnalytics.logNotificationDismiss(var1);
        }
      } else if ("com.google.firebase.messaging.NEW_TOKEN".equals(var2)) {
        this.onNewToken(var1.getStringExtra("token"));
      } else {
        String var3 = String.valueOf(var1.getAction());
        String var4 = "Unknown intent action: ";
        if (var3.length() != 0) {
          var3 = var4.concat(var3);
        } else {
          var3 = new String(var4);
        }

        Log.d("FirebaseMessaging", var3);
      }
    } else {
      this.handleMessageIntent(var1);
    }
  }

  private void handleMessageIntent(Intent var1) {
    if (!this.alreadyReceivedMessage(var1.getStringExtra("google.message_id"))) {
      this.passMessageIntentToSdk(var1);
    }
  }

  private void passMessageIntentToSdk(Intent var1) {
    String var2 = var1.getStringExtra("message_type");
    if (var2 == null) {
      var2 = "gcm";
    }

    byte var3;
    label37:
    {
      switch (var2.hashCode()) {
        case -2062414158:
          if (var2.equals("deleted_messages")) {
            var3 = 1;
            break label37;
          }
          break;
        case 102161:
          if (var2.equals("gcm")) {
            var3 = 0;
            break label37;
          }
          break;
        case 814694033:
          if (var2.equals("send_error")) {
            var3 = 3;
            break label37;
          }
          break;
        case 814800675:
          if (var2.equals("send_event")) {
            var3 = 2;
            break label37;
          }
      }

      var3 = -1;
    }

    switch (var3) {
      case 0:
        MessagingAnalytics.logNotificationReceived(var1);
        this.dispatchMessage(var1);
        return;
      case 1:
        this.onDeletedMessages();
        return;
      case 2:
        this.onMessageSent(var1.getStringExtra("google.message_id"));
        return;
      case 3:
        var2 = this.getMessageId(var1);
        ReactNativeFirebaseSendException var6 = new ReactNativeFirebaseSendException(var1.getStringExtra("error"));
        this.onSendError(var2, var6);
        return;
      default:
        String var4 = "Received message with unknown type: ";
        if (var2.length() != 0) {
          var4 = var4.concat(var2);
        } else {
          String var5 = new String(var4);
          var4 = var5;
        }

        Log.w("FirebaseMessaging", var4);
    }
  }

  private void dispatchMessage(Intent var1) {
    Bundle var2 = var1.getExtras();
    if (var2 == null) {
      var2 = new Bundle();
    }

    var2.remove("androidx.content.wakelockid");
    if (ReactNativeFirebaseNotificationParams.isNotification(var2)) {
      ReactNativeFirebaseNotificationParams var3 = new ReactNativeFirebaseNotificationParams(var2);
      ExecutorService var4 = FcmExecutors.newNetworkIOExecutor();
      ReactNativeFirebaseDisplayNotification var5 = new ReactNativeFirebaseDisplayNotification(this, var3, var4);
      boolean var7 = false;

      boolean var10;
      try {
        var7 = true;
        var10 = var5.handleNotification();
        var7 = false;
      } finally {
        if (var7) {
          var4.shutdown();
        }
      }

      if (var10) {
        var4.shutdown();
        return;
      }

      var4.shutdown();
      if (MessagingAnalytics.shouldUploadScionMetrics(var1)) {
        MessagingAnalytics.logNotificationForeground(var1);
      }
    }

    RemoteMessage var9 = new RemoteMessage(var2);
    this.onMessageReceived(var9);
  }

  private boolean alreadyReceivedMessage(String var1) {
    if (TextUtils.isEmpty(var1)) {
      return false;
    } else if (overwrittenRecentlyReceivedMessageIds.contains(var1)) {
      if (Log.isLoggable("FirebaseMessaging", 3)) {
        String var2 = String.valueOf(var1);
        var1 = "Received duplicate message: ";
        if (var2.length() != 0) {
          var2 = var1.concat(var2);
        } else {
          var2 = new String(var1);
        }

        Log.d("FirebaseMessaging", var2);
      }

      return true;
    } else {
      if (overwrittenRecentlyReceivedMessageIds.size() >= 10) {
        overwrittenRecentlyReceivedMessageIds.remove();
      }

      overwrittenRecentlyReceivedMessageIds.add(var1);
      return false;
    }
  }

  private String getMessageId(Intent var1) {
    String var2 = var1.getStringExtra("google.message_id");
    return var2 == null ? var1.getStringExtra("message_id") : var2;
  }

  static {
    ArrayDeque var0 = new ArrayDeque(10);
    overwrittenRecentlyReceivedMessageIds = var0;
  }
}
