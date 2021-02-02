package io.invertase.firebase.messaging;

import java.io.IOException;
import java.util.concurrent.Callable;

final class ReactNativeFirebaseImageDownloadLambda implements Callable {
  private final ReactNativeFirebaseImageDownload arg$1;

  ReactNativeFirebaseImageDownloadLambda(ReactNativeFirebaseImageDownload var1) {
    this.arg$1 = var1;
  }

  public Object call() throws IOException {
    return this.arg$1.blockingDownload();
  }
}
