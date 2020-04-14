package au.com.livewire;

public class InvalidCodeException extends IllegalArgumentException {

  public InvalidCodeException(final String message) {
    super(message);
  }

  public InvalidCodeException(final String message, final Throwable causal) {
    super(message, causal);
  }
}
