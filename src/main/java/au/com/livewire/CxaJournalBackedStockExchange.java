package au.com.livewire;

public class CxaJournalBackedStockExchange extends JournalBackedStockExchange {
  public CxaJournalBackedStockExchange(
      TradingJournal tradingJournal,
      int currentBrokerage
  ) {
    super(ExchangeCode.CXA, tradingJournal, currentBrokerage);
  }
}
