package au.com.livewire;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents an entry within the trading journal, which is either a
 * buy or sell transaction.
 */
@Data
@Builder
public class Trade {

  /**
   * Distinguishes buy trades from sell trades.
   */
  enum TransactionType {
    BUY, SELL
  }

  /**
   * The exchange used to conduct this transaction.
   */
  private ExchangeCode exchangeCode;

  /**
   * Was this a buy (from the exchange) or a sell (to the exchange) transaction?
   */
  private TransactionType transactionType;

  /**
   * When the trade took place
   */
  private Date when;

  /**
   * The company that the trade took place for.
   */
  private CompanyCode code;

  /**
   * How many units were bought or sold?
   */
  private Integer quantity;

  /**
   * Cost of the transaction (in cents).
   */
  private Integer brokerage;

  @Builder
  public Trade(
      final ExchangeCode exchangeCode,
      final TransactionType type,
      final Date when,
      final CompanyCode code,
      final Integer quantity,
      final Integer brokerage
  ) {
    this.exchangeCode = exchangeCode;
    this.transactionType = type;
    this.when = when;
    this.code = code;
    this.quantity = quantity;
    this.brokerage = brokerage;
  }

  public ExchangeCode getExchangeCode() {
    return this.exchangeCode;
  }

  public TransactionType getTransactionType() {
    return this.transactionType;
  }

  public CompanyCode getCompanyCode() {
    return this.code;
  }

  public Date getTimestamp() {
    return this.when;
  }

  public Integer getQuantity() {
    return this.quantity;
  }

  public Integer getBrokerage() {
    return this.brokerage;
  }

  public String toCsv() {
    final String result;
    if (exchangeCode == null || transactionType == null || when == null || code == null) {
      StringBuilder missingBob = new StringBuilder();
      Set<String> missingFieldSet = new HashSet<>();
      if (exchangeCode == null) {missingFieldSet.add("exchange");}
      if (transactionType == null) {missingFieldSet.add("transactionType");}
      if (when == null) {missingFieldSet.add("timestamp");}
      if (code == null) {missingFieldSet.add("companyCode");}
      throw new IllegalStateException(
          String.format(
              "Unable to persist trade, some fields (%s) are missing.",
              missingFieldSet.stream().collect(Collectors.joining(","))
          )
      );
    }

    // OK to use string builder here, separate instance per invocation.
    StringBuilder bob = new StringBuilder();
    bob.append(exchangeCode.name())
        .append(",")
        .append(transactionType.name())
        .append(",")
        .append(when.getTime())
        .append(",")
        .append(code.name())
        .append(",")
        .append(quantity)
        .append(",")
        .append(brokerage);
    result = bob.toString();
    return result;
  }

  /**
   * Ugly manual parsing of a line of csv to extract a trade entry. Far more
   * elegant solutions possible with more time available.
   * @param candidate the (hopefully comma separated) text to be interrogated
   * @return a trade representing the data specified in the CSV file.
   * @throws IllegalArgumentException when things go awry.
   */
  public static Trade fromCsv(
      final String candidate
  ) throws IllegalArgumentException {
    Trade result = null; // our resultant trade
    final String[] bits; // the textual elements of the csv entry being interrogated.
    final ExchangeCode exchangeCode; // which exchange does this trade belong to?
    final TransactionType transactionType; // is this a buy or sell?
    final Date when; // proposed timestamp of the trade
    final CompanyCode code; // identifies the company that the trade is associated with.
    final Integer quantity; // how many units have been transferred.
    final Integer brokerage; // what did the exchange charge for the transaction at the time it occurred?
    String bit; // current field from the csv entry being interrogated
    int cursor = 0;
    if (StringUtils.isBlank(candidate)) {
      throw new IllegalArgumentException("Must provide non-null, non-blank candidate");
    }
    bits = candidate.split(",");
    if (bits == null || bits.length != 6) {
      throw new IllegalArgumentException(
          String.format("provided string \"%s\" does not contain date, symbol and quantity", candidate)
      );
    }

    // all three fields must be non-null and non-blank
    if (StringUtils.isBlank(bits[0])
        || StringUtils.isBlank(bits[1])
        || StringUtils.isBlank(bits[2])
        || StringUtils.isBlank(bits[3])
        || StringUtils.isBlank(bits[4])
        || StringUtils.isBlank(bits[5])
    ) {
      throw new IllegalArgumentException(
          String.format(
              "exchange code, transaction-type, timestamp, company code, "
                  + "amount and brokerage fields must all be present "
                  + "... invalid entry \"%s\"", candidate)
      );
    }

    // the first field must represent the exchange
    bit = bits[cursor++];
    try {
      exchangeCode = ExchangeCode.valueOf(bit.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          String.format("Field \"%d\" from line \"%s\" does not represent a valid exchange",
              cursor, bit, candidate)
      );
    }

    // the second field must represent the transaction type
    bit = bits[cursor++];
    try {
      transactionType = TransactionType.valueOf(bit.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          String.format("Field \"%d\" from line \"%s\" does not represent a valid transaction type",
              cursor, bit, candidate)
      );
    }

    // the timestamp field must represent a positive integer value...
    bit = bits[cursor++];
    if (!StringUtils.isNumeric(bit)) {
      throw new IllegalArgumentException(
          String.format("Field %d \"%s\" from line \"%s\" does not represent a valid timestamp",
              cursor, bit, candidate)
      );
    }
    when = new Date(Long.parseLong(bit));

    // the fourth field must represent a valid company code...
    bit = bits[cursor++];
    try {
      code = CompanyCode.valueOf(bit.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          String.format(
              "Column %d \"%s\" from line \"%s\" does not represent a valid company code",
              cursor, bit, candidate)
      );
    }

    // fifth field is the quantity of units exchanged
    bit = bits[cursor++];
    if (!StringUtils.isNumeric(bit)) {
      throw new IllegalArgumentException(
          String.format("Field %d \"%s\" from line \"%s\" does not represent a valid quantity",
              cursor, bit, candidate)
      );
    }
    quantity = Integer.parseInt(bit);

    // last field is the brokerage charge.
    bit = bits[cursor++];
    if (!StringUtils.isNumeric(bit)) {
      throw new IllegalArgumentException(
          String.format("Field %d \"%s\" from line \"%s\" does not represent a valid brokerage",
              cursor, bit, candidate)
      );
    }
    brokerage = Integer.parseInt(bit);

    result = new Trade(exchangeCode, transactionType, when, code, quantity, brokerage);
    return result;
  }
}
