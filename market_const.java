/*----------------------------------------------------------------------------
    market_const.java 
                   : debug log writting 

    Written by     : RaspiRepo
    Date           : Aug 30, 2016
------------------------------------------------------------------------------*/




//package org.RaspiRepo.googlefinance;

 

public class market_const
/*----------------------------------------------------------------------------
    maket_const    : define all constant

    Written by     : RaspiRepo
    Date           : Aug 30, 2016
------------------------------------------------------------------------------*/
{
    //email authentication
    public final static String smtp_server = "smtp.gmail.com";
    public final static int    smtp_port   = 465;
    public final static String username    = "youremail@gmail.com";
    public final static String password    = "your password";
    public final static String from_email  = "fromaddress@gmail.com";
    public final static String to          = "to@gmail.com";
    public final static String subject     = "Alert from StockManager";

    public final static String dbuser_name     = "stock_automate";
    public final static String dbpsw           = "mysql db password";
    public final static String database_name   = "stock_automate";

//    public final static String xch_div_name   = "_daily_history";
//    public final static String xch_his_name   = "_daily_history";

    public final static int MAX_NUM_RSI_SAMPLES = 16;
}