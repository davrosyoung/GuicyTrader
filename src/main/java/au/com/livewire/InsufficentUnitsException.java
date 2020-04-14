package au.com.livewire;

/**
 * Indicates that there are not sufficent units of a particular
 * stock for purchase. One must investgate the exception message to
 * infer the company in question.
 */
public class InsufficentUnitsException extends IllegalArgumentException {

  public InsufficentUnitsException(final String message) {
    super(message);
  }

  public InsufficentUnitsException(final String message, final Throwable causal) {
    super(message, causal);
  }
}
