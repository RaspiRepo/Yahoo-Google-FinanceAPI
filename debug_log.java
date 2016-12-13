/*----------------------------------------------------------------------------
    debug_log.java : debug log writting 

    Written by     : RaspiRepo
    Date           : Aug 30, 2016
------------------------------------------------------------------------------*/


import java.text.DateFormat;
import java.text.SimpleDateFormat;
 

import java.io.*;
import java.util.*;




public class debug_log
/*----------------------------------------------------------------------------
    debug_log      : Class allow to create write debug information

    Written by     : RaspiRepo
    Date           : Aug 30, 2016
------------------------------------------------------------------------------*/
{
    
    private PrintWriter writer = null;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private Date date = new Date(System.currentTimeMillis());



    public void open (String logfile_name)
    /*------------------------------------------------------------------------
        open           : open debug log file
        
        Written by     : RaspiRepo
        Date           : Aug 30, 2016
    ------------------------------------------------------------------------*/
    {
        try {
            writer = new PrintWriter(logfile_name, "UTF-8");
        } catch (IOException e) {
        }
    }


    public void open_debug_log ()
    /*------------------------------------------------------------------------
        open           : open debug log file
        
        Written by     : RaspiRepo
        Date           : Aug 30, 2016
    ------------------------------------------------------------------------*/
    {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
            String logfile_name = dateFormat.format(new Date()) + "_reports.txt";
            writer = new PrintWriter(logfile_name, "UTF-8");
            dateFormat = null;
        } catch (IOException e) {
        }
    }



    public void write (String error_msg)
    /*------------------------------------------------------------------------
        writ           : write to debug log file
        
        Written by     : RaspiRepo
        Date           : Aug 30, 2016
    ------------------------------------------------------------------------*/
    {
        try {
            writer.print("[" + dateFormat.format(new Date()) + "] ");
            writer.println(error_msg);
            writer.flush();
        } catch (Exception e) {
        }
    }



    public void close ()
    /*------------------------------------------------------------------------
        close          : close debug log file
        
        Written by     : RaspiRepo
        Date           : Aug 30, 2016
    ------------------------------------------------------------------------*/
    {
        try {
            writer.close();
        } catch (Exception e) {
        }
    }
}