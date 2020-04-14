# GuicyTrader
Programming Assignment

Instructions
- mvn dependency:copy-dependencies
- mvn package
- run.sh (takes an optional argument which is the exchange name)

Configuration
- the main/src/resources/some.properties file specifies the attributes that can be altered.
- if one wishes to update the brokerage charges, they can be updated in this properties file.

Assumptions
- stock exchanges can change prices, but this only takes affect after programme starts running.
- it is not possible to buy units of a company, if a sufficent quantity has not yet been sold into the market.
- the journal.csv file grows ever larger, it takes  manual intervention (e.g.; rm journal.csv) to remove it.

