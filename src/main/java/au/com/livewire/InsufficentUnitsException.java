package au.com.livewire;

public class InsufficentUnitsException extends IllegalArgumentException {

  public InsufficentUnitsException(final String message) {
    super(message);
  }

  public InsufficentUnitsException(final String message, final Throwable causal) {
    super(message, causal);
  }
}
