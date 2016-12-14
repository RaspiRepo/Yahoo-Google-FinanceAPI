<?php 
require_once('myconfig.php'); 


$type = "p";
$label = "Price ";
if (isset($_GET["type"])) {
	$type = $_GET["type"];
}

$symbol = "";
if (isset($_GET["symbol"])) {
	$symbol = $_GET["symbol"];
}

if ($type == "r") {
    $label = "RSI ";
}
    
    
//Create connection
$conn = new mysqli($hostname, $username, $password, $database);

// Check connection
if ($conn->connect_error) {
     die("Connection failed: " . $conn->connect_error);
} 


$xchange   = "";
$comp_name = "";
$goog_url  = "https://www.google.com/finance?q=";
$week52_low_high = "";
$symbol_name = $symbol;


$xchange = "nasdaq";
$sql = "SELECT description FROM `".$xchange."` where symbol = '".$symbol_name."'";
$result = $conn->query($sql);
//echo $sql;
//echo $result->num_rows;

if ($result->num_rows > 0) {
	$row = mysqli_fetch_assoc($result);
	$comp_name = $row['description'];
	
	// 52 week high, low 
	$sql = "SELECT MAX(high) as week_high, MIN(low) as week_low from ".$xchange."_daily_history where symbol = '".$symbol_name."' order by date desc";
	$result = $conn->query($sql);
	if ($result->num_rows > 0) {
		$row = mysqli_fetch_assoc($result);
		$week52_low_high = number_format($row['week_low'], 2).' - '.number_format($row['week_high'],2);
	}
	$goog_url = $goog_url."NASDAQ%3A".$symbol_name."&ei=F2nOV_ndJ86IigKmi4HwAw";
} else {
	$xchange = "nyse";
	$sql = "SELECT description FROM `".$xchange."` where symbol = '".$symbol_name."'";
	$result = $conn->query($sql);
	if ($result->num_rows > 0) {
		$row = mysqli_fetch_assoc($result);
		$comp_name = $row['description'];
	
		// 52 week high, low
		$sql = "SELECT MAX(high) as week_high, MIN(low) as week_low from ".$xchange."_daily_history where symbol = '".$symbol_name."' order by date desc";
		
		$result = $conn->query($sql);
		if ($result->num_rows > 0) {
			$row = mysqli_fetch_assoc($result);
			$week52_low_high = number_format($row['week_low'], 2).' - '.number_format($row['week_high'],2);
		}
		$goog_url = $goog_url."NYSE%3A".$symbol_name."&ei=F2nOV_ndJ86IigKmi4HwAw";
	}
}


//fine exchange for this symbol
//find_stock_exchange($symbol);
    
$rows = array();
$table = array();
$today = date("Y_m_d");

//echo "Symbol ".$symbol."exchange name ".$xchange;
    
//find exchange name
$market_daily_tb_name = $today.'_'.$xchange.'_stocks';

//echo $market_daily_tb_name;

//$market_daily_tb_name = "2016_09_30_".$xchange."_stocks";

$num_records = 400; //just max limit

//$sql = "SELECT * FROM `".$market_daily_tb_name ."` WHERE symbol = '".$symbol."' order by date desc, rec_id asc limit ".$num_records;
$sql = "SELECT last_traded, rsi, date FROM (SELECT * FROM `".$market_daily_tb_name ."` WHERE symbol = '".$symbol."' ORDER BY rec_id desc limit ".$num_records.") sub order by date asc";

//SELECT * FROM (SELECT * FROM recharge ORDER BY sno DESC LIMIT 5)sub ORDER BY sno ASC
$result = $conn->query($sql);


$rows = array();
if ($result->num_rows > 0) {

	// output data of each row
	while($row = $result->fetch_assoc()) {
        
        $temp = array();
        
		if ($type == "p") {
			$curr_value = number_format($row["last_traded"], 2);
            if ($curr_value > 0.0) {
                
                $time   = date_format(date_create($row["date"]), 'H:i');
                $temp[] = array('v' => (string) $time);
                
                // Values of each slice
                $temp[] = array('v' => (double) $curr_value);
                $rows[] = array('c' => $temp);
            }

        } else if ($type == "c") {
            $curr_value = number_format($row["change_value"], 2);
           
            $time   = date_format(date_create($row["date"]), 'H:i');
            $temp[] = array('v' => (string) $time);
            
            // Values of each slice
            $temp[] = array('v' => (double) $curr_value);
            $rows[] = array('c' => $temp);
        } else if ($type == "cp") {
            $curr_value = number_format($row["change_per"], 2);
           
            $time   = date_format(date_create($row["date"]), 'H:i');
            $temp[] = array('v' => (string) $time);
            
            // Values of each slice
            $temp[] = array('v' => (double) $curr_value);
            $rows[] = array('c' => $temp);
            
        //always default rsi
        } else {
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
}

$table['rows'] = $rows;

$table['cols'] = array(

    // Labels for your chart, these represent the column titles
    // Note that one column is in "string" format and another one is in "number" format as pie chart only required "numbers" for
    // calculating percentage and string will be used for column title
    array('label' => 'Time', 'type' => 'string'),
    array('label' => $label.$symbol, 'type' => 'number')

);

$jsonTable = json_encode($table);
echo $jsonTable;
?>