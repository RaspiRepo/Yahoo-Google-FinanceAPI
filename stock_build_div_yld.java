/*----------------------------------------------------------------------------
COPYRIGHT (c) 2014, RaspiRepo,
Mounatin View, California, USA.

ALL RIGHTS RESERVED.
-----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------
    stock_build_div_yld.java 
                         : This class will get real time information
                           of all symbols and insert into database.

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

    public String symbol       = "";
    public float  prev_close   = 0.0f;
    public float  last_traded  = 0.0f;
    public float  change       = 0.0f;
    public float  per_change   = 0.0f;
    public long   volume       = 0;

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





public class stock_build_div_yld implements Runnable
/*----------------------------------------------------------------------------
    stock_build_div_yld.java : This class will get real time information of all 
                          symbols and insert into database.

    Written By          : RaspiRepo
    Address             : Mountain View, CA 94040

    Date                : September 24, 2014

    Copyright (c) 2014-Present.
    All Rights Reserved.
------------------------------------------------------------------------------*/
{
    //for debug write
    private debug_log log = new debug_log();

    private HttpURLConnection   connection    = null;
    private OutputStreamWriter  wr            = null;
    private BufferedReader      rd            = null;
    private URL                 serverAddress = null;
    private InputStreamReader   io_stream_read = null;

    private Thread              thread_proces = null; 


    private stock_database mrkt_realtime_db = new stock_database();

    private String            exchange_name   = "";
    private ArrayList<String> market_symbols  = null;
    private ListIterator      lst_itr         = null;

    private Hashtable         symbols_prv_change = new Hashtable();
    private Timestamp         timestamp          = null;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private String curr_date_str         = "";
    private PrintWriter symbol_data_file = null;


    int num_stocks_changed = 0;

    public void set_stock_exchange (String exchangename)
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

        timestamp = new Timestamp(System.currentTimeMillis());

        //dividend_tbl_name = exchange_name + "_div_Yield";

        //open debug log file
        log.open(timestamp + "_" + exchange_name + "div_log.txt");
        mrkt_realtime_db.check_create_dividend_table(exchange_name);
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
            serverAddress = new URL("http://download.finance.yahoo.com/d/quotes.csv?s="
                                  + symbol_list + "&f=&e=.csv");
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
            //System.out.println("https://finance.google.com " +  symbol_list);

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
          rd         = null;
          connection = null;
        } catch (Exception e) {
        }
    }




    private String read_realtime_report ()
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
        String sym_realtime_info = "";
        try {
            //sym_realtime_info = rd.readLine();
            int data = io_stream_read.read();
            while(data != -1){
                sym_realtime_info += (char) data;
                data = io_stream_read.read();
            }
            //System.out.println("read_realtime_report () " + sym_realtime_info);

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

    


    private int build_symbol_list_from_table ()
    /*------------------------------------------------------------------------
        build_symbol_list_from_table
                            : This function return list of symbols into 
                              hashtable and also if valus already
                              inserted into fixed information table like
                              preview value details retrived and stored
                              in hashtable to easy access
        
        Written By          : RaspiRepo
        Address             : Mountain View, CA 94040


        Copyright (c) 2014-Present.
        All Rights Reserved.
    ------------------------------------------------------------------------*/
    {
        symbol_div_yld_info   prev_change;
        int    retval      = 1;
        String symbol_name = null;

        try {

            //get all symbol names from database table
            market_symbols = mrkt_realtime_db.get_market_symbols_list(exchange_name);

            ListIterator symbol_list_itr = market_symbols.listIterator();

            //for each market table find its symbols and then 
            //get market report, insert into endofday report table
            while(symbol_list_itr.hasNext()) {

                //get symbol name and get last  dividend value.
                symbol_name = (String)symbol_list_itr.next();
                prev_change = (symbol_div_yld_info)mrkt_realtime_db.get_recent_dividend(symbol_name);
                if (prev_change != null) {
                    //keep previous dividend informarion into hashatable
                    symbols_prv_change.put(symbol_name, prev_change);
                }   
            }

            retval = 0;
        } catch (Exception e) {
        }

        return retval;
    }


    private void parse_update_dividend_values_google (String symbol_record)
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
            
            symbol_div_yld_info curr_divyld = new symbol_div_yld_info();
            symbol_div_yld_info prev_divyld = new symbol_div_yld_info();

            String str_j = symbol_record.replace("// ", "");

            //loop thru all json object list for each symbol 
            JSONArray stock_sym_list = new JSONArray(str_j);

            int index = 0;
            JSONObject stcok_obj = null;

            while (index < stock_sym_list.length()) {
                stcok_obj = new JSONObject(stock_sym_list.get(index).toString());
                curr_divyld.symbol = stcok_obj.getString("t");
                try {
                    curr_divyld.dividend = 0.0;
                    curr_divyld.yield    = 0.0;

                    //insert/update dividend stcok details
                    try {
                        if (stcok_obj.has("div") && !stcok_obj.getString("div").isEmpty()) {
                            curr_divyld.dividend = stcok_obj.getDouble("div");
                        }

                        if (stcok_obj.has("yld") && !stcok_obj.getString("yld").isEmpty()) {
                            curr_divyld.yield = stcok_obj.getDouble("yld");
                        }
                        //insert divident stock, one entry per stock
                    } catch (Exception e) {
                        System.out.println("Dividend parse error :" + curr_divyld.symbol +  " " + e.getMessage());
                        log.write("Dividend parse error :" + e.getMessage() + curr_divyld.symbol);
                    }

                    //check dividend stocks then update into database
                    if (curr_divyld.dividend > 0.0 && curr_divyld.yield > 0.0) {

                        //get last time updated dividend information counts
                        if (symbols_prv_change.containsKey(curr_divyld.symbol) == true) {
                            prev_divyld = (symbol_div_yld_info)symbols_prv_change.get(curr_divyld.symbol);
                        } else {
                            symbols_prv_change.put(curr_divyld.symbol, curr_divyld);
                        }

                        //previous information not available or value difference then 
                        //update into database
                        if (prev_divyld == null || prev_divyld.dividend != curr_divyld.dividend) {
                            ++num_stocks_changed;
                            //insert records into table
                            symbols_prv_change.put(curr_divyld.symbol, curr_divyld);
                            mrkt_realtime_db.update_dividend_info(curr_divyld);

                            //write to debug 
                            String s = String.format("%-8s: %4.2f\t%4.2f",
                                            curr_divyld.symbol, curr_divyld.dividend, curr_divyld.yield);

                            //System.out.println(s);
                            log.write(s);

                            try {
                                symbol_data_file.print(str_j);
                                symbol_data_file.flush();
                            } catch (Exception e) {

                            }
                        }
                    }

                } catch (Exception e) {
                    System.out.println("JSONObject Error :" + curr_divyld.symbol +  " " + e.getMessage() );
                    log.write("google parse error :" + e.getMessage() + symbol_record);

                }
                ++index;
            }

        } catch (Exception e) {
            System.out.println(" ERROR :" + e.getMessage());
            log.write("google parse error :" + e.getMessage() + symbol_record);

        }
    }





    //yahoo method
    private void parse_update_realtime_values_yahoo (String symbol_record)
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
        long prv_volume = 0;

        //"ESP",22.20,21.44,-0.76,"-3.42%",8255
        //sym:prv_close:last_tade:change:change_per:volume:
        //sym_eof.per_change = (sym_eof.change / sym_eof.open ) * 100.0f;

        try {
            symbol_curr_info symbol_realtime = new symbol_curr_info();

            String[] tokens = symbol_record.split(",");

            for (int i = 0; i < tokens.length; i++) {
                System.out.print(tokens[i] + " ");
            }

            symbol_realtime.symbol       = tokens[0].replace("\"","");
            symbol_realtime.prev_close   = Double.parseDouble(tokens[1]);
            symbol_realtime.last_traded  = Double.parseDouble(tokens[2]);
            symbol_realtime.change       = Double.parseDouble(tokens[3]);
            
            //calculate percentage of change
            String per_change = tokens[4].replace("%","");
            per_change        = per_change.replace("\"", "");

            //convert into number
            symbol_realtime.per_change = Double.parseDouble(per_change);

            symbol_realtime.volume = Long.parseLong(tokens[5]);

            //get last time updated volume counts
            if (symbols_prv_change.containsKey(symbol_realtime.symbol) == true) {
                prv_volume = (long)symbols_prv_change.get(symbol_realtime.symbol);
            } else {
                symbols_prv_change.put(symbol_realtime.symbol, symbol_realtime.volume);
            }

            if (prv_volume != symbol_realtime.volume) {
                //insert records into table
                mrkt_realtime_db.update_realtime_values(symbol_realtime);

                symbols_prv_change.put(symbol_realtime.symbol, symbol_realtime.volume);

                //write to debug 
                String s = String.format("%-8s: %4.2f\t%4.2f\t%4.2f\t%4.2f\t%d\t%d",
                                symbol_realtime.symbol, symbol_realtime.prev_close, symbol_realtime.last_traded, 
                                symbol_realtime.change, symbol_realtime.per_change, symbol_realtime.volume, prv_volume);

                //System.out.println(s);
                log.write(s);
            }

        } catch (Exception e) {
            System.out.println("parse error " + symbol_record + ":" + e.getMessage());
            log.write("parse error " + symbol_record + ":" + e.getMessage());
        }
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
        String sym_list   = null;

        try {
            try {
                symbol_data_file = new PrintWriter("stock_dividend.json", "UTF-8");
            } catch (IOException e) {
            }

            //setuop database connection and build symbol list.
            build_symbol_list_from_table();

            int counter = 0;
            num_stocks_changed = 0;

            while (thread_proces != null) {

                lst_itr = market_symbols.listIterator();

                while ((sym_list = get_next_symbol_list(20)) != null) {
                    //System.out.println(sym_list);

                    request_realtime_report_g(sym_list);
                    sym_record = read_realtime_report();
                    close_server_connection();

                    if(sym_record  != null) {
                        //parse and insert stock real time values
                        //into table
                        //parse_update_realtime_values_yahoo(sym_record);
                        parse_update_dividend_values_google(sym_record);
                    }
                    mrkt_realtime_db.commit_curr_day_report();
                    Thread.sleep(1000 * 5);
                }

                System.out.println("Dividend_Yield update completed " + ++counter 
                         + "num_stocks_changed " + num_stocks_changed);
                num_stocks_changed = 0;
                mrkt_realtime_db.commit_records();
                break;

            }//endof while
            
        try {
            symbol_data_file.close();
        } catch (Exception e) {
        }

        } catch (Exception e) {
            System.out.println("Realtime tracking thread terminated ");
            e.printStackTrace();

        }
        
        //close database conneciton
        mrkt_realtime_db.disconnect_database();
    }



    public static void main(String[] args)
    /*------------------------------------------------------------------------
        main                : Test code to build Stock symbols from file
        
        Written By          : RaspiRepo
        Address             : Mountain View, CA 94040

        Date                : September 25, 2014

        Copyright (c) 2014-Present.
        All Rights Reserved.
    ------------------------------------------------------------------------*/
    {
        try {
            System.out.println("**** Welcome to Stock market data collection");
            stock_build_div_yld market_nasdaq = new stock_build_div_yld();

            market_nasdaq.set_stock_exchange("nasdaq");
            market_nasdaq.start_realtime_update();


            stock_build_div_yld market_nyse = new stock_build_div_yld();

            market_nyse.set_stock_exchange("nyse");
            market_nyse.start_realtime_update();


        } catch (Exception e) {
        }   
    }   
}