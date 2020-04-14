package au.com.livewire;

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
