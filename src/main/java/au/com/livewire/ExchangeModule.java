package au.com.livewire;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import java.io.File;
import java.util.Properties;

/**
 * Wires up the CXA and ASX stock exchanges..
 */
public class ExchangeModule extends AbstractModule {
  ExchangeCode exchangeCode;
  Properties properties;

  ExchangeModule(
      final ExchangeCode exchangeCode,
      final Properties properties
  )
  {
    this.exchangeCode = exchangeCode;
    this.properties = properties;
  }

  @Override
  protected void configure() {
    bind(Assignment.class);
  }

  @Named("tradingJournal")
  @Provides TradingJournal getTradingJournal(
      @Named("journalFile") File journalFile,
      @Named("journalLockFile") File journalLockFile
  ) {
    TradingJournal result;
    result = new FileBasedTradingJournal(journalFile, journalLockFile);
    return result;
  }

  @Named("journalFile")
  @Provides File getJournalFile(
      final @Named("exchangeCode") ExchangeCode exchangeCode,
      final AppProperties appProperties
  ) {
    File result = null;
    switch(exchangeCode) {
      case ASX:
        result = appProperties.getAsxJournalFile();
        break;
      case CXA:
        result = appProperties.getCxaJournalFile();
        break;
      default:
        System.err.println("ERROR - Unknown exchange code \"" + exchangeCode + "\"");
      break;
    }
    return result;
  }

  @Named("journalLockFile")
  @Provides File getJournalLockFile(
      final AppProperties appProperties
  ) {
    File result = null;
    switch(this.exchangeCode) {
      case ASX:
        result = appProperties.getAsxJournalLockFile();
        break;
      case CXA:
        result = appProperties.getCxaJournalLockFile();
        break;
      default:
        System.err.println("ERROR - Unknown exchange code \"" + exchangeCode + "\"");
      break;
    }
    return result;
  }

  @Provides
  AppProperties getAppProperties() {
    final AppProperties result;
    result  = new AppProperties(this.properties);
    return result;
  }

  @Provides
  @Named("exchangeCode")
  ExchangeCode getExchangeCode() {
    return this.exchangeCode;
  }

  @Provides
  StockExchange getStockExchange(
      @Named("tradingJournal") TradingJournal tradingJournal,
      @Named("exchangeCode") ExchangeCode exchangeCode,
      @Named("currentBrokerage") int currentBrokerage
  ) {
    StockExchange result;
    result = new JournalBackedStockExchange(exchangeCode, tradingJournal, currentBrokerage);
    return result;
  }

  @Provides
  @Named("currentBrokerage")
  int getCurrentBrokerage(@Named("exchangeCode") ExchangeCode exchangeCode) {
    int result = 0;
    switch(exchangeCode) {
      case ASX:
        result = getAppProperties().getAsxBrokerageCost();
        break;
      case CXA:
        result = getAppProperties().getCxaBrokerageCost();
        break;
      default:
        System.err.println("ERROR - UNKNOWN exchange code " + getExchangeCode());
        System.err.flush();
        break;
    }
    return result;
  }
}
