<html>
<head>
<meta content="text/html; charset=utf-8" http-equiv="Content-Type" />

<!--Load the AJAX API-->
<script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
<script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>



<?php
require_once('myconfig.php'); 

$symbol = "aapl";
if (isset($_POST['symbol'])) {
	$symbol = $_POST["symbol"];
}
$type   = 'p';






?>
    



<script type="text/javascript">

    // Load the Visualization API and the piechart package.
    google.charts.load('current', {'packages':['corechart']});
      
    // Set a callback to run when the Google Visualization API is loaded.
    google.charts.setOnLoadCallback(drawpriceChart);
    
    <?php

	$symbol = "aapl";
	if (isset($_POST['symbol'])) {
		$symbol = $_POST["symbol"];
	}

	$type   = 'p';
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

		   refreshInterval:5,

           colors: ['green'],
          width: 1500,
          height: 250
        }; 
          
      // Create our data table out of JSON data loaded from server.
      var data = new google.visualization.DataTable(jsonData);

      // Instantiate and draw our chart, passing in some options.
      var chart = new google.visualization.LineChart(document.getElementById('chart_div'));
      chart.draw(data, options);
      
      <?php $type = 'r'; ?>
      
      var rsijsonData = $.ajax({
          url: "getData.php?symbol=<?php echo $symbol.'&type='.$type;?>",
          dataType: "json",
          async: false
          }).responseText;
    
      var rsioptions = {
           title: '<?php echo $symbol; ?> RSI(14)',
           legend: 'none',
           lineWidth: 1,
           colors: ['purple'],
           is3D: 'false',
          width: 1500,
          height: 250
        };     
        
     
      // Create our data table out of JSON data loaded from server.
      var rsidata = new google.visualization.DataTable(rsijsonData);
      
      // Instantiate and draw our chart, passing in some options.
      var rsichart = new google.visualization.LineChart(document.getElementById('rsichart_div'));
      rsichart.draw(rsidata, rsioptions);
      setTimeout(drawpriceChart, 20000); 
    }
    </script>
<title><?php echo $symbol; ?> Activity Live</title>
<style type="text/css">
.auto-style1 {
	text-align: right;
}
.auto-style11 {
	text-align: center;
}
.auto-style4 {
	font-size: large;
	background-color: #E3E1F1;
}
.auto-style10 {
	text-align: left;
	background-color: #E3E1F1;
}
.auto-style13 {
	color: #BFB5F3;
	background-color: #E3E1F1;
}
.auto-style14 {
	color: #000000;
	background-color: #C1B7F1;
}
.auto-style15 {
	background-color: #E3E1F1;
}
.auto-style18 {
	text-align: right;
	background-color: #E3E1F1;
}
</style>
</head>

<body>
<form action="todays_activity.php" method="post">
<div style="width: 1626px; height: 903px">
<table style="width: 83%; height: 33px" cellpadding="0" cellspacing="0" class="auto-style15">
					<tr>
						<td class="auto-style10" style="width: 993px; height: 36px;">
						<a href="google.com"><?php echo $symbol; ?> (Apple Inc.) </a> </td>
						<td class="auto-style15" style="width: 745px; height: 36px">
						<div style="height: 33px; width: 747px;" class="auto-style13">
							<table align="left" cellspacing="1" style="width: 100%; height: 35px" class="auto-style14">
								<tr class="auto-style11">
									<td style="height: 20px; width: 61px" class="auto-style18">
	<label class="auto-style1"><span class="auto-style4">Quote</span></label></td>
									<td class="auto-style10" style="height: 20px; width: 94px">
									<label class="auto-style1">
						<?php echo "<input type='text' name='symbol' style='width: 76px; height: 25px;' size = 6 value ='".$symbol."'>"; ?>

									</td>
									<td class="auto-style18" style="height: 20px; width: 52px">
									Date </td>
									<td class="auto-style10" style="height: 20px; width: 86px">
									<label class="auto-style1">
						<input name="date" style="width: 86px; height: 25px;" type="text" size="6" value="2016/10/03" /></label></td>
									<td class="auto-style18" style="height: 20px; width: 75px">
									Start Time</td>
									<td class="auto-style10" style="height: 20px; width: 101px">
									<label class="auto-style1">
						<input name="start_time" style="width: 53px; height: 25px;" type="text" size="6" value="6:30" /></label></td>
									<td class="auto-style18" style="height: 20px; width: 88px">
									End <span class="auto-style15">Time</span></td>
									<td class="auto-style10" style="height: 20px; width: 70px">
									<label class="auto-style1">
						<input name="end_time" style="width: 76px; height: 25px;" type="text" size="6" value="13:10" /></label></td>
									<td class="auto-style10" style="height: 20px; width: 32px">
									<input name="QuoteSubmit" style="height: 26px; width: 38px;" type="submit" value="Go" /></td>
								</tr>
							</table>
						
						</div>
						</td>
					</tr>
					</table>


