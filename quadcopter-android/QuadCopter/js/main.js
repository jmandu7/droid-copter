var saveImage = false;
var stopPosts;
var unique = '';
var host = window.location;
var armed = false;
var getImages = false;
var sensitivityFactor = 5;

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
	for(var i=5; i<= 25; i = i+5)
	{
		optionsHTML += '<option>' + i + '</option>';
	}
	$('#inputControlSensivity').html(optionsHTML);
	
	$('#inputControlSensivity').change(function() {
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
		step: 5,
		slide: function(event, ui) {
			$('#lblThrottleValue').html(ui.value);
		},
		change: function(event, ui) {
			$('#lblThrottleValue').html(ui.value);
			$('#holdThrottleValue').val(ui.value * 10 + 1000);
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
	$(document).bind('keydown', 'Shift', function (evt){
		var throttleSlider = $('#divThrottleSlider');
		var currentValue = throttleSlider.slider('value');
		throttleSlider.slider('value', currentValue+5);
		return false;
	});
	$(document).bind('keydown', 'Ctrl', function (evt){
		var throttleSlider = $('#divThrottleSlider');
		var currentValue = throttleSlider.slider('value');
		throttleSlider.slider('value', currentValue-5);
		return false;
	});
	$(document).bind('keydown', 'up', function (evt){
		var pitchSlider = $('#divPitchSlider');
		var currentValue = pitchSlider.slider('value');
		pitchSlider.slider('value', currentValue+5);
		return false;
	});
	$(document).bind('keydown', 'down', function (evt){
		var pitchSlider = $('#divPitchSlider');
		var currentValue = pitchSlider.slider('value');
		pitchSlider.slider('value', currentValue-5);
		return false;
	});
	$(document).bind('keydown', 'left', function (evt){
		var rollSlider = $('#divRollSlider');
		var currentValue = rollSlider.slider('value');
		rollSlider.slider('value', currentValue-5);
		return false;
	});
	$(document).bind('keydown', 'right', function (evt){
		var rollSlider = $('#divRollSlider');
		var currentValue = rollSlider.slider('value');
		rollSlider.slider('value', currentValue+5);
		return false;
	});
	
	//--Instant Controls
	$(document).bind('keydown', 'w', function (evt){
		var pitchSlider = $('#divPitchSlider');
		var currentValue = pitchSlider.slider('value');
		pitchSlider.slider('value', 500 - sensitivityFactor);
		return false;
	});
	$(document).bind('keyup', 'w', function (evt){
		$('#divPitchSlider').slider('value', 500 );
		return false;
	});
	$(document).bind('keydown', 's', function (evt){
		var pitchSlider = $('#divPitchSlider');
		var currentValue = pitchSlider.slider('value');
		pitchSlider.slider('value', 500 + sensitivityFactor);
		return false;
	});
	$(document).bind('keyup', 's', function (evt){
		$('#divPitchSlider').slider('value', 500);
		return false;
	});
	$(document).bind('keydown', 'a', function (evt){
		var rollSlider = $('#divRollSlider');
		var currentValue = rollSlider.slider('value');
		rollSlider.slider('value', 500 - sensitivityFactor);
		return false;
	});
	$(document).bind('keyup', 'a', function (evt){
		$('#divRollSlider').slider('value', 500);
		return false;
	});
	$(document).bind('keydown', 'd', function (evt){
		var rollSlider = $('#divRollSlider');
		var currentValue = rollSlider.slider('value');
		rollSlider.slider('value', 500 + sensitivityFactor);
		return false;
	});
	$(document).bind('keyup', 'd', function (evt){
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
		//--get current values from page
		var json = '{Roll: ' + $('#holdRollValue').val() + ',Pitch: ' + $('#holdPitchValue').val() + ',Yaw: ' + $('#holdYawValue').val() + ',Throttle: ' + $('#holdThrottleValue').val() + '}';
		//--send to android
		postDataToAndroid(json);
		//--call sendControls again
		var q = setTimeout('sendControls()', 1000);
	}
	else
	{
		//--shut off engines (if from kill switch)
		$('#holdThrottleValue').val('1000');
		var json = '{Roll: 1500, Pitch: 1500, Yaw: 1500, Throttle: 1000}';
		//--shutoff throttle
		postDataToAndroid(json);
		//--disarm
		postDataToAndroid('{Arm: 0}');
		//--reset values
		resetValues();
	}
};

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
		}
	});
	$('#lbl' +  controlName + 'Value').html($('#div' + controlName + 'Slider').slider('value'));
	$('#hold' +  controlName + 'Value').val($('#div' + controlName + 'Slider').slider('value') + 1000);
};