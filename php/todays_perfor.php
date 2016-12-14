<!DOCTYPE html>
<html lang="en">
<head>
  <title>Stock Performance</title>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
  <link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/foundation/5.1.1/css/normalize.css" />

  <script src="jquery.min.js"></script>
  <script src="bootstrap.min.js"></script>



</head>
<body>



<?php
require_once('myconfig.php'); 

$symbol         = 'baba';
$num_of_samples = 30;

if (isset($_POST["symbol"])) {
	$symbol = $_POST["symbol"];
}

if (isset($_POST["rows"])) {
	$num_of_samples = $_POST["rows"];
}

//Create connection
$conn = new mysqli($hostname, $username, $password, $database);

// Check connection
if ($conn->connect_error) {
     die("Connection failed: " . $conn->connect_error);
} 

$today = "2016_09_30"; //"date("Y_m_d"); 

//find exchange name
$market_daily_tb_name = 'nasdaq_daily_history';
$comp_name = "";
$goog_url  = "https://www.google.com/finance?q=";
$rsi_tb_name = $today.'_nasdaq_rsi_temp';


$sql = "SELECT description FROM `nasdaq` where symbol = '".$symbol."'";
$result = $conn->query($sql);
if ($result->num_rows > 0) {
	$row = mysqli_fetch_assoc($result);
	$comp_name = $row['description'];
	$goog_url = $goog_url."NASDAQ%3A".$symbol."&ei=F2nOV_ndJ86IigKmi4HwAw";
	
	//find exchange name
    $market_daily_tb_name = $today.'_nasdaq_stocks';
	
} else {
	$sql = "SELECT description FROM `nyse` where symbol = '".$symbol."'";
	$result = $conn->query($sql);
	if ($result->num_rows > 0) {
		$row = mysqli_fetch_assoc($result);

		$market_daily_tb_name = 'nyse_daily_history';
		$comp_name = $row['description'];
		$goog_url = $goog_url."NYSE%3A".$symbol."&ei=F2nOV_ndJ86IigKmi4HwAw";

		$market_daily_tb_name = $today.'_nyse_stocks';
		$rsi_tb_name = $today.'_nyse_rsi_t';
	}
}
//echo $market_daily_tb_name;
//http://www.marketvolume.com/stocks/oversold.asp   ***** OVER SOLD STOCKS LIST *****
$num_records = $num_of_samples;

//$market_daily_tb_name = "2016_09_27_nyse_stocks";

$sql = "SELECT * FROM `".$market_daily_tb_name ."` WHERE symbol = '".$symbol."' order by rec_id asc, date asc limit ".$num_records;
$result = $conn->query($sql);
//echo $market_daily_tb_name;

?>


<div class="container">
  <?php echo "<h2 ><a href=$goog_url>".strtoupper($symbol)."</a>(".$comp_name.")</h2>"; ?>

	<?php echo '<table class="table table-hover"><tr><div id="DaywisePrice">';
	echo '<img width="1200" height="200" src="todays_graph.php?t=p&qry='.$sql.'"/>';
	echo '</div></tr></table>'; 
	?>

	<?php 
		echo '<table class="table table-hover"><tr><div id="RSI">';
		echo '<img width="1200" height="200" src="todays_graph.php?t=r&qry='.$sql.'"/>';
		echo '</div></tr></table>'; 
	?>

   <table class="table table-hover">
      <tr class="warning">

  <form action="todays_perfor.php" method="post">
  <td>
  <?php echo "<input type='text' name='symbol' value ='".$symbol."'>"; ?>
   <?php echo "<input type='text' name='rows' value ='".$num_of_samples."'>"; ?>
 
    <input type="submit" name ='Apply'></td>

	</td><td>
	</td><td>
<td></td>
<td></td>
<td></td>
	</table>

     <table class="table table-hover">

    <thead>
      <tr class="success">
        <th>S.No</th>
        <th>Date</th>
        <th>Prev Close</th>
        <th>last traded</th>
        <th>change</th>
        <th>change %</th>
        <th>volume</th>
        <th>rsi</th>
        <th></th>        
      </tr>
    </thead>
    <tbody>

<?php
$colors = array("success", "danger", "info");
$color_index = 0;


if ($result->num_rows > 0) {

	$index = 1;
	$prev_trade = 0;

     // output data of each row
     while($row = $result->fetch_assoc()) {
		if ($prev_trade != $row["last_traded"]) {

			$prev_trade = $row["last_traded"];
			$date = date_create($row["date"]);

			$rec_time = date_format($date, 'H:i:s');
			echo '<tr>';
			echo '<td>'.$index.'</td>';

			 echo "<td>" . $rec_time. "</td>";
			 echo "<td>" . number_format($row["prev_close"], 2). "</td>";
			 echo "<td>" . number_format($row["last_traded"], 2). "</td>";
			 echo "<td>" . number_format($row["change_value"],2). "</td>";
			 echo "<td>" . number_format($row["change_per"],2). "</td>";
			 echo "<td>" . number_format($row["volume"], 0). "</td>";
			 
			 
			 $rsi = number_format($row["rsi"],2);
			 
			 if ($rsi > 0.0) {
			 	echo "<td>". $rsi ."</td>";
			 }

			 echo "</tr>";
			 $index++;
		}
		 
	  }
	/* free result set */
	$result->free();

	/* close connection */
	$mysqli->close();
}
?>
    </tbody>
  </table>


</form>
</div>

</body>
</html>