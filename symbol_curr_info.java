/*----------------------------------------------------------------------------
    symbol_curr_info.java

    Written by     : RaspiRepo
    Date           : Aug 30, 2016
------------------------------------------------------------------------------*/

//package org.RaspiRepo.googlefinance;

public class symbol_curr_info {
    public String symbol       = "";
    public boolean first_rsi_flag = false;
    public int     samples_count = 0;
    public double  prev_close    = 0.0;
    public double  curr_price    = 0.0;
    public double  change        = 0.0;
    public double  per_change    = 0.0;
    public long    volume        = 0;
    public double  rsi           = 0.0;
    public double  prev_avg_gain = 0.0;
    public double  prev_avg_loss = 0.0;
    public double  dividend      = 0.0;
    public double  yield         = 0.0;
};
