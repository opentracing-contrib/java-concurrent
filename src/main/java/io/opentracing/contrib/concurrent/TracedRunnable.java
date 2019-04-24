package io.opentracing.contrib.concurrent;

import io.opentracing.Scope;
import io.opentracing.Tracer;
import io.opentracing.Span;
import io.opentracing.util.AutoFinishScope;
import io.opentracing.util.AutoFinishScopeManager;

/**
 * @author Pavol Loffay
 * @author Jose Montoya
 */
public class TracedRunnable implements Runnable {

  private final Runnable delegate;
  private final Span span;
  private final Tracer tracer;
  private final AutoFinishScope.Continuation cont;

  public TracedRunnable(Runnable delegate, Tracer tracer) {
    this.delegate = delegate;
    this.tracer = tracer;
    this.span = tracer.activeSpan();

	if (tracer.scopeManager() instanceof AutoFinishScopeManager && tracer.scopeManager().active() != null)
	  cont = ((AutoFinishScope) tracer.scopeManager().active()).capture();
	else
	  cont = null;
  }

  @Override
  public void run() {
	Scope scope = null;
	if (cont != null)
	  scope = cont.activate();
	else if (span != null)
      scope = tracer.scopeManager().activate(span, false);

	try {
      delegate.run();
    } finally {
      if (scope != null) {
        scope.close();
      }
    }
  }
}
