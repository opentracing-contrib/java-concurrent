package io.opentracing.contrib.concurrent;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;

import java.util.concurrent.Callable;

/**
 * @author Pavol Loffay
 */
public class TracedCallable<V> implements Callable<V> {

  private final Callable<V> delegate;
  private final Span span;
  private final Tracer tracer;

  public TracedCallable(Callable<V> delegate, Tracer tracer) {
    this(delegate, tracer, tracer.activeSpan());
  }

  public TracedCallable(Callable<V> delegate, Tracer tracer, Span span) {
    this.delegate = delegate;
    this.tracer = tracer;
    this.span = span;
  }

  @Override
  public V call() throws Exception {
    Scope scope = span == null ? null : tracer.scopeManager().activate(span);
    try {
      return delegate.call();
    } finally {
      if (scope != null) {
        scope.close();
      }
    }
  }
}
