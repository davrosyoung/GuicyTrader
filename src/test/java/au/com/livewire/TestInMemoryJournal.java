package au.com.livewire;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestInMemoryJournal {

  TradingJournal tradingJournal;

  @Before
  public void setup() throws IllegalArgumentException, IOException {
    long when = 1023439393000L;
    tradingJournal = new InMemoryTradingJournal();
    tradingJournal.add(new Trade(ExchangeCode.ASX, Trade.TransactionType.SELL, new Date(when), CompanyCode.NAB, 58, 5));
    tradingJournal.add(new Trade(ExchangeCode.ASX, Trade.TransactionType.SELL, new Date(when + 2), CompanyCode.CBA, 43, 5));
    tradingJournal.add(new Trade(ExchangeCode.ASX, Trade.TransactionType.SELL, new Date(when + 4), CompanyCode.QAN, 27, 5));
  }

  @Test
  public void testReadingEntries() throws IOException {
    Collection<Trade> result;
    result = tradingJournal.list();
    assertNotNull(result);
    assertEquals(3, result.size());

    // all trades should belong to the ASX exchange
    assertTrue(result.stream().allMatch(trade -> ExchangeCode.ASX == trade.getExchangeCode()));
    // there should be a trade of 43 units against CBA
    assertTrue(result.stream().anyMatch(trade -> Trade.TransactionType.SELL == trade.getTransactionType() && 43 == trade.getQuantity() && CompanyCode.CBA == trade.getCompanyCode()));
    // there should be a trade of 27 units against QAN
    assertTrue(result.stream().anyMatch(trade -> Trade.TransactionType.SELL == trade.getTransactionType() && 27 == trade.getQuantity() && CompanyCode.QAN == trade.getCompanyCode()));
    // there should be a trade of 58 units against NAB
    assertTrue(result.stream().anyMatch(trade -> Trade.TransactionType.SELL == trade.getTransactionType() && 58 == trade.getQuantity() && CompanyCode.NAB == trade.getCompanyCode()));
  }

  public void testAddingEntires() throws IOException {
    tradingJournal.add(new Trade(ExchangeCode.CXA, Trade.TransactionType.BUY, new Date(1023439393008L), CompanyCode.QAN, 7, 5));
  }
}
