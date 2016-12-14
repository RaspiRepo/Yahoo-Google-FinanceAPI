<?php 
require_once("stock_database.php");

$db_obj = new stock_database();
$db_obj->connect_database();

$type = "p";
$label = "Price ";
if (isset($_GET["type"])) {
	$type = $_GET["type"];
}

$symbol = "";
if (isset($_GET["symbol"])) {
	$symbol = $_GET["symbol"];
}

if ($type == "p") {
	echo $db_obj->get_todays_price_json($symbol, $label);
} else if ($type == "r") {
    $label = "RSI ";
	echo $db_obj->get_todays_rsi_json($symbol, $label);
}
$db_obj->disconnect_database();

?>