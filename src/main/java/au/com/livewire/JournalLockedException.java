package au.com.livewire;

/**
 * Thrown when an attempt to access a journal which is locked.
 */
public class JournalLockedException extends Exception {
  public JournalLockedException(String message) {
    super(message);
  }

  public JournalLockedException(
      final String message,
      final Throwable causal
  ) {
    super(message, causal);
  }
}
