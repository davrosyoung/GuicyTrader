package au.com.livewire;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * Dumb thread-safe ASCII file trading journal. Whilst it pretends
 * to take care of locking, it doesn't actually. This is a first-cut
 * proof of concept implementation that needs to be replaced.
 */
public class SimpleFileBasedTradingJournal implements TradingJournal {
  /**
   * File containing the data we want.
   */
  @Inject
  @Named("journalFile")
  public File file;

  public SimpleFileBasedTradingJournal(
      @Named("journalFile") File file
  ) {
    this.file = file;
  }

  @Override
  public synchronized void add(Trade trade) throws IOException {

    if (file == null) {
      throw new IllegalStateException("Unable to add to journal without file reference");
    }

    FileWriter writer;
    writer = new FileWriter(file, true);
    String entry = trade.toCsv();
    writer.append(entry).append("\n");
    writer.flush();
    writer.close();
  }

  @Override
  public synchronized List<Trade> list() throws IOException {
    final List<Trade> result;
    result = new ArrayList<>();

    do {
      // if no journal file yet exists ... just return an empty list.
      if (!file.exists() || !file.canRead()) {
        System.err.println("WARN - journal file \"" + file.getCanonicalPath() + "\" does not yet exist");
        System.err.flush();
        break;
      }

      BufferedReader br;
      FileReader reader;
      reader = new FileReader(file);
      br = new BufferedReader(reader);
      String line;
      int count = 0;
      while ((line = br.readLine()) != null) {
        count++;
        final Trade entry;
        try {
          entry = Trade.fromCsv(line);
          result.add(entry);
        } catch (IllegalArgumentException e) {
          final String msg = String.format("%s - at line %d", e.getMessage(), count);
          System.err.println(msg);
          System.err.flush();
          br.close();
          reader.close();
          throw new IllegalArgumentException(msg, e);
        }
      }
      br.close();
      reader.close();

    } while (false);

    return result;
  }

  public synchronized void lock(
      final long timeoutMs
  ) throws JournalLockedException, IOException {
    // dummy implementation
    return;
  }

  /**
   * Whether a lock has been placed upon this journal
   * @return
   */
  public synchronized boolean isLocked() {
    // dummy implementation
    return false;
  }

  /**
   * Returns null if there is no active lock present or a non-null value
   * is there is an active lock present.
   * @return timestamp when the current lock expires, or null if there is
   *     no lock file present.
   */
  public synchronized Long getLockExpiry() {
    final Long result;
    result = null; // dummy implementation
    return result;
  }

  /**
   * Re-entrant method which allows us to retry getting a lock file if
   * it disappears between checking for its existence and trying to
   * read its contents.
   * @param retryCount how many times to re-enter this method if the lock file goes missing.
   * @return timestamp of a non-expired lock, or null if there is no lock or it has expired (and been removed).
   */
  private Long doGetLockExpiry(final int retryCount) {
    return null; // dummy implementation.
  }

  /**
   * We trust that whomever is releasing the lock also acquired it!!
   */
  public synchronized void releaseLock() {
    // dummy implementation
    return;
  }
}
