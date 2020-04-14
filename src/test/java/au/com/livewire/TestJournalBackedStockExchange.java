package au.com.livewire;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestJournalBackedStockExchange {

  TradingJournal tradingJournal;
  JournalBackedStockExchange instance;

  @Before
  public void setup() {
    tradingJournal = new InMemoryTradingJournal();
    instance = new JournalBackedStockExchange(ExchangeCode.ASX, tradingJournal, 3);
  }

  @Test
  public void testListingVolumesFromEmptyExchange() throws IOException {
    Map<String, Integer> result = instance.getOrderBookTotalVolume();
    assertNotNull(result);
    assertEquals(0, result.size());
  }

  @Test
  public void testListingVolumesFromExchangeWithSellsThenBuys() throws IOException {
    instance.sell("CBA", 100);
    instance.sell("QAN", 100);
    instance.sell("NAB", 100);
    instance.buy("CBA", 47);
    instance.buy("QAN", 13);
    instance.buy("NAB", 99);
    Map<String, Integer> result = instance.getOrderBookTotalVolume();
    assertNotNull(result);
    assertEquals(53, result.getOrDefault("CBA", 0).intValue());
    assertEquals(87, result.getOrDefault("QAN", 0).intValue());
    assertEquals(1, result.getOrDefault("NAB", 0).intValue());

    assertEquals(0.18D, instance.getTradingCosts().doubleValue(), 0.001D);
  }

  @Test(expected = InsufficentUnitsException.class)
  public void testBuyingWhereInsufficentVolumeAvailable() throws IOException {
    instance.sell("CBA", 100);
    instance.buy("CBA", 101);
  }

  @Test(expected = InvalidCodeException.class)
  public void testBuyingWithInvalidCompanyCode() throws IOException {
    instance.sell("CBA", 100);
    instance.buy("DBA", 101);
  }

  @Test
  public void testTradingCostsFromEmptyExchange() throws IOException {
    BigDecimal result = instance.getTradingCosts();
    assertNotNull(result);
    assertEquals(0.00D, result.doubleValue(), 0.000001D);
  }
}
