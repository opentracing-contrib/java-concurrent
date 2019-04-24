package io.opentracing.contrib.concurrent;

import io.opentracing.Scope;
import io.opentracing.Tracer;
import io.opentracing.Span;

/**
 * @author Pavol Loffay
 */
public class TracedRunnable implements Runnable {

  private final Runnable delegate;
  private final Span span;
  private final Tracer tracer;

  public TracedRunnable(Runnable delegate, Tracer tracer) {
    this(delegate, tracer, tracer.activeSpan());
  }

  public TracedRunnable(Runnable delegate, Tracer tracer, Span span) {
    this.delegate = delegate;
    this.tracer = tracer;
    this.span = span;
  }

  @Override
  public void run() {
    Scope scope = span == null ? null : tracer.scopeManager().activate(span);
    try {
      delegate.run();
    } finally {
      if (scope != null) {
        scope.close();
      }
    }
  }
}
