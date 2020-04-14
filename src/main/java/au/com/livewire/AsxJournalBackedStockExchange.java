package au.com.livewire;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class AsxJournalBackedStockExchange extends JournalBackedStockExchange {
  public AsxJournalBackedStockExchange(
      TradingJournal tradingJournal,
      int currentBrokerage
  ) {
    super(ExchangeCode.ASX, tradingJournal, currentBrokerage);
  }
}
