var saveImage = false;
var stopPosts;
var unique = '';
var customHost = window.location;
var armed = false;
var getImages = false;
var sensitivityFactor = 15;
var AcrobaticMode = 1;
var up_down = false;
var down_down = false;
var left_down = false;
var right_down = false;
var ROLL_TRIM = -20;
var PITCH_TRIM = -15;

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
	
	$('#inputLoadPage').click(function() {
		$('#lblCurrentIP').html(document.domain);
		
		$('#divStartUp').css('display', 'none');
		$('#divMainContent').css('display', 'block');
	});
	
	$('#inputSubmitIP').click(function() {
		var customIP = $('#customIP').val();
		customHost = 'http://' + customIP + ':8080/';
		$('#lblCurrentIP').html('<a href=' + customHost + ' target="blank">' + customIP + '</a>');		
		
		$('#divStartUp').css('display', 'none');
		$('#divMainContent').css('display', 'block');
	});

	$('#inputSave').click(function() {
		saveImage = true;
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
		}
		else if($('#holdThrottleValue').val() < 1100)
		{
			armed = false;
			//--sendControls in case they are not firing off
			sendControls();
		}
		else
		{
			alert('Decrease throttle before disarm');
		}
	});
	
	var optionsHTML;
	var optionsElement = $('#inputControlSensivity');
	for(var i=5; i<= 25; i = i+5)
	{
		optionsHTML += '<option>' + i + '</option>';
	}
	optionsElement.html(optionsHTML);
	//--set default value
	optionsElement.val('15').attr('selected', true);
	
	optionsElement.change(function() {
		sensitivityFactor = parseInt($("#inputControlSensivity option:selected").text());
	});
	
	$('#submitKillSwitch').click(killEngines);
	
	setupControls('Roll');
	setupControls('Pitch');
	setupControls('Yaw');
	
	$('#divThrottleSlider').slider({
		range: 'min',
		orientation: 'vertical',
		value:0,
		min: 0,
		max: 100,
		step: 1,
		slide: function(event, ui) {
			$('#lblThrottleValue').html(ui.value);
		},
		change: function(event, ui) {
			$('#lblThrottleValue').html(ui.value);
			$('#holdThrottleValue').val(ui.value * 10 + 1000);
			if(armed)
				sendControls();
		}
	});
	$('#lblThrottleValue').html($('#divThrottleSlider').slider('value'));
	$('#holdThrottleValue').val($('#divThrottleSlider').slider('value')*10 + 1000);
	
	$('#submitSetThrottle').click(function() {
		$(this).next().val($('#divThrottleSlider').slider('value')*10 + 1000);
	});
	
	$('#submitResetInputs').click(function() {
		$('#divControls div').each(function() {
			$(this).slider('value', 500);
		});
	});
	
	$('#submitCalibrate').click(function() {
		postDataToAndroid('{Calibrate: 1}');
	});
	
	$('#submitFlyingMode').click(function() {
		if(AcrobaticMode == 1)
		{
			AcrobaticMode = 0;
			postDataToAndroid('{AcrobaticMode: 0}');
			$(this).val('Switch to Acrobatic Mode');
		}
		else
		{
			AcrobaticMode = 1;
			postDataToAndroid('{AcrobaticMode: 1}');
			$(this).val('Switch to Stable Mode');
		}
	});
	
	$('input:submit, a').button();
	
	$('#inputSubmitPost').click(function() {
		var dateSent = new Date();
		$.post(customHost + 'EchoServlet', {PostText: $('#inputPost').val()},
		function(response){
			alert(response);
		})
		.error(function(){
			alert('error');
		})
	});
	
	//--bind hotkeys
	$(document).bind('keydown', 'backspace', function (evt){
		killEngines();
		return false; 
	});
	$(document).bind('keydown', 'del', function (evt){
		killEngines();
		return false; 
	});
	$(document).bind('keydown', 'esc', function (evt){
		killEngines();
		return false; 
	});
	$(document).bind('keydown', 'space', function (evt){
		killEngines();
		return false; 
	});
	$(document).bind('keydown', 'Shift', function (evt){
		var throttleSlider = $('#divThrottleSlider');
		var currentValue = throttleSlider.slider('value');
		throttleSlider.slider('value', currentValue+1);
		return false;
	});
	$(document).bind('keydown', 'Ctrl', function (evt){
		var throttleSlider = $('#divThrottleSlider');
		var currentValue = throttleSlider.slider('value');
		throttleSlider.slider('value', currentValue-1);
		return false;
	});
	
	//--Instant Controls
	$(document).bind('keydown', 'up', function (evt){
		if(!up_down)
		{
			var pitchSlider = $('#divPitchSlider');
			var currentValue = pitchSlider.slider('value');
			pitchSlider.slider('value', 500 + sensitivityFactor);
		}
		up_down = true;
		return false;
	});
	$(document).bind('keyup', 'up', function (evt){
		up_down = false;
		$('#divPitchSlider').slider('value', 500 );
		return false;
	});
	$(document).bind('keydown', 'down', function (evt){
		if(!down_down)
		{
			var pitchSlider = $('#divPitchSlider');
			var currentValue = pitchSlider.slider('value');
			pitchSlider.slider('value', 500 - sensitivityFactor);
		}
		down_down = true;
		return false;
	});
	$(document).bind('keyup', 'down', function (evt){
		down_down = false;
		$('#divPitchSlider').slider('value', 500);
		return false;
	});
	$(document).bind('keydown', 'left', function (evt){
		if(!left_down)
		{
			var rollSlider = $('#divRollSlider');
			var currentValue = rollSlider.slider('value');
			rollSlider.slider('value', 500 - sensitivityFactor);
		}
		left_down = true;
		return false;
	});
	$(document).bind('keyup', 'left', function (evt){
		left_down = false;
		$('#divRollSlider').slider('value', 500);
		return false;
	});
	$(document).bind('keydown', 'right', function (evt){
		if(!right_down)
		{
			var rollSlider = $('#divRollSlider');
			var currentValue = rollSlider.slider('value');
			rollSlider.slider('value', 500 + sensitivityFactor);
		}
		right_down = true;
		return false;
	});
	$(document).bind('keyup', 'right', function (evt){
		right_down = false;
		$('#divRollSlider').slider('value', 500);
		return false;
	});
	
});

