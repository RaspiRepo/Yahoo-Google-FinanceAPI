/*----------------------------------------------------------------------------
COPYRIGHT (c) 2014, RaspiRepo,
Mounatin View, California, USA.

ALL RIGHTS RESERVED.
-----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------
    stock_realtime.java : This class will get real time information of all symbols
                          and insert into database.

    Written By          : RaspiRepo
    Address             : Mountain View, CA 94040

    Date                : September 24, 2014

    Copyright (c) 2014-Present.
    All Rights Reserved.
------------------------------------------------------------------------------*/


/******************************************************************
v0 - volume
x0 - stock exchange
p0 - previous close
o0 - open - not getting values
l1 - last traded value
d0 - Annual Dividend Yield - not getting value
c6 - change in real time - when market opens otherwise "N/A"
k2 - change percentage in real time
p2 - change in percentage
c1 - change 
c8 - after hour change

End of day format: p0l1c1p2v0
prv_close:last_tade:change:change_per:volume:


multiple stocks retrival
http://finance.yahoo.com/d/quotes.csv?s=AAPL+GOOG+MSFT&f=sb2b3jk
http://download.finance.yahoo.com/d/quotes.csv?s="aapl"&f=sp0l1c1p2v0&e=.csv
http://finance.yahoo.com/d/quotes.csv?s=AAPL&f=v
http://download.finance.yahoo.com/d/quotes.csv?s="aapl"&f=v0&e=.csv

******************************************************************/

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Hashtable;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import org.json.*;
import java.util.concurrent.TimeUnit.*;
import java.util.StringTokenizer;
import java.util.Random;



