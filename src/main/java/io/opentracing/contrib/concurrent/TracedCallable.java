package io.opentracing.contrib.concurrent;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.AutoFinishScope;
import io.opentracing.util.AutoFinishScopeManager;

import java.util.concurrent.Callable;

/**
 * @author Pavol Loffay
 * @author Jose Montoya
 */
public class TracedCallable<V> implements Callable<V> {

  private final Callable<V> delegate;
  private final Span span;
  private final Tracer tracer;
  private final AutoFinishScope.Continuation cont;

  public TracedCallable(Callable<V> delegate, Tracer tracer) {
    this.delegate = delegate;
    this.tracer = tracer;
    this.span = tracer.activeSpan();

    if (tracer.scopeManager() instanceof AutoFinishScopeManager && tracer.scopeManager().active() != null)
      cont = ((AutoFinishScope) tracer.scopeManager().active()).capture();
    else
      cont = null;
  }

  @Override
  public V call() throws Exception {
    Scope scope = null;
    if (cont != null)
      scope = cont.activate();
    else if (span != null)
      scope = tracer.scopeManager().activate(span, false);

    try {
      return delegate.call();
    } finally {
      if (scope != null) {
        scope.close();
      }
    }
  }
}
