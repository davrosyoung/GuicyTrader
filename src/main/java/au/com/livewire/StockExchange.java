package au.com.livewire;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Represents an exchange that has units to buy or sell, keyed by a company code.
 */
public interface StockExchange {
  /**
   * Buy stock.
   * @param code company to purchase for
   * @param units number of units to purchase.
   */
  void buy(String code, Integer units) throws IllegalArgumentException, InsufficentUnitsException, IOException;

  /**
   * Sell stock.
   * @param code company to purchase for
   * @param units number of units to dispense of.
   */
  void sell(String code, Integer units) throws IllegalArgumentException;

  /**
   * Report aggregate volume available for each stock.
   * @return map, keyed by company code, containing the number of units
   *     available upon the exchange for that company.
   */
  Map<String, Integer> getOrderBookTotalVolume() throws IOException;

  /**
   * Returns dollar value of trading activity.
   * @return the total brokerage charged for all transactions.
   */
  BigDecimal getTradingCosts() throws IOException;
}
