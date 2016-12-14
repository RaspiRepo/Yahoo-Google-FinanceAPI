<!DOCTYPE html>
<html lang="en">
<head>
  <title>Stock Market Technical analysis</title>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
  <link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/foundation/5.1.1/css/normalize.css" />

  <script src="jquery.min.js"></script>
  <script src="bootstrap.min.js"></script>



    <!--Load the AJAX API-->
    <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
    <script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
    <script type="text/javascript">
    
    // Load the Visualization API and the piechart package.
    google.charts.load('current', {'packages':['corechart']});
      
    // Set a callback to run when the Google Visualization API is loaded.
    google.charts.setOnLoadCallback(drawpriceChart);
    
    <?php
    $symbol = 'vmw';
    $type   = 'd';
    //refreshInterval:5
    ?>
    
    function drawpriceChart() {
      var jsonData = $.ajax({
          url: "getData.php?symbol=<?php echo $symbol.'&type='.$type;?>",
          dataType: "json",
          async: false
          }).responseText;
          
          
      var options = {
           title: 'Stock Performance <?php echo $symbol; ?>',
          is3D: 'true',
           legend: 'none',
           lineWidth: 1,
           colors: ['green'],
          width: 1500,
          height: 250
        }; 
          
      // Create our data table out of JSON data loaded from server.
      var data = new google.visualization.DataTable(jsonData);

      // Instantiate and draw our chart, passing in some options.
      var chart = new google.visualization.LineChart(document.getElementById('chart_div'));
      chart.draw(data, options); 
    }
    </script>


</head>
<body>



<?php
require_once('myconfig.php'); 

$symbol         = 'AAPL';
$num_of_samples = 14;

if (isset($_POST["symbol"])) {
	$symbol = $_POST["symbol"];
}

if (isset($_POST["days"])) {
	$num_of_samples = $_POST["days"];
}

//Create connection
$conn = new mysqli($hostname, $username, $password, $database);

// Check connection
if ($conn->connect_error) {
     die("Connection failed: " . $conn->connect_error);
} 


//find exchange name
$market_daily_tb_name = 'nasdaq_daily_history';
$comp_name = "";
$goog_url  = "https://www.google.com/finance?q=";

$sql = "SELECT description FROM `nasdaq` where symbol = '".$symbol."'";
$result = $conn->query($sql);
if ($result->num_rows > 0) {
	$row = mysqli_fetch_assoc($result);
	$comp_name = $row['description'];
	$goog_url = $goog_url."NASDAQ%3A".$symbol."&ei=F2nOV_ndJ86IigKmi4HwAw";
} else {
	$sql = "SELECT description FROM `nyse` where symbol = '".$symbol."'";
	$result = $conn->query($sql);
	if ($result->num_rows > 0) {
		$row = mysqli_fetch_assoc($result);

		$market_daily_tb_name = 'nyse_daily_history';
		$comp_name = $row['description'];
		$goog_url = $goog_url."NYSE%3A".$symbol."&ei=F2nOV_ndJ86IigKmi4HwAw";
	}
}

//http://www.marketvolume.com/stocks/oversold.asp   ***** OVER SOLD STOCKS LIST *****
$num_records = $num_of_samples;

$sql = "SELECT * FROM (SELECT date as cu_date, open, close, low, high from `".$market_daily_tb_name 
          ."` WHERE symbol = '".$symbol."' order by cu_date desc limit ".($num_records)." ) as tmp order by cu_date";

$result = $conn->query($sql);
?>


<div class="container">
  <?php echo "<h2 >RSI <a href=$goog_url>".strtoupper($symbol)."</a>(".$comp_name.") ".$num_of_samples ." Days</h2>"; ?>

	<?php echo '<table class="table table-hover"><tr><div id="DaywisePrice">';
	echo '<img width="1200" height="200" src="rsi_daywise_graph.php?qry='.$sql.'"/>';
	echo '</div></tr></table>'; 

	?>

   <table class="table table-hover">
      <tr class="warning">

  <form action="rsi1.php" method="post">
  <td>
  <?php echo "<input type='text' name='symbol' value ='".$symbol."'>"; ?>
   <?php echo "<input type='text' name='days' value ='".$num_of_samples."'>"; ?>
 
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
        <th>Open</th>
        <th>Low</th>
        <th>High</th>
        <th>Close</th>
        <th>Gain/Loss</th>
        <th>Gain</th>
        <th>Loss</th>        
      </tr>
    </thead>
    <tbody>

