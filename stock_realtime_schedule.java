/*----------------------------------------------------------------------------
COPYRIGHT (c) 2014, RaspiRepo,
Mounatin View, California, USA.

ALL RIGHTS RESERVED.
-----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------
    stock_realtime_schedule.java
                        : This class will get real time information of all symbols
                          and insert into database. ITs scheduler class which start/stop
                          stcok market data collection process for week days and as per
                          market open duration only.

    Written By          : RaspiRepo
    Address             : Mountain View, CA 94040

    Date                : September 24, 2014

    Copyright (c) 2014-Present.
    All Rights Reserved.
------------------------------------------------------------------------------*/

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;





public class stock_realtime_schedule 
/*----------------------------------------------------------------------------
    stock_realtime_schedule.java :
                          This class will get real time information of all 
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

    stock_realtime nasdaq_realtime = new stock_realtime();
    stock_realtime nyse_realtime   = new stock_realtime();

    stock_realtime endofday_update = new stock_realtime();


    private notification_system notify = new notification_system ();


    Timer market_open_timer  = new Timer();
    Timer market_close_timer = new Timer();
    Timer endoday_timer      = new Timer();

    long next_time_check = 24 * 60 * 60 * 1000;

    
    private boolean market_update_flag = false;
    

    private void compose_send (String subject,
                               String message)
    /*------------------------------------------------------------------------
        compose_send   : Test code to build Stock symbols from file

        Written by     : RaspiRepo
        Date           : Aug 30, 2016
    ------------------------------------------------------------------------*/
    {
        notify.send_email(market_const.from_email, market_const.to, subject, message);
    }



    public stock_realtime_schedule ()
    /*------------------------------------------------------------------------
        compose_send   : Test code to build Stock symbols from file
        
        Written by     : RaspiRepo
        Date           : Aug 30, 2016
    ------------------------------------------------------------------------*/
    {
        log.open("schedule_times.txt");
    }



    abstract class task_reminder extends TimerTask
    {
        public abstract void doRun ();

        public void run ()
        /*-------------------------------------------------------------------------
            run                 : Just ensures that the task is executed on the
                                  event dispatching thread.   
                              
            Written By          : RaspiRepo
            Address             : Mountain View, CA 94040

            Date                : September 29, 2014

            Copyright (c) 2014-Present.
            All Rights Reserved.
       --------------------------------------------------------------------------*/
        {
            doRun();
        }
    }


    
    public void start_update_market ()
    /*------------------------------------------------------------------------
        start_update_market : start data update process
     
        Written By          : RaspiRepo
        Address             : Mountain View, CA 94040

        Date                : September 29, 2014

        Copyright (c) 2014-Present.
        All Rights Reserved.
     ------------------------------------------------------------------------*/
    {
        Calendar rightNow = Calendar.getInstance();
        
        //market closed so terminate update process
        nyse_realtime.stop_realtime_update();
        nasdaq_realtime.stop_realtime_update();
        
        //start real time makrket update process
        nasdaq_realtime.init_stock_exchange("nasdaq");
        nyse_realtime.init_stock_exchange("nyse");
        
        //start real time makrket update process
        nasdaq_realtime.init_stock_exchange_live();
        nyse_realtime.init_stock_exchange_live();
        
        System.out.println("Market....open... " + rightNow.getTime());
        log.write("Market....open... " + rightNow.getTime());
        
        //email notification
        String email_msg = rightNow.getTime() + "<br><br>";
        email_msg += "Stock Market session open, data update started"
                    + "<br><br><br>Report from Alert System<br>t105.";
        
        nyse_realtime.start_realtime_update();
        nasdaq_realtime.start_realtime_update();
        
        //send email message
        //compose_send("Market open", email_msg);
        market_update_flag = true;
    }
    
    
    
    
    public void stop_live_market_update ()
    /*------------------------------------------------------------------------
     stop_live_market_update  : stop data update process

    Written By          : RaspiRepo
    Address             : Mountain View, CA 94040

     Date                : September 29, 2014
     
     Copyright (c) 2014-Present.
     All Rights Reserved.
     ------------------------------------------------------------------------*/
    {
        Calendar rightNow = Calendar.getInstance();
        
        //market closed so terminate update process
        nyse_realtime.stop_realtime_update();
        nasdaq_realtime.stop_realtime_update();
        
        market_update_flag = false;
        
        //email notification
        String email_msg = rightNow.getTime() + "\n\n";
        email_msg += "Stock Market session ended, data update stopped"
        + "\n\n\nReport from Alert System\nt105.";
        
        //send email message
        compose_send("Market closed", email_msg);
        
        System.out.println("Market....closed... " + rightNow.getTime());
        log.write("Market....closed... " + rightNow.getTime());
        
    }

    
     public void schedule_market_open ()
    /*------------------------------------------------------------------------
        setup_market_open
                            : Create fixed time scheduler and excute end
                              of day market update for every day
     
        Written By          : RaspiRepo
        Address             : Mountain View, CA 94040

        Date                : September 29, 2014

        Copyright (c) 2014-Present.
        All Rights Reserved.
    ------------------------------------------------------------------------*/
    {
        Calendar rightNow = Calendar.getInstance();

        boolean market_open_flag = false;
        
        //set date and time of first execution, then interval of
        //next execution 6.30 AM
        int curr_hr  = rightNow.get(Calendar.HOUR_OF_DAY);
        int curr_min = rightNow.get(Calendar.MINUTE);
        int week_day = rightNow.get(Calendar.DAY_OF_WEEK);

        
        //current day market already closed then add one day next
        if (curr_hr > 14) {
            rightNow.add(Calendar.DAY_OF_MONTH, 1);
            
        } else if (curr_hr > 06 || (curr_hr == 06 && curr_min > 00)) {
            if (week_day > 1 && week_day < 7) {
                start_update_market();
                System.out.println("start  executre right now");
            }

            rightNow.add(Calendar.DAY_OF_MONTH, 1);
        }
                   
        //set date and time of first execution, then interval of
        //next execution 6.30 AM
        rightNow.set(Calendar.HOUR_OF_DAY, 06);
        rightNow.set(Calendar.MINUTE, 25);
        rightNow.set(Calendar.SECOND, 00);

       
        //Create the schedule class instance and implement
        //the run method and schedule the process for next time
        //running.
        task_reminder open_process = new task_reminder() {
            public void doRun ()
            /*-----------------------------------------------------------------
                doRun       : Thread run method implemented to call a function
                              to process the message events

                Written By          : RaspiRepo
                Address             : Mountain View, CA 94040

                Copyright (c) 2014-Present.
                All Rights Reserved.
            -----------------------------------------------------------------*/
            {
                try {
                    Calendar rightNow = Calendar.getInstance();

                    int week_day = rightNow.get(Calendar.DAY_OF_WEEK);

                    //only week days only market open so days are monday to friday
                    if (week_day > 1 && week_day < 7) {
                        
                        int curr_hr = rightNow.get(Calendar.HOUR_OF_DAY);
                        int curr_min = rightNow.get(Calendar.MINUTE);

                        System.out.println("Market....open.. curr_hour " + curr_hr + ":" + curr_min);
                        
                        //start real time makrket update process
                        start_update_market();

                    }
                } catch (Exception er) {
                    System.out.println("schedule_market_open: doRun: " + er.getMessage());
                    log.write("schedule_market_open gone wrong!!! ..." + er.getMessage());

                }
            }
        };

        //schedule this task for next calling
        market_open_timer.schedule(open_process, rightNow.getTime(), next_time_check);
        System.out.println("market open update schedule it for " + rightNow.getTime());
        rightNow = null;

    }



     public void schedule_market_close ()
    /*------------------------------------------------------------------------
        setup_market_open
                            : Create fixed time scheduler and excute end
                              of day market update for every day
        
        Written By          : RaspiRepo
        Address             : Mountain View, CA 94040

        Date                : September 29, 2014

        Copyright (c) 2014-Present.
        All Rights Reserved.
    ------------------------------------------------------------------------*/
    {
        Calendar rightNow = Calendar.getInstance();

        //if current day market time already closed
        //schedule to next day morning
        if (rightNow.get(Calendar.HOUR_OF_DAY) > 12) {
            rightNow.add(Calendar.DAY_OF_MONTH, 1); 
        }

        //set date and time of first execution, then interval of 
        //next execution 6.30 AM 
        rightNow.set(Calendar.HOUR_OF_DAY, 13);
        rightNow.set(Calendar.MINUTE , 50);
        rightNow.set(Calendar.SECOND , 00);


        //Create the schedule class instance and implement
        //the run method and schedule the process for next time
        //running.
        task_reminder close_process = new task_reminder() {
            public void doRun ()
            /*-----------------------------------------------------------------
                doRun       : Thread run method implemented to call a function
                              to process the message events

                Written By          : RaspiRepo
                Address             : Mountain View, CA 94040

                Copyright (c) 2014-Present.
                All Rights Reserved.
            -----------------------------------------------------------------*/
            {
                try {
                    Calendar rightNow = Calendar.getInstance();
                    int week_day = rightNow.get(Calendar.DAY_OF_WEEK);

                    //only week days only market open so days are monday to friday
                    if (week_day > 1 && week_day < 7 || (market_update_flag == true)) {
                        stop_live_market_update();
                    }
                } catch (Exception er) {
                    System.out.println("schedule_market_close: doRun: " + er.getMessage());
                    log.write("schedule_market_close: gone wrong!!! ..." + er.getMessage());
                }
            }
        };

        //schedule this task for next calling
        market_close_timer.schedule(close_process, rightNow.getTime(), next_time_check);
        System.out.println("market close update schedule it for " + rightNow.getTime());
        rightNow = null;

    }

    
    
    public void endof_day_market_update (Calendar rightNow)
    /*------------------------------------------------------------------------
     endof_day_market_update  : stop data update process
     
     Written By          : RaspiRepo
     Address             : Mountain View, CA 94040

     Date                : September 29, 2014
     
     Copyright (c) 2014-Present.
     All Rights Reserved.
     ------------------------------------------------------------------------*/
    {
        int start_year = 0;
        int start_mon  = 0; //Sep (0-11)
        int start_day  = 0;
        
        start_year = rightNow.get(Calendar.YEAR);
        start_mon = rightNow.get(Calendar.MONTH);
        start_day = rightNow.get(Calendar.DAY_OF_MONTH);
        
        int curr_hour = rightNow.get(Calendar.HOUR_OF_DAY);
        int curr_min = rightNow.get(Calendar.MINUTE);
        
        String log_msg = "nasdaq : Daily History started for " + rightNow.getTime();
        
        System.out.println(log_msg);
        log.write(log_msg);
        
        //NASDAQ symbols
        endofday_update.init_stock_exchange("nasdaq");

        int end_day = start_day; //for same day  or 1 day

        //execute
        endofday_update.start_history_update(start_year, start_mon, start_day, start_year, start_mon, end_day);
        
        log_msg = "nyse : Daily History started for " + rightNow.getTime();
        System.out.println(log_msg);
        log.write(log_msg);

        //NYSE Exchange symbols
        endofday_update.init_stock_exchange("nyse");
        endofday_update.start_history_update(start_year, start_mon, start_day, start_year, start_mon, end_day);
        
        //email notification
        rightNow = Calendar.getInstance();
        String email_msg = rightNow.getTime() + "<br><br>";
        email_msg += "EXCHANGE end of the day update done, number symbols updated :"
                     + "<br><br><br>Report from Alert System<br>t105.";
        
        compose_send("End of the day Update", email_msg);
        
        log_msg = "Daily History Completed for " + rightNow.getTime();
        System.out.println(log_msg);
        log.write(log_msg);
        
       
    }
    
    

     public void schedule_end_of_day_update (int day)
    /*------------------------------------------------------------------------
        setup_market_open
                            : Create fixed time scheduler and excute end
                              of day market update for every day
        
        Written By          : RaspiRepo
        Address             : Mountain View, CA 94040

        Date                : September 29, 2014

        Copyright (c) 2014-Present.
        All Rights Reserved.
    ------------------------------------------------------------------------*/
    {
        Calendar rightNow = Calendar.getInstance();

        //future date/time
        if (day == 0) {
            if (rightNow.get(Calendar.HOUR_OF_DAY) > 21) {
                rightNow.add(Calendar.DAY_OF_MONTH, 1);
            }
            
            //set date and time of first execution, then interval of
            //next execution 3.30 PM 
            rightNow.set(Calendar.HOUR_OF_DAY, 17);
            rightNow.set(Calendar.MINUTE , 30);
            rightNow.set(Calendar.SECOND , 00);


            //Create the schedule class instance and implement
            //the run method and schedule the process for next time
            //running.
            task_reminder hist_process = new task_reminder() {
                public void doRun ()
                /*-----------------------------------------------------------------
                    doRun       : Thread run method implemented to call a function
                                  to process the message events

                    Written By  : K.Mariya (email: mariya@opticalfusion.net)

                    Copyright (c) 2014-Present.
                    All Rights Reserved.
                -----------------------------------------------------------------*/
                {
                    try {
                        Calendar rightNow = Calendar.getInstance();

                        int week_day = rightNow.get(Calendar.DAY_OF_WEEK);

                        //only week days only market open so days are monday to friday
                        if (week_day > 1 && week_day < 7) {
                            endof_day_market_update(rightNow);
                         }
                    } catch (Exception er) {
                        System.out.println("schedule_end_of_day_update: doRun: " + er.getMessage());
                    }
                }
            };

            System.out.println("Daily History update schedule it for " + rightNow.getTime());
            
            //schedule this task for next calling
            endoday_timer.schedule(hist_process, rightNow.getTime(), next_time_check);
        } else {

            rightNow = Calendar.getInstance();
            rightNow.set(Calendar.DAY_OF_MONTH, day);
            
            endof_day_market_update(rightNow);
        }
        rightNow = null;

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
        stock_realtime_schedule schedule_trade = new stock_realtime_schedule();

        //schedule_trade.start_update_market();

        schedule_trade.schedule_market_open();
        schedule_trade.schedule_market_close();
        schedule_trade.schedule_end_of_day_update(0); //earlier date update 1, realtime update 0
    }
}
