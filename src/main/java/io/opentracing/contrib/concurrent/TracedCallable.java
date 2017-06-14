package io.opentracing.contrib.concurrent;

import io.opentracing.ActiveSpan;
import io.opentracing.ActiveSpan.Continuation;
import io.opentracing.NoopActiveSpanSource.NoopContinuation;
import java.util.concurrent.Callable;

/**
 * @author Pavol Loffay
 */
public class TracedCallable<V> implements Callable<V> {

  private final Callable<V> delegate;
  private final Continuation continuation;

  public TracedCallable(Callable<V> delegate, ActiveSpan activeSpan) {
    this.delegate = delegate;
    this.continuation = activeSpan != null ? activeSpan.capture() : NoopContinuation.INSTANCE;
  }

  @Override
  public V call() throws Exception {
    ActiveSpan activeSpan = continuation.activate();
    try {
      return delegate.call();
    } finally {
      activeSpan.close();
    }
  }
}
