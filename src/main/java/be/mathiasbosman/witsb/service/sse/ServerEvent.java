package be.mathiasbosman.witsb.service.sse;

public interface ServerEvent<E> {

  Object payload();

  E eventType();
}
