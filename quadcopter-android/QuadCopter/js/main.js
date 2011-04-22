var saveImage = false;
var stopPosts;
var unique = '';
var customHost = window.location;
var armed = false;
var refreshMap = false;
var getImages = false;
var sensitivityFactor = 15;
var AcrobaticMode = 1;
var up_down = false;
var down_down = false;
var left_down = false;
var right_down = false;
var ROLL_TRIM = 0;
var PITCH_TRIM = 0;
var stepSizeThrottle = 1;
var googleMap;
var droidMarker;

$(document).ready(function(){
	$('#submitToggleDebug').toggle(function() {
		$(this).val('Hide Debug Section');
		$('#divDebug').slideDown(400);
	}, function() {
		stopPosts = true;
		$(this).val('Show Debug Section');
		$('#divDebug').slideUp(400);
	});
	
	$('#submitToggleMap').toggle(function() {
		$(this).val('Hide Map Section');	
		$('#divMapCanvas').show();
		refreshMap = true;	
		loadGoogleMaps();
	}, function() {
		refreshMap = false;
		$(this).val('Show Map Section');		
		$('#divMapCanvas').slideUp(400);
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
		
		showPage();
	});
	
	$('#inputSubmitIP').click(function() {
		var customIP = $('#customIP').val();
		customHost = 'http://' + customIP + ':8080/';
		$('#lblCurrentIP').html('<a href=' + customHost + ' target="blank">' + customIP + '</a>');		
		
		showPage();
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
		sensitivityFactor = parseInt($('#inputControlSensivity option:selected').text());
	});
	
	optionsElement = $('#selectThrottleStepSize');
	optionsHTML = '<option>1</option>';
	optionsHTML += '<option>5</option>';
	optionsHTML += '<option>10</option>';
	optionsElement.html(optionsHTML);
	
	optionsElement.change(function() {
		stepSizeThrottle = parseInt($('#selectThrottleStepSize option:selected').text());
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
	
	$('#addThrottle').click(increaseThrottle);
	$('#subtractThrottle').click(decreaseThrottle);
	
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
		increaseThrottle();
		return false;
	});
	$(document).bind('keydown', 'Ctrl', function (evt){
		decreaseThrottle();
		return false;
	});
	
	//--Instant Controls
	$(document).bind('keydown', 'up', function (evt){
		if(!up_down)
		{
			var pitchSlider = $('#divPitchSlider');
			var currentValue = pitchSlider.slider('value');
			pitchSlider.slider('value', currentValue + sensitivityFactor);
		}
		up_down = true;
		return false;
	});
	$(document).bind('keyup', 'up', function (evt){
		up_down = false;
		var pitchSlider = $('#divPitchSlider');
		var currentValue = pitchSlider.slider('value');
		$('#divPitchSlider').slider('value', currentValue - sensitivityFactor );
		return false;
	});
	$(document).bind('keydown', 'down', function (evt){
		if(!down_down)
		{
			var pitchSlider = $('#divPitchSlider');
			var currentValue = pitchSlider.slider('value');
			pitchSlider.slider('value', currentValue - sensitivityFactor);
		}
		down_down = true;
		return false;
	});
	$(document).bind('keyup', 'down', function (evt){
		down_down = false;
		var pitchSlider = $('#divPitchSlider');
		var currentValue = pitchSlider.slider('value');
		pitchSlider.slider('value', currentValue + sensitivityFactor);
		return false;
	});
	$(document).bind('keydown', 'left', function (evt){
		if(!left_down)
		{
			var rollSlider = $('#divRollSlider');
			var currentValue = rollSlider.slider('value');
			rollSlider.slider('value', currentValue - sensitivityFactor);
		}
		left_down = true;
		return false;
	});
	$(document).bind('keyup', 'left', function (evt){
		left_down = false;
		var rollSlider = $('#divRollSlider');
		var currentValue = rollSlider.slider('value');
		rollSlider.slider('value', currentValue + sensitivityFactor);
		return false;
	});
	$(document).bind('keydown', 'right', function (evt){
		if(!right_down)
		{
			var rollSlider = $('#divRollSlider');
			var currentValue = rollSlider.slider('value');
			rollSlider.slider('value', currentValue + sensitivityFactor);
		}
		right_down = true;
		return false;
	});
	$(document).bind('keyup', 'right', function (evt){
		right_down = false;
		var rollSlider = $('#divRollSlider');
		var currentValue = rollSlider.slider('value');
		rollSlider.slider('value', currentValue - sensitivityFactor);
		return false;
	});
	
});

function increaseThrottle()
{
	var throttleSlider = $('#divThrottleSlider');
	var currentValue = throttleSlider.slider('value');
	throttleSlider.slider('value', currentValue + stepSizeThrottle);
}

function decreaseThrottle()
{
	var throttleSlider = $('#divThrottleSlider');
	var currentValue = throttleSlider.slider('value');
	throttleSlider.slider('value', currentValue - stepSizeThrottle);
}

function changeThrottle()
{

}

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
		$('#divPhoto').append('<br>' + unique + '<br><div style="width: 640; height: 480;"><img src="' + customHost + 'webcam.jpg?time=' + unique.getTime() + '"></div>');
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
	var sliderDiv = $('#div' + controlName + 'Slider');

	sliderDiv.slider({
		range: 'min',
		value:500,
		min: 400,
		max: 600,
		step: 1,
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
	$('#lbl' +  controlName + 'Value').html(sliderDiv.slider('value'));
	$('#hold' +  controlName + 'Value').val(sliderDiv.slider('value') + 1000);
	
	$('#add' + controlName).click(function() {
		var currentValue = sliderDiv.slider('value');
		sliderDiv.slider('value', currentValue+1);
	});
	
	$('#subtract' + controlName).click(function() {
		var currentValue = sliderDiv.slider('value');
		sliderDiv.slider('value', currentValue-1);
	});
};

function showPage()
{
	$('#divStartUp').css('display', 'none');
	$('#divMainContent').css('display', 'block');
};

function loadGoogleMaps()
{
	//--dynamically add the goolge maps script to page
	var script = document.createElement('script');
	script.type = 'text/javascript';
	script.src = 'http://maps.google.com/maps/api/js?sensor=false&callback=initializeGoogleMaps';
	$('body').append(script);
};

function getLocationFromDroid()
{
	//--get gps data from the droid
	$.ajax({
		type: 'GET',
		url: customHost + 'GPSDataServlet',
		success: function(returnData) {
			var gpsJson = $.parseJSON(returnData);
			updateGoogleMaps(gpsJson.lat, gpsJson.lng);
		}
	});
	
	if(refreshMap)
	{		
		var t = setTimeout('getLocationFromDroid()', 1000);
	}
};

function initializeGoogleMaps() {
	//--hardcode initial startup position
	var myLatlng = new google.maps.LatLng(0,0);
	var myOptions = {
		zoom: 18,
		disableDefaultUI: true,
		draggable: false,
		scrollwheel: false,
		center: myLatlng,
		mapTypeId: google.maps.MapTypeId.ROADMAP
	}
	googleMap = new google.maps.Map(document.getElementById('divMapCanvas'), myOptions);

	//--add marker for droidcopter
	droidMarker = new google.maps.Marker({
		position: myLatlng,
		map: googleMap,
		title:'Droidcopter Location',
		icon: 'img/droid.png'
	});

	getLocationFromDroid();
};

function updateGoogleMaps(lat, lng)
{
	//--build LatLng object from coordinates
	var newLatLing = new google.maps.LatLng(lat, lng);
	googleMap.panTo(newLatLing);
	droidMarker.setPosition(newLatLing);
};