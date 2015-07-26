We have built a four wheeled robot with an Arduino microntroller, specifically the Arduino Mega 2650. We have written an Arduino and Android libraries to allow an Android device to control the robot through a USB connection. The robot is designed to track objects by spinning left and right to keep the object in sight and driving forward and backward to maintain a constant distance between the robot and the object. Images are acquired through the camera of an Android device which is attached to the robot. The camera is attached to servos on the robot which allow the camera to pan and tilt. Several image processing techniques are used to detect the location of the object being tracked in the images. In our project there are two Android applications. One of them uses color based tracking methods and the other uses a template based tracking method. Both applications use Android's OpenCV library to help with the image processing.

Included in this project are 2 Arduino Projects and 5 Java Projects.

Arduino Projects:
1. Arduino Robot Code -
This is a single file code that can be uploaded to an Arduino microcontroller. It listens for commands from an attached Android device.

2. PololuWheelEncoders2 -
This is a modification of the PololuWheelEncoder library. This uses a long instead of an int for keeping track of how far the robot has traveled.

Java Projects:
1. OpenCV-2.4.2-android-sdk -
This is Android's OpenCV library, it is required for the image processing in some of the other projects.

2. Robot Controller -
This is a library that allows an Android device to communicate with an Arduino robot through a USB cable using Android's open accessory mode.

3. Video Processing -
This is a library which contains several classes for processing frames from the Android's camera and identifying and tracking an object.

Currently there are two frame processors: one which uses color tracking techniques and another which uses template tracking techniques.

4. Object Tracker -
This is an Android application which contains two activities that track object using the above video processing techniques (color based and template based).

5. Robot Functionality Test -
This is a simple Android activity which tests the basic operations of the robot such as driving forward and backward, spinning, and moving the attached camera.