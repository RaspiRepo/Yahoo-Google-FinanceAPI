# Yahoo/Google Finance data API.

To build mysql databse + tracking current tarading value of (NYSE/NASDAQ) symbols, RSI 

#Java, Mysql, PHP, Restful API, Json.

#Summary
  This project created to download Live data from Stock Exchange (NASDAQ/NYSE) for all Stock Symbols using Yahoo/Google Finanace API. Its developed for personal use generate alter notification for your watch list of stock symbols, determine buy/sell based on RSI (Relative Strength index - http://www.investopedia.com/terms/r/rsi.asp).

Every week days during Market open session a schedule process initiate downloading all Stock Symbols real time values and store into local MYSQL database.

Addional reports like can be generated Top Gainers/Loosers, Create alter notification for symbols when price match given threshold, using RSI index once can determine when overbought/over sold for given day, last 14 days RSI 


#Example URLs

http://finance.yahoo.com/d/quotes.csv?s=AAPL+GOOG+MSFT&f=sb2b3jk

http://download.finance.yahoo.com/d/quotes.csv?s="aapl"&f=sp0l1c1p2v0&e=.csv

http://finance.yahoo.com/d/quotes.csv?s=AAPL&f=v

http://download.finance.yahoo.com/d/quotes.csv?s="aapl"&f=v0&e=.csv

Google Finance
http://finance.google.com/finance/info?client=ig&q=aapl,goog,msft,fb

#Steps and Requirements

  1. Before using this software, need to create mysql database on local/remote system. Follow any online reference to setup MYSQL database and Java mySQL connector.

  2. Export required database, table records using  "stock_automate.sql".  There are many tools avilable to copy this information into your own database.

  3. Modify "market_const.java" according to local database name, username, password, and email address.  Email address is used to send notification or daily activity. Some of the features commented out by default. Enable according to need.

  4. Once stcoket market database created and inserted all data from "stock_automate.sql" compile and run "stock_schedule_main.java"

  5. To build Divident/Yeild table execute "stock_build_div_yld.java"
  
# Run
  1. Run "stock_schedule_main" Java class, it will check day of week and time, accordingly schedule the execution. As soon as time arrive (Week days/ morning 6:30 PST to 1:00 PM PST) start download Stocks trading values and inserted into datewise table for each exchange. 
NOTE: Daily table created dynamically in mysql as soon as market open.

  2. Data collection from Yahoo Finance API which is CSV/JSon format. This can be changed to Google Fianance API which gives Json format.  
  
  3. After collecting very first 16 trading value RSI_14(14 samples) calculated and stored into separate table.  This report can be viewed thru Apache/PHP script with google graph.
  
  
