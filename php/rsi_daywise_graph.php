<?php
require_once('myconfig.php'); 
require_once('../phpgraphlib/phpgraphlib.php');

//Create connection
$conn = new mysqli($hostname, $username, $password, $database);

// Check connection
if ($conn->connect_error) {
     die("Connection failed: " . $conn->connect_error);
} 
/*
$symbol         = 'AAPL';
$num_records = 14;

if (isset($_GET["symbol"])) {
	$symbol = $_GET["symbol"];
}

if (isset($_GET["days"])) {
	$num_records = $_GET["days"];
}

$price_value = array();
$market_daily_tb_name = 'nyse_daily_history';
$market_daily_tb_name = 'nasdaq_daily_history';


$sql = "SELECT * FROM (SELECT date as cu_date, open, close, low, high from `".$market_daily_tb_name 
          ."` WHERE symbol = '".$symbol."' order by cu_date desc limit ".($num_records)." ) as tmp order by cu_date";
*/
//header("Content-type: image/png");
$sql = "";
if (isset($_GET["qry"])) {
	$sql = $_GET["qry"];
}
$result = $conn->query($sql);
$avg = 0;

if ($result->num_rows > 0) {

	// output data of each row
	$row = $result->fetch_assoc();
	$num_samples = 0;
	$sum = 0;
	$min = 9999.00;
	$max = 0;

	// output data of each row
	while($row = $result->fetch_assoc()) {
		$date = date_create($row["cu_date"]);
		$day  = date_format($date, 'd');

		$close_price = number_format($row["close"],2);

		if ($max < $close_price) {
			$max = $close_price;
		}
		if ($min > $close_price) {
			$min = $close_price;
		}

		$sum += $close_price;
		$price_value[$num_samples] = $close_price;
		$num_samples++;
	}
	$avg = $sum / $num_samples;
}
$graph = new PHPGraphLib(1200,200);
$graph->addData($price_value);
$graph->setTitle('Price Change');
$graph->setBars(false);
$graph->setLine(true);
$graph->setDataPoints(true);
$graph->setDataPointColor('maroon');
$graph->setDataValues(true);
$graph->setDataValueColor('maroon');
$graph->setGoalLine($avg);
$graph->setRange($min, $max);
$graph->setGoalLineColor('red');
$graph->createGraph();



?>