function killEngines()
{
	//--disarm
	armed = false;
	//--fire off sendControls (in case it is not already running)
	sendControls();
}

function sendControls()
{
	if(armed)
	{
		//--get current values from page and add trim
		var roll = parseInt($('#holdRollValue').val()) + ROLL_TRIM;
		var pitch = parseInt($('#holdPitchValue').val()) + PITCH_TRIM;
		var json = '{Roll: ' + roll + ',Pitch: ' + pitch + ',Yaw: ' + $('#holdYawValue').val() + ',Throttle: ' + $('#holdThrottleValue').val() + '}';
		//--send to android
		postDataToAndroid(json);
	}
	else
	{
		//--shut off engines (if from kill switch)
		var json = '{Roll: 1500, Pitch: 1500, Yaw: 1500, Throttle: 1000, Arm: 0}';
		//--shutoff throttle + disarm
		postDataToAndroid(json);
		//--reset values
		resetValues();
	}
};

function postDataToAndroid(data)
{
	$.ajax({
		type: 'POST',
		url: customHost + 'ControlReceiverServlet',
		data: data,
		success: function(returnData) {
			//--nothing for now
		}
	})
	.error(function() {
		if(armed)
		{
			$('#lblControlStatus').css('color', 'red');
			$('#lblControlStatus').html('Error!');
		}
	});
}

function resetValues()
{
	$('#divThrottleSlider').slider('value', 0);

	$('#divControls div').each(function() {
		$(this).slider('value', 500);
	});
	
	$('#submitArmDisarm').val('Arm');
	$('#lblControlStatus').css('color', 'black');
	$('#lblControlStatus').html('Offline');
}

function refreshImage()
{
	if(saveImage)
	{
		$('#divPhoto').append('<br>' + unique + '<br><div style="width: 480; height: 640; margin-top: 150px; margin-left: -50px"><img style="-webkit-transform: rotate(+90deg); -moz-transform: rotate(+90deg);" src="' + customHost + 'webcam.jpg?time=' + unique.getTime() + '"></div>');
		saveImage = false;
	}

	unique = new Date();
	$('#imgDynamic').attr('src', customHost + 'webcam.jpg?time=' + unique.getTime());
	if(getImages)
	{
		var t = setTimeout('refreshImage()', 1000);
	}
};

function refreshInfo()
{
	var sent = new Date();
	$.post(customHost + 'EchoServlet', {PostText: $('#inputPost').val()})
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
		range: 'min',
		value:500,
		min: 0,
		max: 1000,
		step: 5,
		slide: function(event, ui) {
			$('#lbl' +  controlName +  'Value').html(ui.value);
		},
		change: function(event, ui) {
			$('#lbl' +  controlName +  'Value').html(ui.value);
			$('#hold' +  controlName + 'Value').val(ui.value + 1000);
			if(armed)
				sendControls();
		}
	});
	$('#lbl' +  controlName + 'Value').html($('#div' + controlName + 'Slider').slider('value'));
	$('#hold' +  controlName + 'Value').val($('#div' + controlName + 'Slider').slider('value') + 1000);
};