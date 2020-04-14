package au.com.livewire;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;

/**
 * Dumb thread-safe ASCII file trading journal.
 */
public class FileBasedTradingJournal extends SimpleFileBasedTradingJournal implements TradingJournal {
  private final static int MAX_LOCK_RETRIES = 5;
  private final static long MAX_LOCK_WAIT_MS = 10000L;

  private final static long DEFAULT_LOCK_DURATION_MS = 1000L;

  private final static long MAX_LOCK_DURATION_MS = 10000L;

  // how long to wait before retrying to acquire lock
  private final static long LOCK_WAIT_DURATION_MS = 500L;

  /**
   * Lock file which is created to prevent others from accessing the file.
   * This file contains a number, representing the milliseconds since epoch 1970
   * that its lock expires.
   */
  @Inject
  @Named("journalLockFile")
  public File lockFile;

  public FileBasedTradingJournal(
      @Named("journalFile") File journalFile,
      @Named("journalLockFile") File journalLockFile
  ) {
    super(journalFile);
    this.lockFile = journalLockFile;
  }

  public synchronized void lock(
      final long timeoutMs
  ) throws JournalLockedException, IOException {
    if (timeoutMs > MAX_LOCK_DURATION_MS) {
      throw new IllegalArgumentException(
          String.format("Lock timeout must be not exceed %dms", MAX_LOCK_DURATION_MS)
      );
    }
    long then = System.currentTimeMillis();
    long retryLockCount = 0;
    Long lockExpiry;
    long elapsed;

    // be patient trying to acquire a lock on the journal.
    do {
      // check for existence of a lock file.
      if ((lockExpiry = getLockExpiry()) != null) {
        try {
          Thread.sleep(LOCK_WAIT_DURATION_MS);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      elapsed = System.currentTimeMillis() - then;
    } while(++retryLockCount < MAX_LOCK_RETRIES && elapsed < MAX_LOCK_WAIT_MS && lockExpiry != null);

    if (lockExpiry != null) {
      throw new JournalLockedException("Unable to acquire lock");
    }

    String expiryText = String.format("%d", System.currentTimeMillis() + timeoutMs);
    FileWriter lockWriter;
    try {
      // re-write the lock file from the start.
      lockWriter = new FileWriter(lockFile, false);
      lockWriter.append(expiryText).append("\n");
      lockWriter.close();
    } catch (IOException e) {
      throw e;
    }
    return;
  }

  /**
   * Whether a lock has been placed upon this journal
   * @return
   */
  public synchronized boolean isLocked() {
    final boolean result;
    result = getLockExpiry() != null;
    return result;
  }

  /**
   * Returns null if there is no active lock present or a non-null value
   * is there is an active lock present.
   * @return timestamp when the current lock expires, or null if there is
   *     no lock file present.
   */
  public synchronized Long getLockExpiry() {
    final Long result;
    result = doGetLockExpiry(MAX_LOCK_RETRIES);
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
    long now = System.currentTimeMillis();
    Long result = null;

    // if no lock file exists ... there is no lock.
    if (lockFile.exists()) {
      // has it expired?
      FileReader lockFileReader;
      try {
        lockFileReader = new FileReader(lockFile);
        BufferedReader br = new BufferedReader(lockFileReader);
        String text;
        try {
          if (((text = br.readLine()) != null) && StringUtils.isNotBlank(text) && (StringUtils.isNumeric(text))) {
            long expiry = Long.parseLong(text.trim());
            if (expiry >= now) {
              result = expiry;
              return result;
            } else {
              // lock has expired ... remove it.
              releaseLock();
            }
          }
        } catch (IOException e) {
          // dealing with a corrupt lock file :-( if it still exists ... remove it.
          System.err.println("WARN - detected corrupt lock file .. removing it");
          System.err.flush();
          if (lockFile.exists()) {
            lockFile.delete();
          }
          return null;
        } finally {
          try {
            br.close();
            lockFileReader.close();
          } catch (IOException ignoreMe) {
            // YOLO
          }
        }
      } catch (FileNotFoundException e) {
        // somebody yanked it from underneath us .... if our retryCount hasn't expired .. try again
        if (retryCount > 0) {
          doGetLockExpiry(retryCount - 1);
        }
      }
    }
    return result;
  }

  /**
   * We trust that whomever is releasing the lock also acquired it!!
   */
  public synchronized void releaseLock() {
    if (lockFile.exists()) {
      lockFile.delete();
    }
  }
}