package io.opentracing.contrib.concurrent;

import io.opentracing.ActiveSpan;
import io.opentracing.ActiveSpan.Continuation;
import io.opentracing.NoopActiveSpanSource.NoopContinuation;

/**
 * @author Pavol Loffay
 */
public class TracedRunnable implements Runnable {

  private final Runnable delegate;
  private final Continuation continuation;

  public TracedRunnable(Runnable delegate, ActiveSpan activeSpan) {
    this.delegate = delegate;
    this.continuation = activeSpan != null ? activeSpan.capture() : NoopContinuation.INSTANCE;
  }

  @Override
  public void run() {
    ActiveSpan activeSpan = continuation.activate();
    try {
      delegate.run();
    } finally {
      activeSpan.close();
    }
  }
}