public class stock_realtime implements Runnable
/*----------------------------------------------------------------------------
    stock_realtime.java : This class will get real time information of all 
                          symbols and insert into database.

    Written By          : RaspiRepo
    Address             : Mountain View, CA 94040

    Date                : September 24, 2014

    Copyright (c) 2014-Present.
    All Rights Reserved.
------------------------------------------------------------------------------*/
{
    //for debug write
    private debug_log log    = new debug_log();
    private debug_log rs_log = new debug_log();
    private debug_log hs_log = new debug_log();
    
    private HttpURLConnection   connection    = null;
    private OutputStreamWriter  wr            = null;
    private BufferedReader      rd            = null;
    private URL                 serverAddress = null;
    private InputStreamReader   io_stream_read = null;

    private Thread              thread_proces = null; 

    private stock_database mrkt_realtime_db = new stock_database();

    
    private String            exchange_name   = null;
    private ArrayList<String> market_symbols  = null;
    private ListIterator      lst_itr         = null;
    private String            sym_realtime_info = "";

    private Hashtable         symbols_trade_volume = new Hashtable();
    private Hashtable         symbols_prv_trade = new Hashtable();

    private Hashtable         symbols_trade_list = new Hashtable();



    private ArrayList<String> req_symbols_record = new ArrayList<String>();;

    private Timestamp         timestamp          = null;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    //private String curr_date_str         = "";
    private PrintWriter symbol_data_file = null;

    private int max_symbols_per_request  = 100;
    private int total_num_stocks_changed = 0;

    private StringBuffer json_report = new StringBuffer("");

    private StringTokenizer st = null;
    private String          event_log_msg = null;

    
    public stock_realtime ()
    /*------------------------------------------------------------------------
     stock_realtime      : Connect to stock_automate database
     
     Written By          : RaspiRepo
     Address             : Mountain View, CA 94040

     Date                : September 29, 2014
     
     Copyright (c) 2014-Present.
     All Rights Reserved.
     ------------------------------------------------------------------------*/
    {
       

    }
    
    



    public void start_realtime_update ()
    /*------------------------------------------------------------------------
        start_realtime_update
                            : Create thread process to get list of symbols
                              and request server to get real time stock values
                              and insert into database table. This process
                              continiue every 5 seconds
        
        Written By          : RaspiRepo
        Address             : Mountain View, CA 94040

        Date                : September 29, 2014

        Copyright (c) 2014-Present.
        All Rights Reserved.
    ------------------------------------------------------------------------*/
    {
        if (thread_proces == null) {
            // Creates the thread instance and starts the thread.
            thread_proces = new Thread(this);
            thread_proces.start();
        }
    }




    public void stop_realtime_update ()
    /*------------------------------------------------------------------------
        stop_realtime_update
                            : Create thread process to get list of symbols
                              and request server to get real time stock values
                              and insert into database table. This process
                              continiue every 5 seconds
        
        Written By          : RaspiRepo
        Address             : Mountain View, CA 94040

        Date                : September 29, 2014

        Copyright (c) 2014-Present.
        All Rights Reserved.
    ------------------------------------------------------------------------*/
    {
        if (thread_proces != null) {
            thread_proces.stop();
            thread_proces = null;
        }
    }


    
    
    public void init_stock_exchange (String exchangename)
    /*------------------------------------------------------------------------
         init_stock_exchange : Initilize exchange name and invoke
                               mysql connection
         
         Written By          : RaspiRepo
         Address             : Mountain View, CA 94040

         Date                : September 29, 2014
         
         Copyright (c) 2014-Present.
         All Rights Reserved.
     ------------------------------------------------------------------------*/
    {
        exchange_name = exchangename;
        
        //create and open database connection
        mrkt_realtime_db.connect_database();
    }
    
    
    
    private stock_realtime (String exchangename)
    /*------------------------------------------------------------------------
         set_stock_exchange  : Connect to stock_automate database
         
         Written By          : RaspiRepo
         Address             : Mountain View, CA 94040

         Date                : September 29, 2014
         
         Copyright (c) 2014-Present.
         All Rights Reserved.
     ------------------------------------------------------------------------*/
    {
        exchange_name = exchangename;
        
        //create and open database connection
        mrkt_realtime_db.connect_database();
        
    }
    
    
    
    public void init_stock_exchange_live ()
    /*------------------------------------------------------------------------
        init_stock_exchange_live
                            : create current date table to store stock value 

        Written By          : RaspiRepo
        Address             : Mountain View, CA 94040

        Date                : September 29, 2014

        Copyright (c) 2014-Present.
        All Rights Reserved.
    ------------------------------------------------------------------------*/
    {
        timestamp = new Timestamp(System.currentTimeMillis());

        //open debug log file
        log.open(timestamp + "_" + exchange_name + "_realtime_log.txt");

        //open debug log file
        //rs_log.open(timestamp + "_" + exchange_name + "_rsi_log.txt");

        // interday table
        mrkt_realtime_db.check_create_exchange_live_table(exchange_name);

        //create current day RSI table 
        mrkt_realtime_db.check_create_exchange_rsi_table (exchange_name);
    }


    public void init_stock_exchange_rsi_test ()
    /*------------------------------------------------------------------------
        init_stock_exchange_rsi_test
                            : RSI calculation test function
        
        Written By          : RaspiRepo
        Address             : Mountain View, CA 94040

        Date                : September 29, 2014

        Copyright (c) 2014-Present.
        All Rights Reserved.
    ------------------------------------------------------------------------*/
    {
        timestamp = new Timestamp(System.currentTimeMillis());

        //open debug log file
        rs_log.open(timestamp + "_" + exchange_name + "_rsi_log.txt");

        //create current day RSI table 
        //mrkt_realtime_db.check_create_exchange_rsi_table (exchange_name);
    }




    /******hisotry update functions *******/
    public void init_history ()
    /*------------------------------------------------------------------------
        init_history        : create history record table
        
        Written By          : RaspiRepo
        Address             : Mountain View, CA 94040

        Date                : September 29, 2014

        Copyright (c) 2014-Present.
        All Rights Reserved.
    ------------------------------------------------------------------------*/
    {
        //open debug log file
        log.open(exchange_name + "_history.txt");

        //create history table if not created already
        mrkt_realtime_db.check_create_history_table(exchange_name);
    }



/*    

googleFinanceKeyToFullName = {
    u'id'     : u'ID',
    u't'      : u'StockSymbol',
    u'e'      : u'Index',
    u'l'      : u'LastTradePrice',
    u'l_cur'  : u'LastTradeWithCurrency',
    u'ltt'    : u'LastTradeTime',
    u'lt_dts' : u'LastTradeDateTime',
    u'lt'     : u'LastTradeDateTimeLong',
    u'div'    : u'Dividend',
    u'yld'    : u'Yield',
    u's'      : u'LastTradeSize',
    u'c'      : u'Change',
    u'cp'      : u'ChangePercent',
    u'el'     : u'ExtHrsLastTradePrice',
    u'el_cur' : u'ExtHrsLastTradeWithCurrency',
    u'elt'    : u'ExtHrsLastTradeDateTimeLong',
    u'ec'     : u'ExtHrsChange',
    u'ecp'    : u'ExtHrsChangePercent',
    u'pcls_fix': u'PreviousClosePrice'
}

*/


     public int start_history_update (int start_y, 
                                      int start_m, 
                                      int start_d,
                                      int end_y, 
                                      int end_m, 
                                      int end_d)
    /*------------------------------------------------------------------------
        start_history_update
                            : thread process to pull the records from server 
                              to update rela time stock values
        
        Written By          : RaspiRepo
        Address             : Mountain View, CA 94040

        Date                : September 29, 2014

        Copyright (c) 2014-Present.
        All Rights Reserved.
    ------------------------------------------------------------------------*/
    {
        int num_rec_changed = 0;
        int retval = 0;

        String sym_record = null;

        try {
            //http://chart.finance.yahoo.com/table.csv?s=VMW&a=8&b=4&c=2015&d=8&e=4&f=2016&g=d&ignore=.csv
            hs_log.open("history.txt");
            
            //get all symbol names from database table
            market_symbols = mrkt_realtime_db.get_market_symbols_list(exchange_name);
            ListIterator symbol_list_itr = market_symbols.listIterator();

            //for each market table find its symbols and then 
            //get market report, insert into endofday report table
            while(symbol_list_itr.hasNext()) {

                //get symbol name
                String symbol_name = (String)symbol_list_itr.next();

                //build reqst string  
                String req_range = String.format("%s&a=%d&b=%d&c=%d&d=%d&e=%d&f=%d",symbol_name,
                                   start_m, start_d, start_y, end_m, end_d, end_y);
                
                //prepare database opertation (batch insert to improve speed
                mrkt_realtime_db.prepare_history_report(exchange_name);

                try {
                    //hs_log.write(req_range);

                    //skip the header
                    request_historical_report_y(req_range);
                    sym_record = read_history_records();

                    while ((sym_record = read_history_records()) != null) {

                        parse_update_history_yahoo(symbol_name, sym_record);
                        num_rec_changed++;
                    } 
                } catch (Exception e) {
                    System.out.println(symbol_name + " " + e.getMessage());
                     hs_log.write(symbol_name + " " + e.getMessage());
                }

                mrkt_realtime_db.complete_history_report();
                Thread.sleep(20);
            }
            System.out.println(exchange_name + "::history update completed " 
                     + " num_rec_changed " + num_rec_changed);
            hs_log.write(exchange_name + "::history update completed  num_rec_changed " +  num_rec_changed);

            close_server_connection();
        } catch (Exception e) {
            System.out.println("history update thread terminated ");
            e.printStackTrace();
        }

        //build top 100 active list and send email
        build_send_most_active_list(exchange_name);

        //close database conneciton
        mrkt_realtime_db.disconnect_database();

        return num_rec_changed;
    }
   
    
    
    
    private void request_historical_report_y (String symbol_req)
    /*------------------------------------------------------------------------
        request_historical_report_y
                            : get historical report from yahoo
        
        Written By          : RaspiRepo
        Address             : Mountain View, CA 94040

        Date                : September 29, 2014

        Copyright (c) 2014-Present.
        All Rights Reserved.
    ------------------------------------------------------------------------*/
    {
        try {

            //http://chart.finance.yahoo.com/table.csv?s=VMW&a=8&b=4&c=2015&d=8&e=4&f=2016&g=d&ignore=.csv
            serverAddress = new URL("http://chart.finance.yahoo.com/table.csv?s="
                                  + symbol_req + "&g=d&ignore=.csv");
            connection = (HttpURLConnection)serverAddress.openConnection();

            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setReadTimeout(10000);
            connection.connect();
            rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));

          } catch (MalformedURLException e) {
          } catch (ProtocolException e) {
          } catch (IOException e) {
          }
    }



    private void request_realtime_report_y (String symbol_list)
    /*------------------------------------------------------------------------
        request_realtime_report
                            : Connect to stock_automate database
        
        Written By          : RaspiRepo
        Address             : Mountain View, CA 94040

        Date                : September 29, 2014

        Copyright (c) 2014-Present.
        All Rights Reserved.
    ------------------------------------------------------------------------*/
    {
        try {
            //serverAddress = new URL("http://download.finance.yahoo.com/d/quotes.csv?s="
            //                      + symbol_list + "&f=&e=.csv");

            serverAddress = new URL("http://finance.yahoo.com/d/quotes.csv?s="
                                  + symbol_list + "&f=spl1c1p2v&e=.csv");

            connection = (HttpURLConnection)serverAddress.openConnection();

            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setReadTimeout(5000);
            connection.connect();

            rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
          } catch (MalformedURLException e) {
          } catch (ProtocolException e) {
          } catch (IOException e) {
          }
    }




    private void request_realtime_report_g (String symbol_list)
    /*------------------------------------------------------------------------
        request_realtime_report
                            : Connect to stock_automate database
        
        Written By          : RaspiRepo
        Address             : Mountain View, CA 94040

        Date                : September 29, 2014

        Copyright (c) 2014-Present.
        All Rights Reserved.
    ------------------------------------------------------------------------*/
    {
        try {
            serverAddress = new URL("https://finance.google.com/finance/info?client=ig&q="
                                  + symbol_list);
            connection = (HttpURLConnection)serverAddress.openConnection();

            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setReadTimeout(10000);
            connection.connect();

            io_stream_read = new InputStreamReader(connection.getInputStream());
          } catch (MalformedURLException e) {
          } catch (ProtocolException e) {
          } catch (IOException e) {
          }
    }



    private void close_server_connection ()
    /*------------------------------------------------------------------------
        close_server_connection
                            : close server connection
        
        Written By          : RaspiRepo
        Address             : Mountain View, CA 94040

        Date                : September 29, 2014

        Copyright (c) 2014-Present.
        All Rights Reserved.
    ------------------------------------------------------------------------*/
    {
        try {
          //close the connection, set all objects to null
          connection.disconnect();
          io_stream_read.close();
          rd         = null;
          connection = null;
          serverAddress = null;
          io_stream_read = null;
        } catch (Exception e) {
        }
    }




    private String read_history_records ()
    /*------------------------------------------------------------------------
        read_history_records
                            : This method read single line of records 
        
        Written By          : RaspiRepo
        Address             : Mountain View, CA 94040

        Date                : September 29, 2014

        Copyright (c) 2014-Present.
        All Rights Reserved.
    ------------------------------------------------------------------------*/
    {
        try {
            sym_realtime_info = "";

            sym_realtime_info = rd.readLine();
          } catch (NullPointerException e) {
            //System.out.println("history update:read::NullPointerException:" + e.getMessage());

          } catch (Exception e) {
            System.out.println("history update:read:Exception:" + e.getMessage());
          }

        return sym_realtime_info;
    }



    private String read_realtime_report_g ()
    /*------------------------------------------------------------------------
        request_realtime_report
                            : Connect to stock_automate database
        
        Written By          : RaspiRepo
        Address             : Mountain View, CA 94040

        Date                : September 29, 2014

        Copyright (c) 2014-Present.
        All Rights Reserved.
    ------------------------------------------------------------------------*/
    {
        try {
            int index = 0;
            sym_realtime_info = "";

            //clear up everything
            json_report.delete(0, json_report.length());

            //sym_realtime_info = rd.readLine();
            int data = io_stream_read.read();
            while(data != -1){
                //json_report.setCharAt(index, (char) data);
                sym_realtime_info += (char) data;
                data = io_stream_read.read();
                ++index;
            }
          } catch (NullPointerException e) {
          } catch (Exception e) {
          }

        return sym_realtime_info;
    }





    private String get_next_symbol_list (int num_symbols)
    /*------------------------------------------------------------------------
        get_next_symbol_list
                            : This function return list of symbols into 
                              single string 
        
        Written By          : RaspiRepo
        Address             : Mountain View, CA 94040

        Date                : September 29, 2014

        Copyright (c) 2014-Present.
        All Rights Reserved.
    ------------------------------------------------------------------------*/
    {
        int count = 0;

        String symbol_list = null;
        try {

            //for each market table find its symbols and then 
            //get market report, insert into endofday report table
            while(lst_itr.hasNext() && count < num_symbols) {
                if (symbol_list == null) {
                    symbol_list = (String)lst_itr.next();
                } else {
                    symbol_list += "," + (String)lst_itr.next();
                }
                ++count;
            }
        } catch (Exception e) {
        }

        return symbol_list;
    }

    


    //yahoo method
    private int parse_update_history_yahoo (String symbol_name,
                                             String symbol_record)
    /*------------------------------------------------------------------------
        parse_update_history_yahoo
                            : parse yahoo finance response json/csv records
        
        Written By          : RaspiRepo
        Address             : Mountain View, CA 94040

        Date                : September 29, 2014

        Copyright (c) 2014-Present.
        All Rights Reserved.
    ------------------------------------------------------------------------*/
    {
        int retval = 0;
        try {
            symbol_history_info symbol_history = new symbol_history_info();
            //hs_log.write(symbol_record);

            String[] tokens = symbol_record.split(",");
//
//            for (int i = 0; i < tokens.length; i++) {
//                System.out.print(tokens[i] + " ");
//            }
//            System.out.println();
//
            if (tokens.length > 4) {
                symbol_history.symbol  = symbol_name;
                symbol_history.date    = tokens[0];
                symbol_history.open    = Double.parseDouble(tokens[1]);
                symbol_history.high    = Double.parseDouble(tokens[2]);
                symbol_history.low     = Double.parseDouble(tokens[3]);
                symbol_history.close   = Double.parseDouble(tokens[4]);
                symbol_history.volume  = Long.parseLong(tokens[5]);

                //insert records into table
                mrkt_realtime_db.update_history_values(symbol_history);
            }
        } catch ( ArrayIndexOutOfBoundsException e) {
            //System.out.println("parse error " + ":" + e.getMessage());
            //log.write("parse error " + ":" + e.getMessage());
            retval = 2;

        } catch (NumberFormatException e) {
            //System.out.println("parse error " + ":" + e.getMessage());
            //log.write("parse error " + ":" + e.getMessage());
            retval = 1;
        } catch (Exception e) {
            //System.out.println("parse error " + ":" + e.getMessage());
            //log.write("parse error " + ":" + e.getMessage());
            retval = 3;
        }
        return retval;

    }


    private int build_symbol_list_from_table () //one time call
    /*------------------------------------------------------------------------
        build_symbol_list_from_table
                            : This function return list of symbols into 
                              single string

        Written By          : RaspiRepo
        Address             : Mountain View, CA 94040

        Date                : September 29, 2014

        Copyright (c) 2014-Present.
        All Rights Reserved.
    ------------------------------------------------------------------------*/
    {
        double curr_price = 0.0;
        int    retval      = 1;

        String symbol_name = null;
        String sym_list = "";

        long volume = 0;
        int count = 0;
        int record_index = 0;

        try {
            symbol_curr_info symbol_realtime = null;
            
            //get all symbol names from database table
            market_symbols = mrkt_realtime_db.get_market_symbols_list(exchange_name);

            ListIterator symbol_list_itr = market_symbols.listIterator();

            //for each market table find its symbols and then 
            //get market report, insert into endofday report table
            while(symbol_list_itr.hasNext()) {
                symbol_realtime = new symbol_curr_info();

                //get symbol name and get latest volume value.
                symbol_name = (String)symbol_list_itr.next();
                symbol_realtime.symbol      = symbol_name;
                
                //To most recent update value (first when server start)
                symbol_realtime = mrkt_realtime_db.get_recent_trade_details(exchange_name, symbol_realtime);
                symbols_trade_list.put(symbol_name, symbol_realtime);

                //build symbols list to request online data service
                if (count < max_symbols_per_request) {
                    if (sym_list.isEmpty()) {
                        sym_list = symbol_name;
                    } else {
                        sym_list += "," + symbol_name;
                    }
                    ++count;
                } else {
                    if (!sym_list.isEmpty()) {
                        //System.out.println(record_index + ":" + sym_list);
                        req_symbols_record.add(record_index, sym_list);
                        ++record_index;
                    }
                    count    = 0;
                    sym_list = "";
                }
            }

            //add last record symbols list
            if (!sym_list.isEmpty()) {
                //System.out.println(record_index + ":" + sym_list);
                req_symbols_record.add(record_index, sym_list);
            }
            count    = 0;
            sym_list = "";
            retval   = 0;
            symbol_name = null;
        } catch (Exception e) {
            System.out.println("build_symbol_list_from_table: " +  e.getMessage());
        }

        return retval;
    }
    




    private void parse_update_realtime_values_google (String symbol_record)
    /*------------------------------------------------------------------------
        parse_update_realtime_google
                            : add symbols real time market value
        
        Written By          : RaspiRepo
        Address             : Mountain View, CA 94040

        Date                : September 29, 2014

        Copyright (c) 2014-Present.
        All Rights Reserved.
    ------------------------------------------------------------------------*/
    {
        try {
            int num_updated_symbols = 0;

            //System.out.println(symbol_record);
            
            symbol_curr_info symbol_realtime = new symbol_curr_info();

            String str_j = symbol_record.replace("// ", "");
            log.write(symbol_record);

            //loop thru all json object list for each symbol 
            JSONArray stock_sym_list = new JSONArray(str_j);

            int index = 0;
            JSONObject stcok_obj = null;

            while (index < stock_sym_list.length()) {
                stcok_obj = new JSONObject(stock_sym_list.remove(index).toString());
                symbol_realtime.symbol = stcok_obj.getString("t");
                try {
                    symbol_realtime.curr_price = stcok_obj.getDouble("l_cur");
                    symbol_realtime.change = stcok_obj.getDouble("c");
                    symbol_realtime.prev_close = stcok_obj.getDouble("pcls_fix");
                    symbol_realtime.per_change = stcok_obj.getDouble("cp");
                    symbol_realtime.volume     = 0;
                    double prv_trade = symbol_realtime.curr_price;

                    //get last time updated volume counts
                    if (symbols_prv_trade.containsKey(symbol_realtime.symbol) == true) {
                        prv_trade = (double)symbols_prv_trade.get(symbol_realtime.symbol);
                    } else {
                        symbols_prv_trade.put(symbol_realtime.symbol, symbol_realtime.curr_price);
                    }

                   //if (prv_trade != symbol_realtime.curr_price) {
                        ++total_num_stocks_changed;
                        ++num_updated_symbols;

                        //recent volume change
                        symbol_realtime.volume = (long)symbols_trade_volume.get(symbol_realtime.symbol);

                        //insert records into table
                        mrkt_realtime_db.update_realtime_values(symbol_realtime);

                        symbols_prv_trade.put(symbol_realtime.symbol, symbol_realtime.curr_price);

                        //write to debug 
                        String s = String.format("%-8s: %4.2f\t%4.2f\t%4.2f\t%4.2f\t%d\tGoogle",
                                        symbol_realtime.symbol, symbol_realtime.prev_close, symbol_realtime.curr_price, 
                                        symbol_realtime.change, symbol_realtime.per_change, symbol_realtime.volume);
                        log.write(s);
                    //} 

                    //stcok_obj = null;

                } catch (Exception e) {
                    System.out.println("JSONObject Error :" + exchange_name + ":" + symbol_realtime.symbol +  " " + e.getMessage());
                    log.write("JSONObject Error :" + exchange_name + ":" + symbol_realtime.symbol +  " " + e.getMessage());
                }
                //stcok_obj = null;
                ++index;
            }

            //avoid duplicated value into database 
            if (num_updated_symbols > 0) {
                mrkt_realtime_db.commit_curr_day_report();
            }

            //stock_sym_list.clear();
            stcok_obj = null;
            stock_sym_list = null;
            symbol_realtime = null;
            str_j = null;

        } catch (Exception e) {
            System.out.println(" ERROR :" + e.getMessage());
            //log.write("google parse error :" + e.getMessage() + symbol_record);

        }
    }


