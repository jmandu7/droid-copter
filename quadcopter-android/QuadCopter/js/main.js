var saveImage = false;
var stopPosts;
var unique = '';
var host = window.location;
var armed = false;
var getImages = false;

$(document).ready(function(){
	$('#submitToggleDebug').toggle(function() {
		$(this).val('Hide Debug Section');
		$('#divDebug').slideDown(400);
	}, function() {
		stopPosts = true;
		$(this).val('Show Debug Section');
		$('#divDebug').slideUp(400);
	});
	
	$('#submitTogglePhoto').toggle(function() {
		$(this).val('Hide Photo Section');
		$('#divPhoto').slideDown(400);
		getImages = true;
		refreshImage();					
	}, function() {
		getImages = false;
		$(this).val('Show Photo Section');		
		$('#divPhoto').slideUp(400);
	});

	$('#inputSave').click(function() {
		saveImage = true;
	});
	
	$('#inputSubmitIP').click(function() {
		host = 'http://' + $('#customIP').val() + ':8080/';
	});
	
	$('#testPost').click(function() {
		stopPosts = false;
		refreshInfo();
	});
	
	$('#stopPost').click(function() {
		stopPosts = true;
	});
	
	$('#submitArmDisarm').click(function() {
		if(!armed)
		{
			postDataToAndroid('{Arm: 1}');
			$(this).val('Disarm');
			$('#lblControlStatus').css('color', 'green');
			$('#lblControlStatus').html('Live');
			armed = true;
			sendControls();
		}
		else if($('#holdThrottleValue').val() < 1100)
		{
			postDataToAndroid('{Arm: 0}');
			$(this).val('Arm');
			armed = false;
			$('#lblControlStatus').css('color', 'black');
			$('#lblControlStatus').html('Offline');
		}
		else
		{
			alert('Decrease throttle before disarm');
		}
	});
	
	$('#submitKillSwitch').click(killEngines);
	
	setupControls('Roll');
	setupControls('Pitch');
	setupControls('Yaw');
	
	$('#divThrottleSlider').slider({
		orientation: 'vertical',
		value:0,
		min: 0,
		max: 100,
		step: 5,
		slide: function(event, ui) {
			$('#inputThrottleValue').val(ui.value);
		}
	});
	$('#inputThrottleValue').val($('#divThrottleSlider').slider('value'));
	$('#holdThrottleValue').val($('#divThrottleSlider').slider('value')*10 + 1000);
	
	$('#submitSetThrottle').click(function() {
		$(this).next().val($('#divThrottleSlider').slider('value')*10 + 1000);
	});
	
	$('#submitHoldInputs').click(function() {
		$('#divControls div').each(function() {
			$(this).next().val($(this).slider('value') + 1000);
		});
	});
	
	$('#submitResetInputs').click(function() {
		$('#divControls div').each(function() {
			$(this).slider('value', 500);
			$(this).prev().val(500);
			$(this).next().val($(this).slider('value') + 1000);
		});
	});
	
	$('#submitCalibrate').click(function() {
		postDataToAndroid('{Calibrate: 1}');
	});
	
	$('#submitFlyingMode').toggle(function() {
		postDataToAndroid('{AcrobaticMode: 0}');
		$(this).val('Switch to Acrobatic Mode');
	},
	function() {
		postDataToAndroid('{AcrobaticMode: 1}');
		$(this).val('Switch to Stable Mode');
	});
	
	$('input:submit, a').button();
	
	$('#inputSubmitPost').click(function() {
		var dateSent = new Date();
		$.post(host + 'EchoServlet', {PostText: $('#inputPost').val()},
		function(response){
			alert(response);
		})
		.error(function(){
			alert('error');
		})
	});
});

function killEngines()
{
	armed = false;
	$('#holdThrottleValue').val('1000');
	var json = '{Roll: 1500, Pitch: 1500, Yaw: 1500, Throttle: 1000}';
	//--shutoff throttle
	postDataToAndroid(json);
	//--disarm
	postDataToAndroid('{Arm: 0}');
	//--reset labels
	$('#submitArmDisarm').val('Arm');
	$('#lblControlStatus').css('color', 'black');
	$('#lblControlStatus').html('Offline');
}

function sendControls()
{
	var json = '{Roll: ' + $('#holdRollValue').val() + ',Pitch: ' + $('#holdPitchValue').val() + ',Yaw: ' + $('#holdYawValue').val() + ',Throttle: ' + $('#holdThrottleValue').val() + '}';
	postDataToAndroid(json);

	if(armed)
	{
		var q = setTimeout('sendControls()', 1000);
	}
	else
	{
		//--make sure engines were killed (async function)
		killEngines();
	}
};

function postDataToAndroid(data)
{
	$.ajax({
		type: 'POST',
		url: 'ControlReceiverServlet',
		data: data,
	})
	.error(function() {
		if(armed)
		{
			$('#lblControlStatus').css('color', 'red');
			$('#lblControlStatus').html('Error!');
		}
	});
}

function refreshImage()
{
	if(saveImage)
	{
		$('#divPhoto').append('<br>' + unique + '<br><div style="width: 480; height: 640; margin-top: 150px; margin-left: -50px"><img style="-webkit-transform: rotate(+90deg); -moz-transform: rotate(+90deg);" src="' + host + 'webcam.jpg?time=' + unique.getTime() + '"></div>');
		saveImage = false;
	}

	unique = new Date();
	$('#imgDynamic').attr('src', host + 'webcam.jpg?time=' + unique.getTime());
	if(getImages)
	{
		var t = setTimeout('refreshImage()', 1000);
	}
};

function refreshInfo()
{
	var sent = new Date();
	$.post(host + 'EchoServlet', {PostText: $('#inputPost').val()})
	.success(function(){
		$('#recordedTimes').append('<td style="border: inset 1px;">' + (new Date() - sent) + '</td>');
		if(!stopPosts)
			var s = setTimeout('refreshInfo()', $('#querySeconds').val()*1000);					
	});				
					
	var count = 0;
	var total = 0;
	$('td').each(function() {
		count += parseInt($(this).html());
		total++;
	});
	var avg = count/total;
	
	$('#averageTime').html('Average Response Time: ' + avg);
};

function setupControls(controlName)
{
	$('#div' + controlName + 'Slider').slider({
		value:500,
		min: 0,
		max: 1000,
		step: 100,
		slide: function(event, ui) {
			$('#input' +  controlName +  'Value').val(ui.value);
		}
	});
	$('#input' +  controlName + 'Value').val($('#div' + controlName + 'Slider').slider('value'));
	$('#hold' +  controlName + 'Value').val($('#div' + controlName + 'Slider').slider('value') + 1000);
};