<?php
$colors = array("success", "danger", "info");
$color_index = 0;

if ($result->num_rows > 0) {

     // output data of each row
	$row = $result->fetch_assoc();

    //echo '<tr class="'.$colors[$color_index].'">';
	echo '<tr>';
	//top record
    echo '<td>1</td>';
    echo '<td>'. $row["cu_date"].'</td>';
    echo '<td>'. number_format($row["open"], 2).'</td>';
    echo '<td>'. number_format($row["low"], 2).'</td>';
    echo '<td>'. number_format($row["high"], 2).'</td>';
    echo '<td>'. number_format($row["close"], 2).'</td>';
    echo '<td>'. number_format($row["Gain/Loss"], 2).'</td>';
    echo '<td>'. number_format($row["Gain"], 2).'</td>';
    echo '<td>'. number_format($row["Loss"], 2).'</td>';

    echo '</tr>';


	$prev_close = $row["close"];
	$index = 2;
	$gain_sum = 0;
	$loss_sum = 0;
	$num_samples = 0;

     // output data of each row
     while($row = $result->fetch_assoc()) {
		 $gain_loss = $row["close"] - $prev_close;
		//echo '<tr class="'.$colors[$index % 3].'">';
		echo '<tr>';
		echo '<td>'.$index.'</td>';

         echo "<td>" . $row["cu_date"]. "</td>";
		 echo "<td>" . number_format($row["open"], 2). "</td>";
		 echo "<td>" . number_format($row["low"], 2). "</td>";
		 echo "<td>" . number_format($row["high"],2). "</td>";
		 echo "<td>" . number_format($row["close"],2). "</td>";
		 echo "<td>" . number_format($gain_loss, 2). "</td>";
		 
		 if ($gain_loss > 0) {
			echo "<td><p class='text-success'>" . number_format($gain_loss, 2). "</p></td>";
			echo "<td>" . number_format(0.0, 2). "</td>";
			$gain_sum += $gain_loss;
		 } else {
			echo "<td>" . number_format(0.0, 2). "</td>";
			echo "<td><p class='text-danger'>" . number_format(-$gain_loss, 2). "</p></td>";
			$loss_sum += -$gain_loss;
		 }
		 //echo "<td>" . number_format($trade_value_array[$row["cu_date"]], 2). "</td>";

		 echo "</tr>";
		 $index++;
		 $prev_close = $row["close"];
		 $num_samples++;
		 if ($num_samples == $num_of_samples) {
			 break;
		 }
     }

	$avg_gain = ($gain_sum / $num_of_samples);
	$avg_loss = ($loss_sum / $num_of_samples);
	if ($avg_loss == 0) {
		$RSi = 100;
	} else {
		//RSIt = ( UT / (UT + DT) ) * 100
		$RS  = $avg_gain / $avg_loss;
		//=if(I17=0,100, 100-(100/(1+J17)))
		$Rsi_m2 = ($avg_gain / ($avg_gain + $avg_loss)) * 100;
		$Rsi = 100 -(100 /( 1 + $RS));
	}
	$row = $result->fetch_assoc();

	echo '<tr class = "info">';
	echo '<td></td>';
	echo '<td></td>';
	echo '<td>M2:'.number_format($Rsi_m2,2).'</td>';
	echo '<td>Avg Gain:</td>';
	echo '<td>'. number_format($avg_gain, 2).'</td>';
	echo '<td>Avg Loss:</td>';
	echo '<td>'. number_format($avg_loss, 2).'</td>';
	echo '<td>RSi :</td>';
	echo '<td>'. number_format($Rsi, 2).'</td>';
	echo "</tr>";

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