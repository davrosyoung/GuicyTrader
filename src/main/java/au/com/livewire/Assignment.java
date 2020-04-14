package au.com.livewire;

import au.com.livewire.Trade.TransactionType;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;

import static au.com.livewire.Trade.TransactionType.BUY;
import static au.com.livewire.Trade.TransactionType.SELL;

public class Assignment {
  private StockExchange stockExchange;
  private ExchangeCode exchangeCode;
  private int currentBrokerage;

  private static final CompanyCode[] COMPANY_CODES = {CompanyCode.CBA, CompanyCode.NAB, CompanyCode.QAN};

  public static void main(String...args) {
    boolean lastParamExchange = false;
    boolean askedForHelp = false;
    boolean firstArg = true;
    boolean lastParamProperties = false;
    String propertiesPath = "src/main/resources/some.properties";
    String exchangeId = null;
    String invokedClassName = null;
    for(String arg : args) {
      if (firstArg) {
        invokedClassName = arg;
      }
      if (lastParamExchange) {
        exchangeId = arg;
      }
      if (lastParamProperties) {
        propertiesPath = arg;
      }
      if ("-exchange".equals(arg) || "--exchange".equals(arg)) {
        lastParamExchange = true;
      } else {
        lastParamExchange = false;
      }
      if ("-props".equals(arg) || "--props".equals(arg) || "-properties".equals(arg) || "--properties".equals(arg)) {
        lastParamProperties =  true;
      } else {
        lastParamProperties = false;
      }
      if ("-help".equals(arg) || "--help".equals(arg)) {
        askedForHelp = true;
      }
      firstArg = false;
    }

    if (StringUtils.isBlank(exchangeId)) {
      System.err.println("Must provide an exchange id");
      System.err.flush();
    }
    if (askedForHelp || StringUtils.isBlank(exchangeId)) {
      System.err.println(
          String.format("Usage: java %s -exchange CXA|ASX", invokedClassName)
      );
      System.err.flush();
      System.exit(StringUtils.isBlank(exchangeId) ? 1 : 0);
    }

    ExchangeCode exchangeCode = null;
    try {
      exchangeCode = ExchangeCode.valueOf(exchangeId);
    } catch (IllegalArgumentException e) {
      System.err.println("Invalid exchange code \"" + exchangeId + "\" provided.");
      System.err.flush();
      System.exit(1);
    }

    Properties props = new Properties();
    InputStream is;
    try {
      is = Assignment.class.getClassLoader().getResourceAsStream(propertiesPath);
      if (is != null) {
        props.load(is);
      }
    } catch (IOException e) {
      System.err.println("FAILED to load properties from \"" + propertiesPath + "\"");
      System.err.println(e.getClass() + " - " + e.getMessage());
      e.printStackTrace(System.err);
      System.err.flush();
      System.exit(1);
    }

    ExchangeModule module = new ExchangeModule(exchangeCode, props);
    Injector injector = Guice.createInjector(module);
    Assignment assignment = injector.getInstance(Assignment.class);

    // need to create appProprties instance with the properties that we've just loaded...

    int exitStatus = 0;
    try {
      assignment.trade();
      assignment.report();
      System.out.flush();
    } catch (Throwable e) {
      System.err.println("-------< TRADING STOPPED >-------");
      System.err.println(e.getClass().getName() + " - " + e.getMessage());
      e.printStackTrace(System.err);
      System.err.flush();
      System.err.println("-------< TRADING STOPPED >-------");
      System.err.flush();
      exitStatus = 1;
    }
    System.exit(exitStatus);
  }

  @Inject
  public Assignment(
      @Named("exchangeCode") ExchangeCode exchangeCode,
      StockExchange stockExchange,
      @Named("currentBrokerage") int currentBrokerage
  ) {
    this.exchangeCode = exchangeCode;
    this.stockExchange = stockExchange;
    this.currentBrokerage = currentBrokerage;
  }

  public void trade() {
    List<Trade> playbook = conjureUpRandomPlaybook(this.exchangeCode, 20, 20);
    for (Trade trade : playbook) {
      switch(trade.getTransactionType()) {
        case BUY:
          try {
            stockExchange.buy(trade.getCompanyCode().name(), trade.getQuantity());
          } catch (InsufficentUnitsException e) {
            System.err.println("WARN - " + e.getMessage());
            System.err.flush();
          } catch (IOException e) {
            System.err.println("ERROR - System failure caused buy to be refused " + trade);
            System.err.println(e.getClass() + " - " + e.getMessage());
            e.printStackTrace(System.err);
            System.err.flush();
          }
          break;
        case SELL:
          stockExchange.sell(trade.getCompanyCode().name(), trade.getQuantity());
          break;
      }
    }
  }

  public void report() {
    BigDecimal tradingCosts;
    try {
      tradingCosts = stockExchange.getTradingCosts();
      System.out.println("--------< "+ exchangeCode + ": Trading Report START >--------------");
      System.out.println(String.format("Total brokerage charged $%9.2f", tradingCosts.doubleValue()));
      System.out.flush();
    } catch (IOException wtf) {
      System.err.println("ERROR - Failed to calculate trading costs");
      System.err.println(wtf.getClass() + " - " + wtf.getMessage());
      wtf.printStackTrace(System.err);
    }
    Map<String, Integer> stockVolumeMap;
    try {
      stockVolumeMap = stockExchange.getOrderBookTotalVolume();
      System.out.println("Stock volumes");
      for(Map.Entry<String, Integer> entry : stockVolumeMap.entrySet()) {
        String code = entry.getKey();
        int volume = entry.getValue();
        System.out.println(String.format("%s : %d units", code, volume));
        System.out.flush();
      }
      System.out.println("--------< " + exchangeCode + ": Trading Report END >--------------");
      System.out.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public List<Trade> conjureUpRandomPlaybook(
      final ExchangeCode exchangeCode,
      final int numberBuys,
      final int numberSells
  ) {
    final List<Trade> result;
    int remainingSells = numberSells;
    int remainingBuys = numberBuys;
    result = new ArrayList<>();
    do {
      double raw = Math.random();
      boolean coin = raw <= 0.50D;
      TransactionType transactionType = coin ? BUY : SELL;
      if (transactionType == SELL && remainingSells < 1) {
        continue;
      } else {
        remainingSells--;
      }
      if (transactionType == BUY && remainingBuys < 1) {
        continue;
      } else {
        remainingBuys--;
      }
      int companyCodeIndex = (int)Math.floor(Math.random() * 2.999);
      int qty = (int)Math.ceil(Math.random() * 100.0D);
      CompanyCode companyCode = COMPANY_CODES[companyCodeIndex];
      Trade trade = new Trade(exchangeCode, transactionType, new Date(System.currentTimeMillis()), companyCode, qty, null);
      result.add(trade);
    } while(remainingBuys > 0 && remainingSells > 0);
    return result;
  }

}
