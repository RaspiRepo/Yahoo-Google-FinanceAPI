/*----------------------------------------------------------------------------
    stock_database.java 
                   : Class get stock symbol from databse and generate 
                     end of the day records.

    Written by     : RaspiRepo
    Date           : Aug 30, 2016
------------------------------------------------------------------------------*/




//package googlefinance;

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Random;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
 

import java.io.*;
import java.util.*;




public class stock_database
/*----------------------------------------------------------------------------
    stock_database.java 
                   : Class get stock symbol from databse and generate 
                     end of the day records.

    Written by     : RaspiRepo
    Date           : Aug 30, 2016
------------------------------------------------------------------------------*/
{

   
    private String connect_str   = "jdbc:mysql://localhost:3306/" + market_const.database_name
                                    + "?user=" + market_const.dbuser_name 
                                    + "&password=" + market_const.dbpsw;

    private String interday_price_table = null;
    private String interday_rsi_table   = null;
    private String history_table        = null;
    private String dividend_table       = null;


    private PreparedStatement curr_day_prep  = null;
    private Connection        stock_db_conn  = null;
    private PreparedStatement his_prep_stmnt = null;

    //for debug write
    private debug_log log = new debug_log();


    /*
    this query will return multiple table symbols for given sector.
    example if you want to see all finance sectors stocks which is trading amex, nyse and nasdaq.
    SELECT DISTINCT  nyse.rec_id, nyse.symbol, amex.sector, nyse.sector 
    FROM amex, nyse where nyse.sector = amex.sector and amex.sector = 3
    ****************************************/


/*

CREATE TABLE `stock_automate`.`Dividend_Yield` ( `rec_id` INT UNSIGNED NOT NULL , `StockSymbol` VARCHAR(8) NOT NULL ,
   `DateTime` DATETIME NOT NULL , `Dividend` DOUBLE NOT NULL , `Yield` DOUBLE NOT NULL , 
   PRIMARY KEY (`rec_id`(4))) ENGINE = InnoDB COMMENT = 'Store Stock symbol Dividend';
*/

    public void connect_database ()
    /*------------------------------------------------------------------------
        connect_database    : Connect to stock_automate database
        
        Written by     : RaspiRepo
        Date           : Aug 30, 2016
    ------------------------------------------------------------------------*/
    {
        try {

            stock_db_conn = DriverManager.getConnection(connect_str);
            stock_db_conn.setAutoCommit(false);
            //System.out.println("Database : " + connect_str);

        } catch (SQLException ex) {
            // handle any errors
            log.write("SQLException: " + ex.getMessage());
            log.write("SQLState: " + ex.getSQLState());
            log.write("VendorError: " + ex.getErrorCode());
        }
    }



    public void disconnect_database ()
    /*------------------------------------------------------------------------
        disconnect_database
                       : disconnect from stock_automate database
        
        Written by     : RaspiRepo
        Date           : Aug 30, 2016
    ------------------------------------------------------------------------*/
    {
        try {
            stock_db_conn.close();
            log.close();
        } catch (SQLException ex) {
        }
    }





    public void check_create_exchange_live_table (String xchange_name)
    /*------------------------------------------------------------------------
        check_create_exchange_table
                        : Check if table name created or not
                          not created then create the table for that day
                          NOTE: this should be done only week days ignore 
                          if day is weekend of month
                              
        
        Written by     : RaspiRepo
        Date           : Aug 30, 2016
    ------------------------------------------------------------------------*/
    {
        try {
            //stock_db_conn

        	DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
        	
        	String todays_date = dateFormat.format(new Date());
        	interday_price_table  = todays_date + "_" + xchange_name + "_stocks";
        	
        	String sql_query = "CREATE TABLE IF NOT EXISTS " + market_const.database_name + "." + interday_price_table + " ("
        			          + "`rec_id` int(10) unsigned NOT NULL AUTO_INCREMENT,"
        			          + "`symbol` varchar(12) NOT NULL DEFAULT '',"
        			          + "`date` TIMESTAMP NOT NULL,"
        			          + "`prev_close` double NOT NULL DEFAULT '0',"
        			          + "`last_traded` double NOT NULL DEFAULT '0',"
        			          + "`change_value` double NOT NULL DEFAULT '0',"
        			          + "`change_per` double NOT NULL DEFAULT '0',"
        			          + "`volume` bigint(20) unsigned NOT NULL DEFAULT '0',"
                              + "`rsi` double NOT NULL DEFAULT '0',"
                              + "`prev_avg_gain` double NOT NULL DEFAULT '0',"
                              + "`prev_avg_loss` double NOT NULL DEFAULT '0',"
        			          + "PRIMARY KEY (`rec_id`), UNIQUE KEY `Index_2` (`symbol`,`date`)"
        			          + ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;";
            
        	//CREATE TABLE `stock_automate`.`test` ( `recid` INT NOT NULL , `name` VARCHAR NOT NULL , `date` DATETIME NOT NULL , `upts` TIMESTAMP NOT NULL ) ENGINE = InnoDB;
        	Statement stmt = stock_db_conn.createStatement();
            //System.out.println(sql_query);

            stmt.execute(sql_query);
            System.out.println(interday_price_table + " Created"); 

        } catch (Exception ex) {
           System.out.println("check_create_exchange_live_table " + interday_price_table + ex.getMessage());
        }
    }

    

    public void check_create_exchange_rsi_table (String xchange_name)
    /*------------------------------------------------------------------------
        check_create_exchange_rsi_table
                        : Daily table for RSI calculation for all symbols
                                      
        Written by     : RaspiRepo
        Date           : Aug 30, 2016
    ------------------------------------------------------------------------*/
    {
        try {
            //stock_db_conn

        	DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
        	
        	String todays_date = dateFormat.format(new Date());
            //todays_date = "2016_09_30";
            
        	interday_rsi_table  = todays_date + "_" + xchange_name + "_rsi_temp";
        	if (interday_price_table == null) {
                interday_price_table  = todays_date + "_" + xchange_name + "_stocks";
            }

        	
        	String sql_query = "CREATE TABLE IF NOT EXISTS " + market_const.database_name + "." + interday_rsi_table + " ("
        			          + "`rec_id` int(10) unsigned NOT NULL AUTO_INCREMENT,"
        			          + "`symbol` varchar(12) NOT NULL DEFAULT '',"
        			          + "`date` time NOT NULL,"
        			          + "`prev_close` double NOT NULL DEFAULT '0',"
        			          + "`last_traded` double NOT NULL DEFAULT '0',"
        			          + "`volume` int(10) NOT NULL DEFAULT '0',"
        			          + "`rs` double NOT NULL DEFAULT '0',"
                              + "`rsi` double NOT NULL DEFAULT '0',"
        			          + "`state` int(2) unsigned NOT NULL DEFAULT '0',"
        			          + "PRIMARY KEY (`rec_id`), UNIQUE KEY `Index_2` (`symbol`,`date`)"
        			          + ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;";
        	
        	//Statement stmt = stock_db_conn.createStatement();

            //stmt.execute(sql_query);
            //System.out.println(interday_rsi_table + " Created");

        } catch (Exception ex) {
           System.out.println("check_create_exchange_rsi_table " + interday_rsi_table + ":"  + ex.getMessage());
        }
    }



    public void check_create_dividend_table (String xchange_name)
    /*------------------------------------------------------------------------
        check_create_exchange_table
                        : Check if table name created or not
                          not created then create the table for that day
                          NOTE: this should be done only week days ignore 
                          if day is weekend of month
                              
        
        Written by     : RaspiRepo
        Date           : Aug 30, 2016
    ------------------------------------------------------------------------*/
    {
        try {
            dividend_table = xchange_name + "_div_Yield";

            //stock_db_conn
        	String sql_query = "CREATE TABLE IF NOT EXISTS " + market_const.database_name + "." + dividend_table + " ("
        			          + "`rec_id` int(10) unsigned NOT NULL AUTO_INCREMENT,"
        			          + "`symbol` varchar(12) NOT NULL DEFAULT '',"
        			          + "`date` timestamp NOT NULL,"
        			          + "`Dividend` double NOT NULL DEFAULT '0',"
        			          + "`Yield` double NOT NULL DEFAULT '0',"
        			          + "PRIMARY KEY (`rec_id`), UNIQUE KEY `Index_2` (`symbol`,`date`)"
        			          + ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;";
        	
        	Statement stmt = stock_db_conn.createStatement();
            //System.out.println(sql_query); 

            stmt.execute(sql_query);
            //System.out.println(table_name + " Created"); 

        } catch (Exception ex) {
           System.out.println("check_create_dividend_table: " + dividend_table + ex.getMessage());
        }
    }

    
    public void check_create_history_table (String xchange_name)
    /*------------------------------------------------------------------------
        check_create_history_table
                        : Check if table name created or not
                          not created then create the table for that day
                          NOTE: this should be done only week days ignore 
                          if day is weekend of month
                              
        Written by     : RaspiRepo
        Date           : Aug 30, 2016
    ------------------------------------------------------------------------*/
    {
        try {
            history_table = xchange_name + "_daily_history";

            //stock_db_conn
        	String sql_query = "CREATE TABLE IF NOT EXISTS " + market_const.database_name 
                              + "." + history_table + " ("
        			          + "`rec_id` int(10) unsigned NOT NULL AUTO_INCREMENT,"
        			          + "`symbol` varchar(12) NOT NULL DEFAULT '',"
        			          + "`date` DATE NOT NULL,"
        			          + "`open` double NOT NULL DEFAULT '0',"
        			          + "`high` double NOT NULL DEFAULT '0',"
        			          + "`low` double NOT NULL DEFAULT '0',"
        			          + "`close` double NOT NULL DEFAULT '0',"
        			          + "`volume` long NOT NULL,"
        			          + "PRIMARY KEY (`rec_id`), UNIQUE KEY `Index_2` (`symbol`,`date`)"
        			          + ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;";
        	
        	Statement stmt = stock_db_conn.createStatement();
            stmt.execute(sql_query);

        } catch (Exception ex) {
           System.out.println("Query Exception : " + history_table + ex.getMessage());
        }
    }




    public ArrayList get_market_xchange_list ()
    /*------------------------------------------------------------------------
        get_market_xchange_list
                       : Function return all exchnage names from table
        
        Written by     : RaspiRepo
        Date           : Aug 30, 2016
    ------------------------------------------------------------------------*/
    {
        ArrayList<String> xchange_list = new ArrayList<String>();

        ResultSet rs = null;
        try {
            Statement st = stock_db_conn.createStatement();

            //execute the query, and get a java resultset
            rs = st.executeQuery("select name from market_center;");
            while (rs.next()) {
                xchange_list.add(rs.getString("name"));
            }

            rs = null;
            st = null;
        } catch (SQLException ex) {
            // handle any errors
           log.write("SQLException: get_market_xchange_list:" + ex.getMessage());
        }
        return xchange_list;
    }






    public ArrayList get_market_symbols_list (String xchange_name)
    /*------------------------------------------------------------------------
        get_market_symbols_list
                       : Function return all exchnage names from table
        
        Written by     : RaspiRepo
        Date           : Aug 30, 2016
    ------------------------------------------------------------------------*/
    {
        ArrayList<String> symbols_list = new ArrayList<String>();

        ResultSet rs = null;
        try {

            log.open(xchange_name + "_stock_db_log.txt");

            Statement st = stock_db_conn.createStatement();

            //execute the query, and get a java resultset
            rs = st.executeQuery("select symbol from " + xchange_name + ";");
            while (rs.next()) {
                symbols_list.add(rs.getString("symbol"));
            }

            rs = null;
            st = null;
        } catch (SQLException ex) {
           log.write("SQLException: get_market_symbols_list:" + ex.getMessage());
        }
        return symbols_list;
    }





    public symbol_div_yld_info get_recent_dividend (String sym_name)
    /*------------------------------------------------------------------------
        get_recent_dividend   
                       : Get dividend and yield information 

        Written by     : RaspiRepo
        Date           : Aug 30, 2016
    ------------------------------------------------------------------------*/
    {
        symbol_div_yld_info div_yld_info = null;

        ResultSet rs = null;
        try {

            Statement st = stock_db_conn.createStatement();
            String query = "select Dividend, Yield from " + dividend_table 
                            + " where symbol = '" + sym_name 
                            + "';";

            rs = st.executeQuery(query);
            if (rs.next()) {
                div_yld_info = new symbol_div_yld_info();
                div_yld_info.yield = rs.getLong("Dividend");
                div_yld_info.yield = rs.getLong("Yield");
            }
            //stock_db_conn.setAutoCommit(true);

            rs = null;
            st = null;

        } catch (SQLException ex) {
           log.write("SQLException: " + ex.getMessage());
        }
        return div_yld_info;
    }






    /****Marked for delete ***/
    private double get_recent_price (String xchange_name,
                                    String sym_name)
    /*------------------------------------------------------------------------
        get_recent_price   
                       : Get last traded price which is updated recently, it could be
                         previous day highest price

        Written by     : RaspiRepo
        Date           : Aug 30, 2016
    ------------------------------------------------------------------------*/
    {
        double last_traded = 0;

        ResultSet rs = null;
        try {
            Statement st = stock_db_conn.createStatement();
            String query = "select last_traded from " + interday_price_table 
                                 + " where symbol = '" + sym_name + "' and date > CURDATE() group by date desc limit 1;";

            rs = st.executeQuery(query);

            //SELECT *, max(rec_id)  FROM nasdaq_curr_day_report WHERE symbol = 'aapl' and date > (SELECT SUBDATE(NOW(), INTERVAL 5 minute)) group by date desc
            //execute the query, and get a java resultset
            if (rs.next()) {
                last_traded = rs.getDouble("last_traded");
            }

            rs = null;
            st = null;
            query = null;

        } catch (SQLException ex) {
           log.write("SQLException: get_recent_price:" + ex.getMessage());
        }
        return last_traded;
    }



    /****Marked for delete ***/
    private long get_recent_volume (String xchange_name,
                                   String sym_name)
    /*------------------------------------------------------------------------
        get_recent_volume   
                       : Get volume which is updated recently, it could be
                         previous day highest price

        Written by     : RaspiRepo
        Date           : Aug 30, 2016
    ------------------------------------------------------------------------*/
    {
        long prev_volume = 0;

        ResultSet rs = null;
        try {
            Statement st = stock_db_conn.createStatement();

            String query = "select volume from " + interday_price_table 
                                 + " where symbol = '" + sym_name + "' and date > CURDATE() group by date desc limit 1;";

            rs = st.executeQuery(query);

            //SELECT *, max(rec_id)  FROM nasdaq_curr_day_report WHERE symbol = 'aapl'
            //and date > (SELECT SUBDATE(NOW(), INTERVAL 5 minute)) group by date desc
            //execute the query, and get a java resultset
            if (rs.next()) {
                prev_volume = rs.getLong("volume");
            }

            rs = null;
            st = null;
            query = null;

        } catch (SQLException ex) {
           log.write("SQLException: get_recent_volume:" + ex.getMessage());
        }
        return prev_volume;
    }




    public double get_recent_rsi (String sym_name)
    /*------------------------------------------------------------------------
        get_recent_price   
                       : Get volume which is updated recently, it could be
                         previous day highest price

        Written by     : RaspiRepo
        Date           : Aug 30, 2016
    ------------------------------------------------------------------------*/
    {
        double last_traded = 0;

        ResultSet rs = null;
        try {
            Statement st = stock_db_conn.createStatement();
            String query = "select rsi from " + interday_rsi_table 
                                 + " where symbol = '" + sym_name + "' and \
                            date > CURDATE() group by date desc limit 1;";

            rs = st.executeQuery(query);
            if (rs.next()) {
                last_traded = rs.getDouble("rsi");
            }

            rs = null;
            st = null;
            query = null;

        } catch (SQLException ex) {
           log.write("SQLException: get_recent_rsi:" + ex.getMessage());
        }
        return last_traded;
    }





    public void prepare_curr_day_report (String xchange_name)
    /*------------------------------------------------------------------------
        prepare_curr_day_report
                       : This function create prepatre statement
                         to insert records into batch mode.
        
        Written by     : RaspiRepo
        Date           : Aug 30, 2016
    ------------------------------------------------------------------------*/
    {
        String query = "insert IGNORE into " + interday_price_table 
                    + " (rec_id, symbol, date, prev_close, last_traded, change_value, \
                    change_per, volume, rsi, prev_avg_gain, prev_avg_loss) "
                    + " values (?,?,?,?,?,?,?,?,?,?,?);";
        try {
            //if any pending update complete it first before create new prepare statement
            complete_curr_day_report();

            //prepate bacth insert qurty for current day report
            curr_day_prep = stock_db_conn.prepareStatement(query);
            query = null;

        } catch (SQLException ex) {
            log.write("SQLException : prepare_curr_day_report: "+ ex.getMessage()); 
        }
    }


    public void complete_curr_day_report ()
    /*------------------------------------------------------------------------
        complete_curr_day_report
                       : complete the batch mode insert
        
        Written by     : RaspiRepo
        Date           : Aug 30, 2016
    ------------------------------------------------------------------------*/
    {
        try {
            //commit and close
            if (curr_day_prep != null) {
                curr_day_prep.executeBatch();
                stock_db_conn.commit();
                curr_day_prep.close();
            }
        } catch (SQLException ex) {
            log.write("SQLException : "+ ex.getMessage()); 
            System.out.print("complete_curr_day_report: " + ex.getMessage());
        }

        curr_day_prep = null;
    }




    public void commit_curr_day_report ()
    /*------------------------------------------------------------------------
        commit_curr_day_report
                       : commit the batch mode insert
        
        Written by     : RaspiRepo
        Date           : Aug 30, 2016
    ------------------------------------------------------------------------*/
    {
        try {
            //comit the battch update
            if (curr_day_prep != null) {
                curr_day_prep.executeBatch();
                stock_db_conn.commit();
            }
        } catch (SQLException ex) {
            log.write("SQLException : "+ ex.getMessage()); 
        }
    }



    public void update_realtime_values (symbol_curr_info symbol_info)
    /*------------------------------------------------------------------------
        update_realtime_values
                       : add symbols real time market value
        
        Written by     : RaspiRepo
        Date           : Aug 30, 2016
    ------------------------------------------------------------------------*/
    {
        try {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            curr_day_prep.setInt(1, 0); //rec_id
            curr_day_prep.setString(2, symbol_info.symbol); //symbol
            curr_day_prep.setTimestamp(3, timestamp); //date
            curr_day_prep.setDouble(4, symbol_info.prev_close); //pervclose
            curr_day_prep.setDouble(5, symbol_info.curr_price); //last_traded
            curr_day_prep.setDouble(6, symbol_info.change);  //change 
            curr_day_prep.setDouble(7, symbol_info.per_change); //perchange
            curr_day_prep.setLong(8, symbol_info.volume);
            curr_day_prep.setDouble(9, symbol_info.rsi);
            curr_day_prep.setDouble(10, symbol_info.prev_avg_gain);
            curr_day_prep.setDouble(11, symbol_info.prev_avg_loss);
            curr_day_prep.addBatch();

        } catch (SQLException ex) {
            log.write("SQLException : "+ ex.getMessage()); 
            System.out.print("update_realtime_values Exception : " + ex.getMessage());

        }
    }



    public void prepare_history_report (String xchange_name)
    /*------------------------------------------------------------------------
        prepare_history_report
                       : This function create prepatre statement
                         to insert records into batch mode.
        
        Written by     : RaspiRepo
        Date           : Aug 30, 2016
    ------------------------------------------------------------------------*/
    {
        String query = "insert IGNORE into " + xchange_name + "_daily_history" 
                    + " (rec_id, symbol, date, open, high, low, close, volume) "
                    + " values (?,?,?,?,?,?,?,?);";
        try {
            stock_db_conn.setAutoCommit(false);

            //prepate bacth insert qurty for current day report
            his_prep_stmnt = stock_db_conn.prepareStatement(query);

            //log.write(query);
            //System.out.print("prepare_history_report " + query);

        } catch (SQLException ex) {
            log.write("SQLException : " + ex.getMessage());
        }
    }


    public void complete_history_report ()
    /*------------------------------------------------------------------------
        complete_history_report
                       : complete the batch mode insert
        
        Written by     : RaspiRepo
        Date           : Aug 30, 2016
    ------------------------------------------------------------------------*/
    {
        try {
            //commit and close
            if (his_prep_stmnt != null) {
                his_prep_stmnt.executeBatch();
                stock_db_conn.commit();
                his_prep_stmnt.close();
            }
        } catch (SQLException ex) {
            log.write("complete_history_report : "+ ex.getMessage());
            System.out.print("complete_history_report " + ex.getMessage());
        }

        his_prep_stmnt = null;
    }



    public void update_history_values (symbol_history_info symbol_info)
    /*------------------------------------------------------------------------
        update_history_values
                       : add symbols historical value
        
        Written by     : RaspiRepo
        Date           : Aug 30, 2016
    ------------------------------------------------------------------------*/
    {
        try {
            //Date trade_date = dayformat.parse(symbol_info.date);

            //Calendar hist_date = Calendar.getInstance();
            //hist_date.set(year, month, day);

            his_prep_stmnt.setInt(1, 0); //rec_id
            his_prep_stmnt.setString(2, symbol_info.symbol); 
            his_prep_stmnt.setDate(3, java.sql.Date.valueOf(symbol_info.date)); 
            his_prep_stmnt.setDouble(4, symbol_info.open); 
            his_prep_stmnt.setDouble(5, symbol_info.high); 
            his_prep_stmnt.setDouble(6, symbol_info.low);   
            his_prep_stmnt.setDouble(7, symbol_info.close); 
            his_prep_stmnt.setLong(8, symbol_info.volume);
            
            //write to debug
            String s = String.format("%-8s: %s\t%4.2f\t%4.2f\t%4.2f\t%4.2f\t%d",
                                     symbol_info.symbol, symbol_info.date, symbol_info.open,
                                     symbol_info.high, symbol_info.low, symbol_info.close, symbol_info.volume);
            log.write(s);
            //System.out.println(s);

            his_prep_stmnt.addBatch();

        } catch (SQLException ex) {
            log.write("update_history_values: SQLException : "+ ex.getMessage());
            System.out.print("update_history_values" + ex.getMessage());

        }
    }



    public void commit_curr_hist_bathch ()
    /*------------------------------------------------------------------------
        commit_curr_hist_bathch
                       : commit the batch mode insert
        
        Written by     : RaspiRepo
        Date           : Aug 30, 2016
    ------------------------------------------------------------------------*/
    {
        try {
            //comit the battch update
            if (his_prep_stmnt != null) {
                his_prep_stmnt.executeBatch();
                stock_db_conn.commit();
            }
        } catch (SQLException ex) {
            log.write("SQLException : "+ ex.getMessage()); 
        }
    }




    public void update_dividend_info (symbol_div_yld_info curr_divyld)
    /*------------------------------------------------------------------------
        get_recent_price   
                       : Get volume which is updated recently, it could be
                         previous day highest price

        Written by     : RaspiRepo
        Date           : Aug 30, 2016
    ------------------------------------------------------------------------*/
    {
        double last_traded = 0;

        try {
            Statement st = stock_db_conn.createStatement();
            String query = "INSERT INTO " + dividend_table + " (symbol, Dividend, Yield, date)"
                           + " values ('" + curr_divyld.symbol + "',"
                           + curr_divyld.dividend + "," + curr_divyld.yield + ",CURRENT_TIMESTAMP);";

//            String query = "update Dividend_Yield set StockSymbol = '" + sym_name + "', Dividend = " + divd_value 
//                          + ", Yield = " + yld + " where StockSymbol = '" + sym_name +"';";

            //System.out.println(query);

            st.executeUpdate(query);
            st = null;

        } catch (SQLException ex) {
           log.write("SQLException: " + ex.getMessage());
        }
    }



    public symbol_curr_info get_recent_trade_details (String xchange_name,
                                                      symbol_curr_info trade_info)
    /*------------------------------------------------------------------------
        get_recent_price   
                       : Get last traded price which is updated recently, it could be
                         previous day highest price

        Written by     : RaspiRepo
        Date           : Aug 30, 2016
    ------------------------------------------------------------------------*/
    {
        ResultSet rs = null;
        try {
            Statement st = stock_db_conn.createStatement();

            String query = "SELECT count(*) as count FROM " + interday_price_table + " where symbol = '" + trade_info.symbol + "'";
            rs = st.executeQuery(query);
            if (rs.next()) {
                trade_info.samples_count = rs.getInt("count");
            }
            if (trade_info.samples_count > 0) {
                query = "select * from " + interday_price_table 
                                     + " where symbol = '" + trade_info.symbol + "' and date > CURDATE() group by date desc limit 1;";

                rs = st.executeQuery(query);

                //SELECT *, max(rec_id)  FROM nasdaq_curr_day_report WHERE symbol = 'aapl' and date > (SELECT SUBDATE(NOW(), INTERVAL 5 minute)) group by date desc
                //execute the query, and get a java resultset
                if (rs.next()) {
                    trade_info.curr_price = rs.getDouble("last_traded");
                    trade_info.prev_close = rs.getDouble("prev_close");
                    trade_info.change = rs.getDouble("change_value");
                    trade_info.per_change = rs.getDouble("change_per");
                    trade_info.volume = rs.getLong("volume");
                    trade_info.rsi = rs.getDouble("rsi");
                    trade_info.prev_avg_gain = rs.getDouble("prev_avg_gain");
                    trade_info.prev_avg_loss = rs.getDouble("prev_avg_loss");
                    if (trade_info.rsi > 0.0) {
                        trade_info.first_rsi_flag = true;
                    }
                }
            }

            rs = null;
            st = null;
            query = null;

        } catch (SQLException ex) {
           log.write("SQLException: get_recent_trade_details:" + ex.getMessage());
        }
        return trade_info;
    }




    public double[] get_recent_avarage_gain_loss (String symbol_name)
    /*------------------------------------------------------------------------
     get_avarage_gain_loss
                    : Return most recent updated values of stcok, i.e always most recent 14)
     
     Written by     : RaspiRepo
     Date           : Aug 30, 2016
     ------------------------------------------------------------------------*/
    {
        double sum_gain = 0;
        double sum_loss = 0;
        double curr_price = 0;
        
        double rsi = 0;
        double prev_price = 0;
        double gain_loss  = 0;
        
        double avg_gl[] = {0,0}; //new double[2];;
        int num_sampes = 1;

        ResultSet rs = null;
        try {
            
            Statement st = stock_db_conn.createStatement();
            //String sql = "SELECT last_traded from " + interday_price_table + " WHERE symbol = '" + symbol
            //                + "' order by date asc limit 14";

            String sql = "SELECT last_traded FROM (SELECT symbol, date as cu_date, date, last_traded from "
                         + interday_price_table + " WHERE symbol = '" + symbol_name + "' order by cu_date desc limit 14) as tmp order by cu_date";

            //execute the query, and get a java resultset
            rs = st.executeQuery(sql);
            rs.next();
            prev_price = (double)rs.getDouble("last_traded");
            
            System.out.println(prev_price);

            //top 14 samples gian/loss sum
            while (rs.next()) {
                curr_price = (double)rs.getDouble("last_traded");
                System.out.println(prev_price);

                gain_loss  = curr_price - prev_price;
                
                if (gain_loss > 0.0) {
                    sum_gain += gain_loss;
                } else {
                    sum_loss += -gain_loss;
                }
                prev_price = curr_price;
                num_sampes = num_sampes + 1;
            }
            avg_gl[0] = (sum_gain / num_sampes);
            avg_gl[1] = (sum_loss / num_sampes);
            System.out.println(num_sampes + " avg G " + avg_gl[0] + " avg L " + avg_gl[1]);

            rs = null;
            st = null;
        } catch (SQLException ex) {
            log.write("SQLException: get_recent_avarage_gain_loss sql error :" + ex.getMessage());
            System.out.println("SQLException: get_recent_avarage_gain_loss sql error :" + ex.getMessage());
        }

        return avg_gl;
    }   



    public void commit_records ()
    /*------------------------------------------------------------------------
        commit_records : complete record update
        
        Written by     : RaspiRepo
        Date           : Aug 30, 2016
    ------------------------------------------------------------------------*/
    {
        try {
            stock_db_conn.commit();
        } catch (SQLException ex) {
        }
    }
    
    
    public ArrayList get_most_active_top_symbols (String xchange_name)
    /*------------------------------------------------------------------------
     get_most_active_top_symbols
                    : Return stock symbols and its price, volume information
     
     Written by     : RaspiRepo
     Date           : Aug 30, 2016
     ------------------------------------------------------------------------*/
    {
        
        //top 100 volume price range 5 to 20$
        //SELECT DISTINCT symbol, date, close, volume, format((open-close), 2) as gian_loss  FROM `nyse_daily_history` where close > 5.0 && close < 20.0 order by date desc, volume desc limit 100
        //SELECT DISTINCT symbol, date, close, volume, format((open-close), 2) as gian_loss FROM `nyse_daily_history` where close > 5.0 && close < 20.0 order by date desc, volume desc limit 100
        double min_price = 5.0;
        double max_price = 140.0;
        
       
        ArrayList<symbol_history_info> recent_price_list = new ArrayList<symbol_history_info>();
        symbol_history_info sym_info = new symbol_history_info ();
        
        ResultSet rs = null;
        try {
            
            Statement st = stock_db_conn.createStatement();
            
            String sql = "SELECT DISTINCT symbol, date, open, close, volume, format((open-close), 2) as gian_loss  FROM "
                        + xchange_name + "_daily_history where close > " + min_price + " && close <= " + max_price + " order by date desc, volume desc limit 100";
            
            //System.out.println(sql);
            
            //execute the query, and get a java resultset
            rs = st.executeQuery(sql);
            while (rs.next()) {
                sym_info = new symbol_history_info ();
                sym_info.symbol = rs.getString("symbol");
                sym_info.date   = rs.getString("date");
                sym_info.open   = rs.getDouble("open");
                sym_info.close  = rs.getDouble("close");
                sym_info.volume = rs.getLong("volume");
                //System.out.println(sym_info.symbol + " " + rs.getDouble("gian_loss"));
                recent_price_list.add(sym_info);
            }
            
            rs = null;
            st = null;
            sym_info = null;
        } catch (SQLException ex) {
            log.write("SQLException: get_most_active_top_symbols sql error :" + ex.getMessage());
            System.out.println("SQLException: get_most_active_top_symbols sql error :" + ex.getMessage());
        }
        return recent_price_list;

    }
    
    
    public void update_rsi_to_price_table (String symbol)
    /*------------------------------------------------------------------------
     get_symbols_price_value
                    : Return most recent updated values of stcok, i.e always top 15)
     
     Written by     : RaspiRepo
     Date           : Aug 30, 2016
     ------------------------------------------------------------------------*/
    {
        double sum_gain = 0;
        double sum_loss = 0;
        double curr_price = 0;
        double rsi = 0;
        double prev_price = 0;
        double gain_loss  = 0;
        double new_loss   = 0;
        double new_gain   = 0;
        
        int sample_index = 0;
        
        ResultSet rs = null;
        try {
            
            Statement st = stock_db_conn.createStatement();
            String sql = "SELECT rec_id, last_traded from " + interday_price_table + " WHERE symbol = '" + symbol
            + "' order by date asc";
            
            //execute the query, and get a java resultset
            rs = st.executeQuery(sql);
            rs.next();
            prev_price = (double)rs.getDouble("last_traded");
            
            stock_db_conn.setAutoCommit(true);
            
            //top 14 samples gian/loss sum
            while (rs.next() && ++sample_index < 14) {
                curr_price = (double)rs.getDouble("last_traded");
                gain_loss  = curr_price - prev_price;
                
                if (gain_loss > 0.0) {
                    sum_gain += gain_loss;
                } else {
                    sum_loss += -gain_loss;
                }
                prev_price = curr_price;
            }
            double prev_avg_gain = sum_gain / 14;
            double prev_avg_loss = sum_loss / 14;
            
            if (prev_avg_loss == 0.0) {
                rsi = 100.0;
            } else {
                rsi = 100.0 - (100.0 / ( 1.0 + (prev_avg_gain / prev_avg_loss)));
            }
            
            
            long rec_id = rs.getLong("rec_id");
            
            update_recent_rsi(symbol, rsi, rec_id);
            log.write(symbol + " " + rec_id + " " + rsi);
            
            while (rs.next()) {
                curr_price = (double)rs.getDouble("last_traded");
                gain_loss  = curr_price - prev_price;
                
                if (gain_loss < 0.0) {
                    new_loss = -gain_loss;
                } else {
                    new_gain = gain_loss;
                }
                
                prev_avg_gain =  ((prev_avg_gain * 13.0 + new_gain) / 14.0);
                prev_avg_loss =  ((prev_avg_loss * 13.0 + new_loss) / 14.0);
                
                if (prev_avg_loss == 0.0) {
                    rsi = 100.0;
                } else {
                    rsi = 100.0 - (100.0 / ( 1.0 + (prev_avg_gain / prev_avg_loss)));
                }
                
                //update rsi into table
                rec_id = rs.getLong("rec_id");
                update_recent_rsi(symbol,  rsi, rec_id);
                log.write(symbol + " " + rec_id + " " + rsi);
                prev_price = curr_price;
            }
            
            rs = null;
            st = null;
            sql = null;
        } catch (SQLException ex) {
            log.write("SQLException: update_rsi_calculation sql error :  " + symbol + " " + ex.getMessage());
            System.out.println("SQLException: update_rsi_calculation sql error :" + symbol + " " + ex.getMessage());
        }
    }
    
    
    private void update_recent_rsi (String symbol,
                                    double rsi_factor,
                                    long    rec_id)
    /*------------------------------------------------------------------------
     udpate_recent_rsi
                     : Get volume which is updated recently, it could be
                        previous day highest price
     
     Written by     : RaspiRepo
     Date           : Aug 30, 2016
     ------------------------------------------------------------------------*/
    {
        String query ="";
        //update `2016_09_30_nyse_stocks` set rsi = 0.0 where rec_id = 375
        try {
            Statement st = stock_db_conn.createStatement();
            query = "UPDATE " + interday_price_table + " set rsi = " + rsi_factor
            + " where rec_id = " + rec_id;
            
            //log.write(query);
            st.executeUpdate(query);
            st = null;
            query = null;
        } catch (SQLException ex) {
            log.write("SQLException: update_recent_rsi:" + ex.getMessage());
            log.write(query);
            System.out.println("SQLException: update_recent_rsi:" + ex.getMessage());
            
        }
    }
    
    
    
    
    private double calculate_recent_rsi (double curr_price,
                                         double prev_price,
                                         double prev_avg_gain,
                                         double prev_avg_loss)
    /*------------------------------------------------------------------------
     calculate_recent_rsi
                    : Calculate RSI using previous loss/gain and current price
     
     Written by     : RaspiRepo
     Date           : Aug 30, 2016
     ------------------------------------------------------------------------*/
    {
        double rsi      = 0;
        double new_loss = 0.0;
        double new_gain = 0.0;
        
        double gain_loss  = curr_price - prev_price;
        
        if (gain_loss < 0.0) {
            new_loss = -gain_loss;
        } else {
            new_gain = gain_loss;
        }
        
        prev_avg_gain =  ((prev_avg_gain * 13.0 + new_gain) / 14.0);
        prev_avg_loss =  ((prev_avg_loss * 13.0 + new_loss) / 14.0);
        
        if (prev_avg_loss == 0.0) {
            rsi = 100.0;
        } else {
            rsi = 100.0 - (100.0 / ( 1.0 + (prev_avg_gain / prev_avg_loss)));
        }
        
        return rsi;
    }
    
    
}