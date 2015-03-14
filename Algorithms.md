# Introduction #

This page has information/links to different algorithms.

# Orientation Sensor Algorithm #

The algorithm for determining the orientation with all of the crazy calculations to compensate for accelerometer and gyroscope propagation errors is known as the Mayhony's DCM filter. You can find the paper at http://code.google.com/p/imumargalgorithm30042010sohm/. However, a guy implemented this same algorithm on the Arduino with the same sensors we have (different board but same sensors). He created a board called freeIMU and he has the Arduino source at http://www.varesano.net/topic/freeimu.

# PID Controller #

This is likely the type of algorithm that will be used for balancing the quadcopter. http://en.wikipedia.org/wiki/PID_controller