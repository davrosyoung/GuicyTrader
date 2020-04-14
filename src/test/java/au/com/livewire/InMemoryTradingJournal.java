package au.com.livewire;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A simpleified implementation of the trading journal which allows us
 * to run unit tests without writing to the filesystem.
 */
public class InMemoryTradingJournal implements TradingJournal {
  public List<Trade> entries = new CopyOnWriteArrayList<>();

  @Override
  public synchronized void add(Trade trade) throws IOException {
    entries.add(trade);
  }

  @Override
  public synchronized List<Trade> list() throws IOException {
    List<Trade> result;
    result = new ArrayList<>();
    result.addAll(entries);
    return result;
  }

  @Override
  public void lock(long timeoutMs) throws JournalLockedException, IOException {
    // dummy implementation
    return;
  }

  @Override
  public boolean isLocked() {
    return false;
  }

  @Override
  public void releaseLock() {
    // dummy implementation
    return;
  }
}
