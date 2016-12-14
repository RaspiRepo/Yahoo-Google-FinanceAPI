<?php

require("Symbol_info.php");

class stock_database
/*----------------------------------------------------------------------------
    stock_database.php 
                   : Class get stock symbol from databse and generate 
                     end of the day records.

    Written by     : RaspiRepo
	Date           : Oct 04, 2016
------------------------------------------------------------------------------*/
{
	private $mysql_dbconn = NULL;

    private $interday_price_table = NULL;
    private $history_table        = NULL;
    private $dividend_table       = NULL;


    public function connect_database ()
    /*------------------------------------------------------------------------
		connect_database    
					   : Connect to stock_automate database
        
        Written by     : RaspiRepo
        Date           : Oct 04, 2016
    ------------------------------------------------------------------------*/
    {
		require_once('myconfig.php'); 
		$this->mysql_dbconn = new mysqli($hostname, $username, 
			                             $password, $database);

		// Check connection
		if ($this->mysql_dbconn->connect_error) {
			 die("Connection failed: " . $this->mysql_dbconn->connect_error);
		}
    }

    public function get_exchange ($symbol)
    /*------------------------------------------------------------------------
		get_exchange   : Return exchnage table name of given stock symbol
        
        Written by     : RaspiRepo
        Date           : Oct 04, 2016
    ------------------------------------------------------------------------*/
    {
		$exchange_tb_name = '';

		$sql = "SELECT description FROM `nasdaq` where symbol = '".$symbol."'";
		$result = $this->mysql_dbconn->query($sql);
		if ($result->num_rows > 0) {
			$exchange_tb_name = 'nasdaq';
			$row = mysqli_fetch_assoc($result);

		} else {
			$sql = "SELECT description FROM `nyse` where symbol = '".$symbol."'";
			$result = $this->mysql_dbconn->query($sql);
			if ($result->num_rows > 0) {
				$row = mysqli_fetch_assoc($result);
				$exchange_tb_name = 'nyse';
			}
		}
		return $exchange_tb_name;
    }


    public function get_company_name ($symbol)
    /*------------------------------------------------------------------------
		get_company_name
		               : Return given stock symbol's company name
        
        Written by     : RaspiRepo
        Date           : Oct 04, 2016
    ------------------------------------------------------------------------*/
    {
		$comp_name = '';

		$sql = "SELECT description FROM `nasdaq` where symbol = '".$symbol."'";
		$result = $this->mysql_dbconn->query($sql);
		if ($result->num_rows > 0) {
			$row = mysqli_fetch_assoc($result);
			$comp_name = $row['description'];

		} else {
			$sql = "SELECT description FROM `nyse` where symbol = '".$symbol."'";
			$result = $this->mysql_dbconn->query($sql);
			if ($result->num_rows > 0) {
				$row = mysqli_fetch_assoc($result);
				$comp_name = $row['description'];
			}
		}
		return $comp_name;
    }


    public function get_52_week_high_low ($symbol)
    /*------------------------------------------------------------------------
		get_52_week_high_low    
					   : Get stock's 52 week High/Low
        
        Written by     : RaspiRepo
        Date           : Oct 04, 2016
    ------------------------------------------------------------------------*/
    {
		$week52_low_high = '';
		$exchange = $this->get_exchange ($symbol);

		// 52 week high, low 
		$sql = "SELECT MAX(high) as week_high, MIN(low) as week_low from ".$exchange."_daily_history where symbol = '".$symbol."' order by date desc limit 1";

		$result = $this->mysql_dbconn->query($sql);
		if ($result->num_rows > 0) {
			$row = mysqli_fetch_assoc($result);
			$week52_low_high = number_format($row['week_low'], 2).' - '.number_format($row['week_high'],2);
		}
		return $week52_low_high;
	}


    public function disconnect_database ()
    /*------------------------------------------------------------------------
	disconnect_database
                       : disconnect from stock_automate database
        
        Written by     : RaspiRepo
        Date           : Oct 04, 2016
    ------------------------------------------------------------------------*/
    {
		$this->mysql_dbconnclose();
    }




    public function get_todays_price_json ($symbol, $label)
   /*------------------------------------------------------------------------
		get_todays_price_json    
					   : Function get given symbols todays traded price values
					     in json array format
        
        Written by     : RaspiRepo
        Date           : Oct 04, 2016
    ------------------------------------------------------------------------*/
    {
		$today = $this->get_trading_date();

		$exchange = $this->get_exchange ($symbol);

		$daily_tb_name = $today.'_'.$exchange.'_stocks';

		$num_records = 100; //change this to time rage

		$sql = "SELECT last_traded, date FROM (SELECT * FROM `".$daily_tb_name 
				."` WHERE symbol = '".$symbol."' ORDER BY rec_id desc limit "
				.$num_records.") sub order by date asc";

		$result = $this->mysql_dbconn->query($sql);
		
		
		$rows = array();
		$table = array();
		if ($result->num_rows > 0) {

			// output data of each row
			while($row = $result->fetch_assoc()) {
				$curr_value = number_format($row["last_traded"], 2);
				if ($curr_value > 0.0) {
					
					$time   = date_format(date_create($row["date"]), 'H:i');
					$temp[] = array('v' => (string) $time);
					
					// Values of each slice
					$temp[] = array('v' => (double) $curr_value);
					$rows[] = array('c' => $temp);
				}
			}
		}

		$table['rows'] = $rows;


		$table['cols'] = array(
			// Labels for your chart, these represent the column titles
			// Note that one column is in "string" format and another one is in 
			//"number" format as pie chart only required "numbers" for
			// calculating percentage and string will be used for column title
			array('label' => 'Time', 'type' => 'string'),
			array('label' => $label.$symbol, 'type' => 'number')
			);

		$jsonTable = json_encode($table);

		return $jsonTable;
	}





    public function get_todays_rsi_json ($symbol, $label)
   /*------------------------------------------------------------------------
		get_todays_rsi_json    
					   : Function get given symbols todays traded RSI values
					     in json array format
        
        Written by     : RaspiRepo
        Date           : Oct 04, 2016
    ------------------------------------------------------------------------*/
    {
		$rows = array();
		$table = array();

		$today = $this->get_trading_date();

		$exchange = $this->get_exchange ($symbol);

		$daily_tb_name = $today.'_'.$exchange.'_stocks';

		$num_records = 100; //change this to time rage

		$sql = "SELECT rsi, date FROM (SELECT * FROM `".$daily_tb_name 
				."` WHERE symbol = '".$symbol."' ORDER BY rec_id desc limit "
				.$num_records.") sub order by date asc";

		$result = $this->mysql_dbconn->query($sql);
		if ($result->num_rows > 0) {

			// output data of each row
			while($row = $result->fetch_assoc()) {
				$curr_value = number_format($row["rsi"], 2);
				if ($curr_value > 0.0) {
					
					$time   = date_format(date_create($row["date"]), 'H:i');
					$temp[] = array('v' => (string) $time);
					
					// Values of each slice
					$temp[] = array('v' => (double) $curr_value);
					$rows[] = array('c' => $temp);
				}
			}
		}

		$table['rows'] = $rows;
		$table['cols'] = array(
			// Labels for your chart, these represent the column titles
			// Note that one column is in "string" format and another one is in 
			//"number" format as pie chart only required "numbers" for
			// calculating percentage and string will be used for column title
			array('label' => 'Time', 'type' => 'string'),
			array('label' => $label.$symbol, 'type' => 'number')
			);

		$jsonTable = json_encode($table);
		return $jsonTable;
	}



    public function get_trading_date ()
   /*------------------------------------------------------------------------
		get_todays_rsi_json    
					   : Function get given symbols todays traded RSI values
					     in json array format
        
        Written by     : RaspiRepo
        Date           : Oct 04, 2016
    ------------------------------------------------------------------------*/
    {
		$date = new DateTime(date("Y-m-d"));
		//$date->modify('-1 day');

		$today = $date->format('Y_m_d');

		return $today;

	}


    public function get_stock_current_info ($symbol)
   /*------------------------------------------------------------------------
		get_todays_rsi_json    
					   : Function get given symbols todays traded RSI values
					     in json array format
        
        Written by     : RaspiRepo
        Date           : Oct 04, 2016
    ------------------------------------------------------------------------*/
    {
		$stock_info = new Symbol_info();

		$stock_info->symbol = $symbol;

		$today = $this->get_trading_date();

		$exchange = $this->get_exchange ($symbol);

		$daily_tb_name = $today.'_'.$exchange.'_stocks';

		$num_records = 1; //most recent record only

		$sql = "SELECT * FROM `".$daily_tb_name 
				."` WHERE symbol = '".$symbol."' ORDER BY date desc limit "
				.$num_records;
		$result = $this->mysql_dbconn->query($sql);
		if ($result->num_rows > 0) {
			$row = $result->fetch_assoc();

			$stock_info->date_time    = date_format(date_create($row["date"]), 'Y-m-d H:i:s');
			$stock_info->last_traded  = $row["last_traded"];
			$stock_info->prev_close   = $row["prev_close"];
			$stock_info->change       = $row["change_value"];
			$stock_info->per_change   = $row["change_per"];
			$stock_info->volume       = $row["volume"];
			$stock_info->rsi          = $row["rsi"];
		}

		//return class object instance
		return $stock_info;
	}
}//end of class


/*
echo '<html><head></head><body>';
$db_obj = new stock_database();
$db_obj->connect_database();

$symbol = 'baba';

echo  $db_obj->get_exchange ($symbol). ' '.$db_obj->get_company_name($symbol).'</br>';
echo $db_obj->get_52_week_high_low($symbol).'</br></br></br>';

$stk_info = $db_obj->get_stock_current_info ($symbol);
echo 'Date        '.$stk_info->date_time.'</br>';
echo 'prev_close  '.$stk_info->prev_close.'</br>';
echo 'last_traded '.$stk_info->last_traded.'</br>';
echo 'change      '.$stk_info->change.'</br>';
echo 'per_change  '.$stk_info->per_change.'</br>';
echo 'volume      '.$stk_info->volume.'</br>';
echo 'RSI         '.$stk_info->rsi.'</br>';

//echo $db_obj->get_todays_price_json($symbol);
//echo '</br></br></br></br></br>';
//echo $db_obj->get_todays_rsi_json($symbol);

$db_obj->disconnect_database();
echo '</body></html>';
*/
?>