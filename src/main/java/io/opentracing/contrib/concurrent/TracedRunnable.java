package io.opentracing.contrib.concurrent;

import io.opentracing.Scope;
import io.opentracing.Tracer;
import io.opentracing.Span;

/**
 * @author Pavol Loffay
 */
public class TracedRunnable implements Runnable {

  private final Runnable delegate;
  private final Span continuation;
  private final Tracer tracer;

  public TracedRunnable(Runnable delegate, Tracer tracer) {
    this.delegate = delegate;
    this.tracer = tracer;
    this.continuation = tracer.scopeManager().active() == null ? null : tracer.scopeManager().active().span();
  }

  @Override
  public void run() {
    Scope scope = continuation == null ? null : tracer.scopeManager().activate(continuation, false);
    try {
      delegate.run();
    } finally {
      if (scope != null) {
        scope.close();
      }
    }
  }
}
