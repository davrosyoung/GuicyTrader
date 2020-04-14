package au.com.livewire;

import com.google.inject.Inject;
import java.io.File;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;

/**
 * Those properties which dictate what attributes are used to configure
 * the application with.
 */
public class AppProperties {
  Properties properties;

  final static private String CXA_BROKERAGE_KEY = "cxa.brokerage";
  final static private String ASX_BROKERAGE_KEY = "asx.brokerage";
  final static private String CXA_JOURNAL_PATH_KEY = "cxa.journal";
  final static private String CXA_JOURNAL_LOCK_PATH_KEY = "cxa.journal.lock";
  final static private String ASX_JOURNAL_PATH_KEY = "asx.jopurnal";
  final static private String ASX_JOURNAL_LOCK_PATH_KEY = "asx.journal.lock";

  final static private int DEFAULT_CXA_BROKERAGE = 5;
  final static private int DEFAULT_ASX_BROKERAGE = 5;
  final static private String DEFAULT_CXA_JOURNAL_PATH = "journal.csv";
  final static private String DEFAULT_ASX_JOURNAL_PATH = "journal.csv";
  final static private String DEFAULT_CXA_JOURNAL_LOCK_PATH = "journal.lock";
  final static private String DEFAULT_ASX_JOURNAL_LOCK_PATH = "journal.lock";

  @Inject
  public AppProperties(Properties properties) {
    this.properties = properties;
  }

  int getCxaBrokerageCost() {
    final String rawText;
    final int result;
    rawText = properties.getProperty(CXA_BROKERAGE_KEY, Integer.toString(DEFAULT_CXA_BROKERAGE));
    result = StringUtils.isNotBlank(rawText) && StringUtils.isNumeric(rawText)
        ? Integer.parseInt(rawText) : DEFAULT_CXA_BROKERAGE;
    return result;
  }

  int getAsxBrokerageCost() {
    final String rawText;
    final int result;
    rawText = properties.getProperty(ASX_BROKERAGE_KEY, Integer.toString(DEFAULT_ASX_BROKERAGE));
    result = StringUtils.isNotBlank(rawText) && StringUtils.isNumeric(rawText)
        ? Integer.parseInt(rawText) : DEFAULT_ASX_BROKERAGE;
    return result;
  }

  File getCxaJournalFile() {
    final String rawText;
    final File result;
    rawText = properties.getProperty(CXA_JOURNAL_PATH_KEY, DEFAULT_CXA_JOURNAL_PATH);
    result = new File(rawText);
    return result;
  }

  File getCxaJournalLockFile() {
    final String rawText;
    final File result;
    rawText = properties.getProperty(CXA_JOURNAL_LOCK_PATH_KEY, DEFAULT_CXA_JOURNAL_LOCK_PATH);
    result = new File(rawText);
    return result;
  }

  File getAsxJournalFile() {
    final String rawText;
    final File result;
    rawText = properties.getProperty(ASX_JOURNAL_PATH_KEY, DEFAULT_ASX_JOURNAL_PATH);
    result = new File(rawText);
    return result;
  }

  File getAsxJournalLockFile() {
    final String rawText;
    final File result;
    rawText = properties.getProperty(ASX_JOURNAL_LOCK_PATH_KEY, DEFAULT_ASX_JOURNAL_LOCK_PATH);
    result = new File(rawText);
    return result;
  }
}
