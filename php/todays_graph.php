<?php
require_once('myconfig.php'); 
require_once('../phpgraphlib/phpgraphlib.php');

//Create connection
$conn = new mysqli($hostname, $username, $password, $database);

// Check connection
if ($conn->connect_error) {
     die("Connection failed: " . $conn->connect_error);
} 

$sql = "";
if (isset($_GET["qry"])) {
	$sql = $_GET["qry"];
}

$type = "";
if (isset($_GET["t"])) {
	$type = $_GET["t"];
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
		if ($type == "p") {
			$curr_price = number_format($row["last_traded"], 2);
		} else {
			$curr_price = number_format($row["rsi"], 2);
		}
		$day        = date_format(date_create($row["date"]), 'd');

		if ($curr_price > 0 && $close_price != $curr_price) {
			$close_price = $curr_price;

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
	}
	$avg = $sum / $num_samples;
}
$graph = new PHPGraphLib(1200,200);
$graph->addData($price_value);
$graph->setTitle('Today Change');
$graph->setBars(false);
$graph->setLine(true);
$graph->setDataPoints(false);
$graph->setDataPointColor('green');
$graph->setDataValues(false);
$graph->setDataValueColor('blue');
$graph->setGoalLine($avg);
$graph->setRange($min, $max);
$graph->setGoalLineColor('red');
$graph->createGraph();



?>