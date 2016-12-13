# Yahoo/Google Finance data API.

To build mysql databse + track stock market (NYSE/NASDAQ) symbols, RSI 

#Summary
  This project created to download and store live stock market values from yahoo or google finance API. Its developed for personal use like generate alter notification of watch  list symbols when to buy/sell based on RSI (Relative Strength index - http://www.investopedia.com/terms/r/rsi.asp).

  Another usage of local database, write a query to determine top gainer/losser according to change %, value or volume. 

#Tools: 
Java, Mysql, PHP, Restful API, Json.

#Steps and Requirements

  1. Before using this software, need to create mysql database on local/remote system. Follow any online reference to setup mysql database or any other database.

  2. Export/Restore required database, table records using  "stock_automate.sql".  There are many tools avilable to copy this information into your own database.

  3. Modify "market_const.java" according to local database name, username, password, and email address.  Email address is used to send notification or daily activity. Some of the features commented out by default. Enable according to need.

  4. Once stcoket market database created and inserted all data from "stock_automate.sql" compile and run "stock_schedule_main.java"

#What will happen

  1. Once execute "stock_schedule_main" it will check day of week and time, accordingly schedule the future execution (Threading). As soon as time arrive (Week days and morning 6:30 PST) start request real time market value for each symbols inserted into NASDAQ, NYSE table records. 
  2. Data collection process Query Yahoo Finance API and get the recent market values and insert into local table. Tihs table is created dialy basis for all symbols per exchange.
  
  3. Once 14 Samples collected for any symbols, there is RSI calculation for each symbol and inserted into separate table.  This table can be used to identify over sold/over bought sitution for given symbol.