<table style="width: 100%; height: 186px;">
					<tr>
						<td style="width: 139px; height: 150px;"></td>
						<td style="width: 534px; height: 150px;">
						<div id="price-panel" class="id-price-panel goog-inline-block" style="border-style: none; border-color: inherit; border-width: 0px; margin: 0px 18px 0px 0px; padding: 0px 12px 0px 0px; position: relative; display: table-cell; vertical-align: top; color: rgb(0, 0, 0); font-family: Arial, sans-serif; font-size: 13px; font-style: normal; font-variant-ligatures: normal; font-variant-caps: normal; font-weight: normal; letter-spacing: normal; orphans: 2; text-align: left; text-indent: 0px; text-transform: none; white-space: nowrap; widows: 2; word-spacing: 0px; -webkit-text-stroke-width: 0px; background-color: rgb(255, 255, 255); left: 0px; top: 0px; width: 324px; height: 82px;">
							<div class="auto-style19" style="margin: 0px; padding: 0px; border: 0px;">
								<span class="pr" style="font-size: 2.6em; font-weight: bold;">
								<span id="ref_690507_l" class="unchanged" style="color: rgb(0, 0, 0);">
								<span class="unchanged" style="color: rgb(0, 0, 0);">
								48.14</span></span><span class="Apple-converted-space">&nbsp;</span></span><div class="auto-style19" style="margin: 0px; padding: 0px; border: 0px; position: relative; display: inline-block; white-space: nowrap;">
									<span class="ch bld" style="font-weight: normal; font-size: 1.8em; vertical-align: bottom;">
									<span id="ref_690507_c" class="up" style="color: rgb(0, 153, 51);">
									+3.54</span><span class="Apple-converted-space">&nbsp;</span><span id="ref_690507_cp" class="up" style="color: rgb(0, 153, 51);">(7.94%)</span></span></div>
							</div>
							<div class="auto-style11" style="margin: 0px; padding: 0px; border: 0px;">
								<div id="ref_690507_elt" style="margin: 0px; padding: 0px; border: 0px;">
									<div class="auto-style19">
										Oct 3, 4:29PM PST&nbsp;&nbsp;</div>
									<div class="auto-style19" style="margin: 0px; padding: 0px; border: 0px; color: rgb(111, 111, 111); font-size: 0.85em;">
										<div class="auto-style19">
											<span class="dis-large"><nobr>Yahoo 
											real-time data </nobr></span></div>
									</div>
								</div>
							</div>
						</div>
						<div class="snap-panel-and-plusone" style="margin: 0px; padding: 0px; border: 0px; display: table-cell; vertical-align: top; white-space: normal; color: rgb(0, 0, 0); font-family: Arial, sans-serif; font-size: 13px; font-style: normal; font-variant-ligatures: normal; font-variant-caps: normal; font-weight: normal; letter-spacing: normal; orphans: 2; text-align: left; text-indent: 0px; text-transform: none; widows: 2; word-spacing: 0px; -webkit-text-stroke-width: 0px; background-color: rgb(255, 255, 255);">
							<div class="snap-panel" style="border-style: none; border-color: inherit; border-width: 0px; margin: 0px; padding: 0px; display: inline-block; vertical-align: top; white-space: nowrap; width: 332px; height: 96px;">
								<table class="auto-style20" style="border-style: none; border-color: inherit; border-width: 0px; padding: 0px; border-collapse: collapse; empty-cells: show; font-size: 1em; display: inline-block; vertical-align: top; margin-left: 0px; margin-top: 0px; margin-bottom: 8px;">
									<tbody style="margin: 0px; padding: 0px; border: 0px;">
										<tr style="margin: 0px; padding: 0px; border: 0px;">
											<td aria-label="Range Price range (low - high) in the latest trading day." class="key" data-snapfield="range" style="border-style: none; border-color: inherit; border-width: 0px; margin: 0px; padding: 0px; color: rgb(102, 102, 102); white-space: nowrap; cursor: default; width: 103px;">
											Range</td>
											<td class="val" style="margin: 0px; padding: 0px; border: 0px; white-space: nowrap; text-align: right;">
											44.61 - 48.18</td>
										</tr>
										<tr style="margin: 0px; padding: 0px; border: 0px;">
											<td aria-label="52-week range Price range (low - high) in the last 52 weeks" class="key" data-snapfield="range_52week" style="border-style: none; border-color: inherit; border-width: 0px; margin: 0px; padding: 0px; color: rgb(102, 102, 102); white-space: nowrap; cursor: default; width: 103px;">
											52 week</td>
											<td class="val" style="margin: 0px; padding: 0px; border: 0px; white-space: nowrap; text-align: right;">
											28.01 - 48.46</td>
										</tr>
										<tr style="margin: 0px; padding: 0px; border: 0px;">
											<td aria-label="Open Opening price on the latest trading day." class="key" data-snapfield="open" style="border-style: none; border-color: inherit; border-width: 0px; margin: 0px; padding: 0px; color: rgb(102, 102, 102); white-space: nowrap; cursor: default; width: 103px;">
											Open</td>
											<td class="val" style="margin: 0px; padding: 0px; border: 0px; white-space: nowrap; text-align: right;">
											44.96</td>
										</tr>
										<tr style="margin: 0px; padding: 0px; border: 0px;">
											<td aria-label="Volume / average volume Volume is the number of shares traded on the latest trading day. The average volume is measured over 30 days." class="key" data-snapfield="vol_and_avg" style="border-style: none; border-color: inherit; border-width: 0px; margin: 0px; padding: 0px; color: rgb(102, 102, 102); white-space: nowrap; cursor: default; width: 103px;">
											Vol / Avg.</td>
											<td class="val" style="margin: 0px; padding: 0px; border: 0px; white-space: nowrap; text-align: right;">
											1.35M/326,908.00</td>
										</tr>
										<tr style="margin: 0px; padding: 0px; border: 0px;">
											<td aria-label="Market capitalization The total value of a company in the stock market. It is generally calculated by multiplying the shares outstanding by the current share price." class="key" data-snapfield="market_cap" style="border-style: none; border-color: inherit; border-width: 0px; margin: 0px; padding: 0px; color: rgb(102, 102, 102); white-space: nowrap; cursor: default; width: 103px;">
											Mkt cap</td>
											<td class="val" style="margin: 0px; padding: 0px; border: 0px; white-space: nowrap; text-align: right;">
											2.02B</td>
										</tr>
										<tr style="margin: 0px; padding: 0px; border: 0px;">
											<td aria-label="Price to earnings ratio The share price divided by the earnings per share." class="key" data-snapfield="pe_ratio" style="border-style: none; border-color: inherit; border-width: 0px; margin: 0px; padding: 0px; color: rgb(102, 102, 102); white-space: nowrap; cursor: default; width: 103px;">
											P/E</td>
											<td class="val" style="margin: 0px; padding: 0px; border: 0px; white-space: nowrap; text-align: right;">
											32.61</td>
										</tr>
								</table>
								<table class="snap-data" style="margin: 0px 18px 8px 0px; padding: 0px; border: 0px; border-collapse: collapse; empty-cells: show; font-size: 1em; display: inline-block; vertical-align: top;">
									<tbody style="margin: 0px; padding: 0px; border: 0px;">
										<tr style="margin: 0px; padding: 0px; border: 0px;">
											<td aria-label="Latest dividend/dividend yield Latest dividend is dividend per share paid to shareholders in the most recent quarter. Dividend yield is the value of the latest dividend, multiplied by the number of times dividends are typically paid per year, divided by the stock price." class="key" data-snapfield="latest_dividend-dividend_yield" style="margin: 0px; padding: 0px; border: 0px; color: rgb(102, 102, 102); white-space: nowrap; cursor: default;">
											Div/yield</td>
											<td class="val" style="margin: 0px; padding: 0px; border: 0px; white-space: nowrap; text-align: right;">
											&nbsp;&nbsp;&nbsp;&nbsp;-</td>
										</tr>
										<tr style="margin: 0px; padding: 0px; border: 0px;">
											<td aria-label="Earnings per share The net income over the last four quarters divided by the shares outstanding." class="key" data-snapfield="eps" style="margin: 0px; padding: 0px; border: 0px; color: rgb(102, 102, 102); white-space: nowrap; cursor: default;">
											EPS</td>
											<td class="val" style="margin: 0px; padding: 0px; border: 0px; white-space: nowrap; text-align: right;">
											1.48</td>
										</tr>
										<tr style="margin: 0px; padding: 0px; border: 0px;">
											<td aria-label="Shares outstanding The number of shares held by investors and company insiders, excluding dilutive securities such as non-vested RSUs and unexercised options." class="key" data-snapfield="shares" style="margin: 0px; padding: 0px; border: 0px; color: rgb(102, 102, 102); white-space: nowrap; cursor: default;">
											Shares</td>
											<td class="val" style="margin: 0px; padding: 0px; border: 0px; white-space: nowrap; text-align: right;">
											45.23M</td>
										</tr>
										<tr style="margin: 0px; padding: 0px; border: 0px;">
											<td aria-label="Beta The measure of a fund's or a stock's risk in relation to the market or to an alternative benchmark." class="key" data-snapfield="beta" style="margin: 0px; padding: 0px; border: 0px; color: rgb(102, 102, 102); white-space: nowrap; cursor: default;">
											Beta</td>
											<td class="val" style="margin: 0px; padding: 0px; border: 0px; white-space: nowrap; text-align: right;">
											0.79</td>
										</tr>
										<tr style="margin: 0px; padding: 0px; border: 0px;">
											<td aria-label="Institutional ownership The percentage of shares outstanding held by institutional investors such as pension plans." class="key" data-snapfield="inst_own" style="margin: 0px; padding: 0px; border: 0px; color: rgb(102, 102, 102); white-space: nowrap; cursor: default;">
											Inst. own</td>
											<td class="val" style="margin: 0px; padding: 0px; border: 0px; white-space: nowrap; text-align: right;">
											110%</td>
										</tr>
								</table>
							</div>
						</div>
						</td>
						<td class="auto-style11" style="height: 150px">
						<div class="auto-style19">
							<strong>Todays News</strong></div>
						<div id="news_div_cont" class="id-news_div_cont" style="margin: 0px; padding: 0px; border: 0px; color: rgb(0, 0, 0); font-family: Arial, sans-serif; font-size: 13px; font-style: normal; font-variant-ligatures: normal; font-variant-caps: normal; font-weight: normal; letter-spacing: normal; orphans: 2; text-align: left; text-indent: 0px; text-transform: none; white-space: normal; widows: 2; word-spacing: 0px; -webkit-text-stroke-width: 0px; background-color: rgb(255, 255, 255);">
							<table border="0" cellpadding="0" cellspacing="0" style="border-style: none; border-color: inherit; border-width: 0px; margin: 0px; padding: 0px; border-collapse: collapse; empty-cells: show; font-size: 1em; width: 97%; height: 112px;">
								<tbody style="margin: 0px; padding: 0px; border: 0px;">
									<tr style="margin: 0px; padding: 0px; border: 0px;">
										<td id="scrollingListTd" style="margin: 0px; padding: 0px 0px 4px; border: 0px;">
										<div class="jfk-scrollbar" style="border-style: none; border-color: inherit; border-width: 0px; margin: 0px; padding: 0px; position: relative; overflow: auto; width: 835px; height: 68px; left: 0px; top: 0px;">
											<div class="news-item" style="margin: 0px; padding: 0px 0px 6px; border: 0px; background: rgb(255, 255, 255);">
												<div class="pin" style="margin: 0px; padding: 0px; border: 0px; display: block; width: 20px; float: left; position: relative;">
													<div class="g-c" style="margin: 0px; padding: 0px; border: 0px;">
														<img id="pinA" align="absmiddle" class="SP_pinA" height="16" src="https://www.google.com/finance/images/cleardot.gif" style="border-style: none; border-color: inherit; border-width: 0px; margin: 0px; padding: 0px; background: url('https://www.google.com/finance/images/sp11.png') no-repeat -34px -68px; width: 19px; height: 20px; cursor: pointer;" width="16" /></div>
												</div>
												<div class="cluster" style="margin: 0px 0px 0px 25px; padding: 0px; border: 0px; display: block; width: auto; float: none;">
													<div class="g-c" style="margin: 0px; padding: 0px; border: 0px;">
														<a id="n-c-" class="title" href="http://news.google.com/news/url?sa=T&amp;ct2=us&amp;fd=S&amp;url=https://www.equities.com/news/cardtronics-plc-catm-jumps-7-94-on-october-03&amp;cid=52779231766379&amp;ei=OfHyV4B9iLCMAsa5m4AP&amp;usg=AFQjCNG89tHnjCVwUaswQZgCSQNIC3rtnQ" style="color: rgb(17, 85, 204); outline: none; font-family: Arial, sans-serif; font-weight: normal; text-decoration: none;" target="">
														Cardtronics plc (CATM) 
														Jumps 7.94% on October 
														03</a><div style="margin: 0px; padding: 0px; border: 0px;">
															<span class="source" style="color: rgb(102, 102, 102);">
															Equities.com -<span class="Apple-converted-space">&nbsp;</span></span><span class="date">2 
															hours ago</span></div>
													</div>
												</div>
											</div>
										</div>
										</td>
									</tr>
							</table>
						</div>
						</td>
					</tr>
				</table>







				<table style="width: 100%; height: 590px">
					<tr>
						<td>
						<div id="chart_div" style="height: 240px"></div>
						</td>
					</tr>
					<tr>
						<td style="height: 289px">
						<div id="rsichart_div" style="height: 240px">
							</div>
						</td>
					</tr>
				</table>
				<br />
				Bootm section. for NEw about symbols RSS</div>
</form>
</body>

</html>