/*** delete this ****/
     private String update_current_volume_y (String symbol_list)
    /*------------------------------------------------------------------------
        get_current_volume_y
                            : check volume and trade value difference list from yahoo

        Written By          : RaspiRepo
        Address             : Mountain View, CA 94040

        Date                : September 19, 2016

        Copyright (c) 2014-Present.
        All Rights Reserved.
    ------------------------------------------------------------------------*/
    {
        String valid_sym_list = "";
        long volume = 0;
        double y_trade_value = 0;
        try {
            URL serverAddr = new URL("http://finance.yahoo.com/d/quotes.csv?s="
                                  + symbol_list + "&f=sl1v&e=.csv");
            HttpURLConnection vol_connection = (HttpURLConnection)serverAddr.openConnection();

            vol_connection.setRequestMethod("GET");
            vol_connection.setDoOutput(true);
            vol_connection.setReadTimeout(5000);
            vol_connection.connect();

            BufferedReader vol_rd = new BufferedReader(new InputStreamReader(vol_connection.getInputStream()));
            String volume_str = "";

            String[] tokens = null;
            while ((volume_str = vol_rd.readLine()) != null) {
                try {
                    tokens = volume_str.split(",");
                    tokens[0] = tokens[0].replaceAll("\"", "");
                    y_trade_value = 0;
                    try {
                        y_trade_value = Double.parseDouble(tokens[1]);
                    } catch (Exception e) {
                    }

                    volume = 0;
                    if (tokens[1].compareToIgnoreCase("N/A") != 0) {
                        volume = Long.parseLong(tokens[2]);
                    }
                    
                    double prv_trade = (double)symbols_prv_trade.get(tokens[0]);

                    //update symbols recent volume
                    //if (y_trade_value != prv_trade || (long)symbols_trade_volume.get(tokens[0]) != volume) {
                        symbols_trade_volume.put(tokens[0], volume);

                        valid_sym_list += tokens[0] + ",";

                        String s = String.format("%-8s: %4.2f\t%4.2f\t%d\t%d\tYHOO",
                                        tokens[0], prv_trade, y_trade_value, 
                                        (long)symbols_trade_volume.get(tokens[0]), volume);
                        log.write(s);

                    //}
                } catch (Exception e) {
                    System.out.println("Get Volume from yahoo :: " + volume_str + " " + e.getMessage());
                }
            }

            //log.write(symbol_list);
            //log.write(valid_sym_list);

            //close the connection, set all objects to null
            vol_connection.disconnect();
            vol_rd         = null;
            vol_connection = null;
            tokens = null;
            volume_str = null;
            serverAddr = null;

        } catch (Exception e) {
            System.out.println("Get Volume from yahoo : ERROR " + e.getMessage());
        }

        return valid_sym_list;
    }

  private Random fRandom = new Random();
  
  public double RandomGaussian(double mean)
  {
    double VARIANCE = 5.0f;
    return (getGaussian(mean, VARIANCE));
  }
    
  
  private double getGaussian(double aMean, double aVariance){
    return aMean + fRandom.nextGaussian() * aVariance;
  }


    //yahoo method
    private int parse_update_realtime_values_yahoo (String symbol_record)
    /*------------------------------------------------------------------------
        get_realtime_values
                            : add symbols real time market value
        
        Written By          : RaspiRepo
        Address             : Mountain View, CA 94040

        Date                : September 29, 2014

        Copyright (c) 2014-Present.
        All Rights Reserved.
    ------------------------------------------------------------------------*/
    {
        int num_records = 0;

        String symbol_name   = "";
        double prev_close    = 0.0;
        double curr_price    = 0.0;
        double change        = 0.0;
        double change_per    = 0.0;
        long   volume        = 0;

        //for rsi calclulation
        double gain_loss     = 0;
        double new_loss   = 0.0;
        double new_gain   = 0.0;

        double[] avg_gl = null;
        String[] tokens = null;
        String per_change_str = "";

        symbol_curr_info trade_info = null;
        //"AAPL",113.0900,113.7638,+0.6738,"+0.5958%",15348028
        //sym_eof.per_change = (sym_eof.change / sym_eof.open ) * 100.0f;
        try {
            //symbol_record = symbol_record.trim();
            st = new StringTokenizer(symbol_record, ",");
            if (st.countTokens() > 4) {

                symbol_name = st.nextToken().replace("\"","");

                trade_info = (symbol_curr_info)symbols_trade_list.get(symbol_name);
               
                prev_close   = Double.parseDouble(st.nextToken());
                curr_price   = Double.parseDouble(st.nextToken());
                change       = Double.parseDouble(st.nextToken());
            
                //calculate percentage of change
                per_change_str = st.nextToken().replace("\"","");
                per_change_str = per_change_str.replace("%", "");
                change_per     = Double.parseDouble(per_change_str);
                volume         = Long.parseLong(st.nextToken());

                //curr_price = RandomGaussian(curr_price);

                if (trade_info != null && trade_info.curr_price != curr_price) {
                    gain_loss  = curr_price - trade_info.curr_price;

                    trade_info.prev_close = prev_close;
                    trade_info.curr_price = curr_price;
                    trade_info.change     = change;
                    trade_info.per_change = change_per;
                    trade_info.volume     = volume;

                    //if more than 14 samples already recorded then caluclate first RSI
                    //average gain, loss.
                    if (trade_info.first_rsi_flag == false && trade_info.samples_count >= 14) {

                        avg_gl = mrkt_realtime_db.get_recent_avarage_gain_loss(symbol_name);

                        trade_info.prev_avg_gain = avg_gl[0];
                        trade_info.prev_avg_loss = avg_gl[1];

                        if (trade_info.prev_avg_loss == 0.0) {
                            trade_info.rsi = 100.0;
                        } else {
                            trade_info.rsi = 100.0 - (100.0 / ( 1.0 + (trade_info.prev_avg_gain / trade_info.prev_avg_loss)));
                        }
                        trade_info.first_rsi_flag = true;

                    } else if (trade_info.samples_count > 15) {
                        if (gain_loss < 0.0) {
                            new_loss = -gain_loss;
                        } else {
                            new_gain = gain_loss;
                        }
                        trade_info.prev_avg_gain =  ((trade_info.prev_avg_gain * 13.0 + new_gain) / 14.0);
                        trade_info.prev_avg_loss =  ((trade_info.prev_avg_loss * 13.0 + new_loss) / 14.0);

                        if (trade_info.prev_avg_loss == 0.0) {
                            trade_info.rsi = 100.0;
                        } else {
                            trade_info.rsi = 100.0 - (100.0 / ( 1.0 + (trade_info.prev_avg_gain / trade_info.prev_avg_loss)));
                        }
                    }
                    
                    //insert records into table
                    mrkt_realtime_db.update_realtime_values(trade_info);

                    trade_info.samples_count = trade_info.samples_count + 1;
                    symbols_trade_list.put(symbol_name, trade_info);

//                    event_log_msg = trade_info.samples_count + " " + trade_info.symbol + "\t" + trade_info.prev_close  + "\t" + trade_info.curr_price  + "\t" 
//                                    + trade_info.change  + "\t" + trade_info.per_change  + "\t" + trade_info.volume + "\t" + trade_info.rsi
//                                    + "num_samples " + trade_info.samples_count + "\t" + trade_info.prev_avg_gain 
//                                    + "\t" + trade_info.prev_avg_loss 
//                                    + "\t" + trade_info.first_rsi_flag;
                    
                    //System.out.println(event_log_msg);
                    //log.write(event_log_msg);

                    num_records = 1;
                }
            }

            tokens = null;
            st     = null;
            trade_info = null;
        } catch (NullPointerException e) {
            //System.out.println("****** yahoo parse error ******" + symbol_record + " " + e.getMessage());
        } catch (Exception e) {
            //System.out.println("yahoo parse error " + symbol_record + " : " + e.getMessage());
            //log.write("yahoo parse error " + symbol_record + " : " + e.getMessage());
            //String del_qury = "delete from exchange_name where symbol = " + sym_name;
            //System.out.println(del_qury);

        }
        return num_records;
    }




     public void run ()
    /*------------------------------------------------------------------------
        run                 : thread process to pull the records from server 
                              to update rela time stock values
        
        Written By          : RaspiRepo
        Address             : Mountain View, CA 94040

        Date                : September 29, 2014

        Copyright (c) 2014-Present.
        All Rights Reserved.
    ------------------------------------------------------------------------*/
    {
        String sym_record = null;
        String sym_list   = "";
        int tot_num_records     = 0;
        int num_records_updated = 0;
        int count = 0;

        int num_of_updates = 0;

        try {

            //setuop database connection and build symbol list.
            build_symbol_list_from_table();

            mrkt_realtime_db.prepare_curr_day_report(exchange_name);
            total_num_stocks_changed = 0;

            //to optimized time and number or request google server
            //caluclate sleep per request based on number of symbols for exchange
            //every 2 minutes there may be vlaue change (2 minute interval)
            int num_req_need = market_symbols.size() / max_symbols_per_request;
            int sleep_secs = (120 / req_symbols_record.size());

            //System.out.println("sleep secs per request is " + sleep_secs + " " + req_symbols_record.size());
            //log.write("sleep secs per request is " + sleep_secs + " number request " + req_symbols_record.size());
            log.write(exchange_name + " update Process started ");

            while (thread_proces != null) {

                for (int i = 0; i < req_symbols_record.size(); i++) {
                    sym_list = req_symbols_record.get(i);
                    num_records_updated = 0;

                    if (!sym_list.isEmpty()) {
                        try {

                            request_realtime_report_y(sym_list);

                            sym_record = rd.readLine();

                            //read each symbol stcok information and insert into database table
                            while (sym_record != null) {
                                sym_record = rd.readLine();
                                if (sym_record != null) {
                                    num_records_updated += parse_update_realtime_values_yahoo(sym_record);
                                }
                            }
                            close_server_connection();
                        } catch (java.io.IOException e) {
                        }

                        //avoid duplicated value into database 
                        if (num_records_updated > 0) {
                            mrkt_realtime_db.commit_curr_day_report();
                            tot_num_records += num_records_updated;
                        }                    
                        sym_list          = "";
                        sym_record        = "";
                        Thread.sleep(200);
                    }
                }
                //first 15 iteration write db quickly
                if (num_of_updates < 15) {
                    mrkt_realtime_db.commit_curr_day_report();
                    System.out.println("commit_curr_day_report called " + num_of_updates);
                }

                System.out.println(exchange_name + " update completed " + ++num_of_updates
                         + " total_num_stocks_changed " + tot_num_records
                            + " num of symbols in volume list " + symbols_trade_volume.size());
                
                log.write(exchange_name + " update completed " + num_of_updates
                          + " total_num_stocks_changed " + tot_num_records
                          + " num of symbols in volume list " + symbols_trade_volume.size());

                tot_num_records = 0;
                Thread.sleep(200);
            }//endof while
            
        } catch (Exception e) {
            System.out.println("Realtime tracking thread terminated ");
            e.printStackTrace();
        }

        log.write("disconnect_database");

        //close database conneciton
        mrkt_realtime_db.disconnect_database();
    }




    public void build_send_most_active_list (String exchangename)
    /*------------------------------------------------------------------------
     build_send_most_active_list
                         : This method get top active stocks from history table 
                           and generate report and send as email.  This 
                           can be called every day when end of the day update
                           invoked
     
    Written By          : RaspiRepo
    Address             : Mountain View, CA 94040

     Date                : September 18, 2016
     
     Copyright (c) 2014-Present.
     All Rights Reserved.
     ------------------------------------------------------------------------*/
    {
        String gain_loss = "";
        String gain_loss_per = "";
        
        ArrayList most_active_list = null;
        
        symbol_history_info sym_info = null;
        
        HTMLTableBuilder htmlBuilder = null;
        
        int num_rows = 0;
        
        int num_cols = 7;
        int seq_num = 1;
        
        //prepare database opertation (batch insert to improve speed
        most_active_list = mrkt_realtime_db.get_most_active_top_symbols(exchangename);
        if (most_active_list != null && most_active_list.size() > 0) {
            num_rows = (most_active_list.size() + 1);
            
            notification_system notify = new notification_system ();
        
            htmlBuilder = new HTMLTableBuilder("Todays Most Active : " + exchangename, true, num_rows, num_cols);
            htmlBuilder.addTableHeader("S.No", "Symbol", "Open", "Close", "Gain/Loss", "G/L %", "Volume");
        
            for (int i = 0; i < most_active_list.size(); i++) {
                
                sym_info = (symbol_history_info)most_active_list.get(i);
                double gl = (sym_info.close - sym_info.open);
                
                gain_loss = String.format("%6.2f", gl);
                gain_loss_per = String.format("%6.2f", (gl / sym_info.close) * 100.0);
                
                htmlBuilder.addRowValues(""+seq_num, sym_info.symbol, String.format("%4.2f", sym_info.open),
                        String.format("%4.2f", sym_info.close), gain_loss, gain_loss_per, ""+sym_info.volume);
                //System.out.println(seq_num + " " + sym_info.symbol + " " + gain_loss + " " + gain_loss_per);
                
                ++seq_num;
            }
            
            //build report ans end to email
            String table = htmlBuilder.build();
            //System.out.println(table.toString());
            
            notify.send_email(market_const.from_email, market_const.to, "Todays Most Active :" + exchangename, table.toString());
        }
    }
    
  
    
    
   
    
     private void caluclate_exchange_todays_rsi_t ()
    /*------------------------------------------------------------------------
        caluclate_exchange_todays_rsi
                            : Get each symbol full day trade value
                              calculate Average gain/loss for first 14 samples and
                              15th sample for RSI factor.
                              Update all latest RSI value into RSI table
     
        Written By          : RaspiRepo
        Address             : Mountain View, CA 94040

        Date                : September 13, 2016

        Copyright (c) 2014-Present.
        All Rights Reserved.
    ------------------------------------------------------------------------*/
    {
        String    symbol_name      = "";

        try {
            mrkt_realtime_db.check_create_exchange_rsi_table(exchange_name);
            market_symbols = mrkt_realtime_db.get_market_symbols_list(exchange_name);

            System.out.println("RSI " + market_symbols.size());

            //get exchange symbol list
            ListIterator symbol_list_itr = market_symbols.listIterator();

            //for each symbol get recent 15 records
            while(symbol_list_itr.hasNext()) {

                //get symbol name and get last  dividend value.
                symbol_name = (String)symbol_list_itr.next();
     //symbol_list_itr.next();
                //get latest price details for given symbol
                //mrkt_realtime_db.update_rsi_calculation(symbol_name);
                //mrkt_realtime_db.update_rsi_to_price_table(symbol_name);

                symbol_name = "";
                //break;
                
            }//endof while all symbols rsi caluclation updated

            System.out.println(exchange_name + ":: Today RSI update completed");
            rs_log.write(exchange_name + ":: Today RSI update completed");
            symbol_list_itr = null;
            
        } catch (Exception e) {
            System.out.println("RSI update terminated " + e.getMessage());
        }
    }


    
    public static void main(String[] args)
    /*------------------------------------------------------------------------
        main                : TStandalone test code for this class
        
        Written By          : RaspiRepo
        Address             : Mountain View, CA 94040

        Date                : September 25, 2014

        Copyright (c) 2014-Present.
        All Rights Reserved.
    ------------------------------------------------------------------------*/
    {
        /*stock_realtime nasdaq_rsi = new stock_realtime();
        //nasdaq_rsi.init_stock_exchange ("nasdaq");
        //nasdaq_rsi.init_stock_exchange_rsi_test ();

        //nasdaq_rsi.caluclate_exchange_todays_rsi_t();

        stock_realtime nyse_rsi = new stock_realtime();
        nyse_rsi.init_stock_exchange ("nyse");
        nyse_rsi.init_stock_exchange_rsi_test ();
        nyse_rsi.caluclate_exchange_todays_rsi_t();
         */
    }

} //end of class
