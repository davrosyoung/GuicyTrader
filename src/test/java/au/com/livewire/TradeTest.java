package au.com.livewire;

import java.util.Date;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

public class TradeTest {

  /**
   * Happy case.
   */
  @Test
  public void testConvertingFromLegitBuyCSV() {
    final String source = "ASX,BUY,1023439393000,NAB,23,7";
    final Trade result = Trade.fromCsv(source);
    assertNotNull(result);
    assertNotNull(result.getTimestamp());
    assertEquals(new Date(1023439393000L), result.getTimestamp());

    assertNotNull(result.getExchangeCode());
    assertEquals(ExchangeCode.ASX, result.getExchangeCode());

    assertNotNull(result.getTransactionType());
    assertEquals(Trade.TransactionType.BUY, result.getTransactionType());

    assertNotNull(result.getCompanyCode());
    assertEquals(CompanyCode.NAB, result.getCompanyCode());

    assertNotNull(result.getQuantity());
    assertEquals(23, result.getQuantity().intValue());

    assertNotNull(result.getBrokerage());
    assertEquals(7, result.getBrokerage().intValue());
  }

  /**
   * Happy case.
   */
  @Test
  public void testConvertingFromLegitBuySell() {
    final String source = "CXA,SELL,1023439393000,CBA,23,5";
    final Trade result = Trade.fromCsv(source);
    assertNotNull(result);
    assertNotNull(result.getTimestamp());
    assertEquals(new Date(1023439393000L), result.getTimestamp());

    assertNotNull(result.getExchangeCode());
    assertEquals(ExchangeCode.CXA, result.getExchangeCode());

    assertNotNull(result.getTransactionType());
    assertEquals(Trade.TransactionType.SELL, result.getTransactionType());

    assertNotNull(result.getCompanyCode());
    assertEquals(CompanyCode.CBA, result.getCompanyCode());

    assertNotNull(result.getQuantity());
    assertEquals(23, result.getQuantity().intValue());


    assertNotNull(result.getBrokerage());
    assertEquals(5, result.getBrokerage().intValue());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConvertingFromCSVMissingField() {
    final String source = "ASX,BUY,1023439393000,23,5";
    Trade.fromCsv(source);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConvertingFromCSVWithInvalidCompanyCodeField() {
    final String source = "ASX,BUY,1023439393000,QFY,23,5";
    Trade.fromCsv(source);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConvertingFromCSVWithInvalidTimestampField() {
    final String source = "ASX,BUY,102343939300O,QFY,23,5";
    Trade.fromCsv(source);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConvertingFromCSVWithInvalidTransactionTypeField() {
    final String source = "ASX,BYE,1023439393000,QFY,23,5";
    Trade.fromCsv(source);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConvertingFromCSVWithInvalidQuantityField() {
    final String source = "ASX,BYE,1023439393000,QFY,0.23,5";
    Trade.fromCsv(source);
  }
}
