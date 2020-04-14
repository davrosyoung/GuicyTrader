package au.com.livewire;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * Implementation of a stock exchange backed by a journal of transactions.
 */
public class JournalBackedStockExchange implements StockExchange {
  /**
   * How much each transaction currently costs for this stock exchange.
   */
  private int currentBrokerage;

  /**
   * Identifies the exchange, currently just two available, ASX and CXA.
   */
  ExchangeCode exchangeCode;

  /**
   * The journal used to record each and every transaction,
   */
  TradingJournal journal;

  public JournalBackedStockExchange(
      ExchangeCode exchangeCode,
      TradingJournal tradingJournal,
      int currentBrokerage
  ) {
    this.exchangeCode = exchangeCode;
    this.journal = tradingJournal;
    this.currentBrokerage = currentBrokerage;
  }

  @Override
  public void buy(
      final String code,
      final Integer units
  ) throws IllegalArgumentException, IOException {
    Trade trade;
    Date when = new Date();
    CompanyCode companyCode;
    if (StringUtils.isBlank(code)) {
      throw new InvalidCodeException("Must provide non-null, non-blank company code");
    }
    if (units == null || units < 1) {
      throw new IllegalArgumentException("Must provide non-null, positive whole number for units, value provided was \"" + units + "\"");
    }
    try {
      companyCode = CompanyCode.valueOf(code.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new InvalidCodeException(String.format("Specified company code \"%s\" not valid", code));
    }

    trade = new Trade(exchangeCode, Trade.TransactionType.BUY, when, companyCode, units, currentBrokerage);

    try {
      journal.lock(5000L);
    } catch (JournalLockedException wtf) {
      final String msg;
      msg = String.format("ERROR - FAILED to lock journal to process BUY request \"%s\"", trade);
      System.err.println("ERROR " + msg);
      System.err.flush();
      throw new IllegalStateException(msg, wtf);
    }

    // ensure that there is a sufficient quantify to buy
    List<Trade> tradeList;
    try {
      tradeList = journal.list();
    } catch (FileNotFoundException firstTimeAroundException) {
      // this can happen if there isn't yet a journal file in existence..
      tradeList = null;
    }
    // if there are NO trades, then there cannot possible be sufficent to buy.
    if (tradeList == null || tradeList.size() < 1) {
      journal.releaseLock();
      throw new InsufficentUnitsException(
          String.format("Exchange \"%s\" does not contain sufficient units of \"%s\" to honour trade",
              exchangeCode, code)
      );
    }

    int buyCount = tradeList.stream()
        .filter(t -> Trade.TransactionType.BUY == t.getTransactionType())
        .filter(t -> exchangeCode == t.getExchangeCode())
        .filter(t -> companyCode == t.getCompanyCode())
        .map(t -> t.getQuantity())
        .mapToInt(Integer::intValue)
        .sum();
    int sellCount = tradeList.stream()
        .filter(t -> Trade.TransactionType.SELL == t.getTransactionType())
        .filter(t -> exchangeCode == t.getExchangeCode())
        .filter(t -> companyCode == t.getCompanyCode())
        .map(t -> t.getQuantity())
        .mapToInt(Integer::intValue)
        .sum();
    int surplus = sellCount - buyCount;
    if (surplus < 0) {
      final String msg = String.format(
          "FATAL - journal corrupt ... invalid negative "
              + "quantity (%d) of stock \"%s\"", surplus, companyCode);
      System.err.println(msg);
      journal.releaseLock();
      throw new IllegalStateException(msg);
    }

    if (surplus < trade.getQuantity()) {
      final String msg = String.format(
          "Insufficient units (%d) available for stock \"%s\" "
          + "to accommodate purchase of %d units",
          surplus, companyCode, trade.getQuantity());
      journal.releaseLock();
      throw new InsufficentUnitsException(msg);
    }

    journal.add(trade);
    journal.releaseLock();
    return;
  }

  @Override
  public void sell(String code, Integer units) throws IllegalArgumentException {
    Trade trade;
    Date when = new Date();
    CompanyCode companyCode;
    if (StringUtils.isBlank(code)) {
      throw new InvalidCodeException("Must provide non-null, non-blank company code");
    }
    if (units == null || units < 1) {
      throw new IllegalArgumentException("Must provide non-null, positive whole number for units. Value provided was \"" + units + "\"");
    }
    try {
      companyCode = CompanyCode.valueOf(code.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new InvalidCodeException(String.format("Specified company code \"%s\" not valid", code));
    }

    trade = new Trade(exchangeCode, Trade.TransactionType.SELL, when, companyCode, units, currentBrokerage);
    try {
      journal.lock(5000L);
      journal.add(trade);
      journal.releaseLock();
    } catch (JournalLockedException wtf) {
      final String msg;
      msg = String.format("ERROR - FAILED to lock journal to process BUY request \"%s\"", trade);
      System.err.println("ERROR " + msg);
      System.err.flush();
      throw new IllegalStateException(msg, wtf);
    } catch (IOException ioe) {
      final String msg;
      msg = String.format("FAILED to append sell trade \"%s\" to journal.", trade);
      System.err.println("FATAL - " + msg);
      throw new IllegalStateException(msg, ioe);
    }
  }

  @Override
  public Map<String, Integer> getOrderBookTotalVolume() throws IOException, IllegalStateException {
    final List<Trade> tradeList;
    try {
      journal.lock(5000L);
    } catch (JournalLockedException wtf) {
      final String msg;
      msg = String.format("ERROR - FAILED to lock journal to calculate order book totals.");
      System.err.println("ERROR " + msg);
      System.err.flush();
      throw new IllegalStateException(msg, wtf);
    }
    tradeList = journal.list();
    journal.releaseLock();
    Map<String, Integer> result;
    result = new HashMap<>();
    int lineCount = 0;
    for (Trade trade : tradeList) {
      // if the trade was not a part of this exchange, ignore it.
      if (!(exchangeCode == trade.getExchangeCode())) {
        continue;
      }
      final String key = trade.getCompanyCode().name();
      int existing = result.getOrDefault(key, 0);
      int after = Trade.TransactionType.SELL == trade.getTransactionType()
          ? existing + trade.getQuantity()
          : existing - trade.getQuantity();
      lineCount++;
      if (after < 0) {
        throw new IllegalStateException(
            String.format("Corrupt journal! At line %d, negative quantity of stock \"%s\"",
                lineCount, key)
        );
      }
      result.put(key, after);
    }
    return result;
  }

  @Override
  public BigDecimal getTradingCosts() throws IOException {
    final BigDecimal result;
    int brokerageSumCents = 0;
    final List<Trade> tradeList;
    tradeList = journal.list();
    do {
      // no point pursuing this if the journal is empty.
      if ((tradeList == null) || tradeList.size() == 0) {
        break;
      }

      /* we can't apply the same brokerage to each trade, as the charge
       * is subject to fluctuate throughout a trading period.
       */
      brokerageSumCents = tradeList.stream()
          .filter(trade -> exchangeCode == trade.getExchangeCode())
          .map(Trade::getBrokerage)
          .filter(Objects::nonNull)
          .mapToInt(Integer::intValue)
          .sum();

    } while (false);
    // return the result in dollars rather than cents.
    result = BigDecimal.valueOf((double)brokerageSumCents / 100.0D);
    return result;
  }
}
