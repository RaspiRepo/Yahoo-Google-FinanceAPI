/*----------------------------------------------------------------------------
COPYRIGHT (c) 2014, RaspiRepo,
Mounatin View, California, USA.

ALL RIGHTS RESERVED.
-----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------
    stock_schedule_main.java
                        : This main class which start market data download
                          process for NASDAQ and NYSE, update all live values 
                          to MYSQL database. 

                          NOTE: Before starting this class mysql Database, required
                          tables should be created

    Written By          : RaspiRepo
    Address             : Mountain View, CA 94040

    Date                : September 24, 2014

    Copyright (c) 2014-Present.
    All Rights Reserved.
------------------------------------------------------------------------------*/

public class stock_schedule_main 
/*----------------------------------------------------------------------------
    stock_schedule_main.java :
                          This main class which start market data download
                          process for NASDAQ and NYSE, update all live values 
                          to MYSQL database. 

    Written By          : RaspiRepo
    Address             : Mountain View, CA 94040

    Date                : September 24, 2014

    Copyright (c) 2014-Present.
    All Rights Reserved.
------------------------------------------------------------------------------*/
{

 
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
        stock_realtime_schedule schedule_trade = new stock_realtime_schedule();

        //schedule_trade.start_update_market();

        schedule_trade.schedule_market_open();
        schedule_trade.schedule_market_close();
        schedule_trade.schedule_end_of_day_update(0); //earlier date update 1, realtime update 0
    }
}
