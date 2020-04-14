package au.com.livewire;

import java.io.IOException;
import java.util.List;

/**
 * Journal of all trades made so far..
 */
public interface TradingJournal {
  /**
   * Cause a new trade to be added to the journal.
   * @param trade the trade to be added to the journal.
   */
  void add(Trade trade) throws IOException;

  /**
   * Return a list of all of the trades which have been added to the journal.
   * @return list of trades made so far.
   */
  List<Trade> list() throws IOException;

  /**
   * Causes the journal to be locked (upto a max time period),
   * so that nobody else may list nor add entries until then.
   */
  void lock(long timeoutMs) throws JournalLockedException, IOException;

  boolean isLocked();

  void releaseLock();
}
