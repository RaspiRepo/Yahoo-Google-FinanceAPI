<?php
require_once('myconfig.php'); 
require_once('../phpgraphlib/phpgraphlib.php');

//Create connection
$conn = new mysqli($hostname, $username, $password, $database);

// Check connection
if ($conn->connect_error) {
     die("Connection failed: " . $conn->connect_error);
} 

$rsi_tb_name = "";
if (isset($_GET["tbl"])) {
	$rsi_tb_name = $_GET["tbl"];
}

$symbol = "";
if (isset($_GET["symbol"])) {
	$symbol = $_GET["symbol"];
}

$start_rec = 0;
if (isset($_GET["start"])) {
	$start_rec = $_GET["start"];
}
$end = 100;
if (isset($_GET["end"])) {
	$end = $_GET["end"];
}

$tbl = "2016_09_30_nasdaq_stocks";
$symbol="aaoi";
$start_rec =0;
$end = 100;

$sql = "SELECT rsi FROM `".$rsi_tb_name."` where symbol = '".$symbol."' order by date asc limit " $end - $start_rec;
$result = $conn->query($sql);
$avg = 0;
//echo $sql;

if ($result->num_rows > 0) {

	// output data of each row
	$row = $result->fetch_assoc();
	$num_samples = 0;
	$sum = 0;
	$min = 0;
	$max = 100;

	$num_records = $result->num_rows;
	if ($num_records > 50) {
		$sample_intval = $num_records / 50;
	}

	// output data of each row
	while($row = $result->fetch_assoc()) {
//		$samples_skip = $sample_intval;
//		while($samples_skip-- > 0) {
//			$result->fetch_assoc();
//		}
		$curr_rsi = number_format($row["rsi"], 2);
		//$day        = date_format(date_create($row["date"]), 'd');

		if ($close_rsi != $curr_rsi) {
			$close_rsi = $curr_rsi;

//			if ($max < $close_rsi) {
//				$max = $close_rsi;
//			}
//			if ($min > $close_rsi) {
//				$min = $close_rsi;
//			}

			$sum += $close_rsi;
			$price_value[$num_samples] = $close_rsi;
			$num_samples++;
		}
	}
	$avg = $sum / $num_samples;
}

$graph = new PHPGraphLib(1200,300);
$graph->addData($price_value);
$graph->setTitle('Todays RSI');
$graph->setBars(false);
$graph->setLine(true);
$graph->setDataPoints(false);
$graph->setDataPointColor('maroon');
$graph->setDataValues(false);
$graph->setDataValueColor('maroon');
$graph->setGoalLine(30);
$graph->setRange($min, $max);
$graph->setGoalLineColor('red');
$graph->createGraph();

?>