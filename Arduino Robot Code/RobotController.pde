#include <Usb.h>
#include <AndroidAccessory.h>
#include <PololuWheelEncoders2.h>
#include <Servo.h>

AndroidAccessory acc("Manufacturer", "Model", "Description", "Version", "URI", "Serial");
byte RcvMsg[5];
byte SntMsg[5];

Servo rightSideMotors;
Servo leftSideMotors;
PololuWheelEncoders2 encoders;
const int MIN_SPEED = 40;
const int MID_SPEED = 90;
const int MAX_SPEED = 140;
const int RIGHT_ADV_THRES = 100;

const int WHEEL_CIR = 36;  //wheel circumference in cm
const int BAUD_RATE = 9600;

const int LAS_HOR_PIN = 2; //blue
const int LAS_VERT_PIN = 3; //white
const int CAM_HOR_PIN = 4;
const int CAM_VERT_PIN = 5;

const int FRONT_SONAR = 8;
const int REAR_SONAR = 9;

const int SPEED_CONT_PIN = 14;
const int DIR_CONT_PIN = 6;

int LoopCount = 0;

Servo LaserHor;
Servo CameraHor;
Servo CameraVert;
Servo LaserVert;

int RightSideAdv = 0;
long LeftSideStop = 0;
long RightSideStop = 0;
int LeftSideSpeed = 0;
int RightSideSpeed = 0;

const int DELAY = 1;
const int READ_SONAR_FREQ = 10;
const int BROADCAST_SONAR_FREQ = 10;
const int SONAR_SAMPLES = 30;
const int SONAR_TAKE_ACTION_THRESHOLD = 50;
const int SYNCH_MOTORS_FREQ = 100;

bool HeedSonar = true;
bool Synchronize = false;
bool WatchForStop = true;

bool TEST_CYCLE = false;
int TEST_COUNT = 0;

enum Commands { CmdMoveForward, CmdMoveForwardDistance, CmdMoveBackward, CmdMoveBackwardDistance, CmdSpinLeft, CmdSpinLeftDistance, CmdSpinRight, CmdSpinRightDistance, CmdStop, CmdMoveCameraVert, CmdMoveCameraHor };
enum Events { EvtFrontSonar, EvtRearSonar, EvtSonarStop, EvtDistanceStop };

void setup () {
	Serial.begin(BAUD_RATE);

	//LaserHor.attach(LAS_HOR_PIN);
	LaserVert.attach(LAS_VERT_PIN);
	CameraHor.attach(CAM_HOR_PIN);
	CameraVert.attach(CAM_VERT_PIN);

	//LaserHor.write(90);
	LaserVert.write(90);
	CameraHor.write(90);
	CameraVert.write(90);

	rightSideMotors.attach(SPEED_CONT_PIN);
	leftSideMotors.attach(DIR_CONT_PIN);
	
	pinMode(FRONT_SONAR, INPUT);
	pinMode(REAR_SONAR, INPUT);

	encoders.init(10, 11, 13, 12);

	MoveForward(50, 1000);
	acc.powerOn();
}

void TestCycle() {
	/*Serial.println("STOPPING:");
	Serial.println(LeftSideStop);
	Serial.println(RightSideStop);
	Serial.println(abs(encoders.getCountsLeft()) - LeftSideStop);
	Serial.println(abs(encoders.getCountsRight()) - RightSideStop);*/
	TEST_COUNT++;
	if (TEST_COUNT == 1) {
		CameraHor.write(45);
		delay(150);
		CameraHor.write(0);
		delay(1000);
		CameraHor.write(45);
		delay(150);
		CameraHor.write(135);
		delay(150);
		CameraHor.write(180);
		delay(1000);
		CameraHor.write(135);
		delay(150);
		CameraHor.write(90);
		delay(150);
		SpinRight(50, 180);
	}
	if (TEST_COUNT == 2) {
		CameraVert.write(45);
		delay(500);
		CameraVert.write(135);
		delay(500);
		CameraVert.write(90);
		delay(500);
		MoveForward(90, 1000);
	}
	if (TEST_COUNT == 3) {
		CameraVert.write(130);
	}
}

void loop() {
	if (acc.isConnected()) {
		LaserVert.write(130);
		ReadIncomingSignal();
		if (WatchForStop) {
			NormalizeMotors();
		}
		if (HeedSonar) {
			UpdateLoopCount();
			if (LoopCount % READ_SONAR_FREQ == 0) {
				ReadSonar();
			}
	
			if (LoopCount % SYNCH_MOTORS_FREQ == 0) {
				SynchronizeMotors();
			}
			delay(DELAY);
		}
	}
	else {
		LaserVert.write(90);
	}
}

void SendToAndroid(byte signal, unsigned long value) {
	//Serial.print("Sending: ");
	//Serial.println(signal);
	SntMsg[0] = signal;
	SntMsg[1] = value >> 24;
	SntMsg[2] = value >> 16;
	SntMsg[3] = value >> 8;
	SntMsg[4] = value;

	acc.write(SntMsg, 5);
}

void ReadIncomingSignal() {
	int len = acc.read(RcvMsg, 9, 1);

	while (len > 0) {
		switch(RcvMsg[0]){
			case CmdMoveForward:
				{
					int speed = getIntFromBytes(RcvMsg[1], RcvMsg[2], RcvMsg[3], RcvMsg[4]);
					MoveForward(speed);
					break;
				}
			case CmdMoveForwardDistance:
				{
					int speed = getIntFromBytes(RcvMsg[1], RcvMsg[2], RcvMsg[3], RcvMsg[4]);
					int distance = getIntFromBytes(RcvMsg[5], RcvMsg[6], RcvMsg[7], RcvMsg[8]);
					MoveForward(speed, distance);
					break;
				}
			case CmdMoveBackward:
				{
					int speed = getIntFromBytes(RcvMsg[1], RcvMsg[2], RcvMsg[3], RcvMsg[4]);
					MoveBackward(speed);
					break;
				}
			case CmdMoveBackwardDistance:
				{
					int speed = getIntFromBytes(RcvMsg[1], RcvMsg[2], RcvMsg[3], RcvMsg[4]);
					int distance = getIntFromBytes(RcvMsg[5], RcvMsg[6], RcvMsg[7], RcvMsg[8]);
					MoveBackward(speed, distance);
					break;
				}
			case CmdSpinLeft:
				{
					int speed = getIntFromBytes(RcvMsg[1], RcvMsg[2], RcvMsg[3], RcvMsg[4]);
					SpinLeft(speed);
					break;
				}
			case CmdSpinLeftDistance:
				{
					int speed = getIntFromBytes(RcvMsg[1], RcvMsg[2], RcvMsg[3], RcvMsg[4]);
					int distance = getIntFromBytes(RcvMsg[5], RcvMsg[6], RcvMsg[7], RcvMsg[8]);
					SpinLeft(speed, distance);
					break;
				}
			case CmdSpinRight:
				{
					int speed = getIntFromBytes(RcvMsg[1], RcvMsg[2], RcvMsg[3], RcvMsg[4]);
					SpinRight(speed);
					break;
				}
			case CmdSpinRightDistance:
				{
					int speed = getIntFromBytes(RcvMsg[1], RcvMsg[2], RcvMsg[3], RcvMsg[4]);
					int distance = getIntFromBytes(RcvMsg[5], RcvMsg[6], RcvMsg[7], RcvMsg[8]);
					SpinRight(speed, distance);
					break;
				}
			case CmdStop:
				{
					Stop();
					break;
				}
			case CmdMoveCameraVert:
				{
					int degrees = getIntFromBytes(RcvMsg[1], RcvMsg[2], RcvMsg[3], RcvMsg[4]);
					MoveCameraVert(degrees);
					break;
				}
			case CmdMoveCameraHor:
				{
					int degrees = getIntFromBytes(RcvMsg[1], RcvMsg[2], RcvMsg[3], RcvMsg[4]);
					MoveCameraHor(degrees);
					break;
				}
		}
		len = acc.read(RcvMsg, 9, 1);
    }
}

int getIntFromBytes(byte b1, byte b2, byte b3, byte b4)
{
	int value = 0;
	value += b1;
	value <<= 8;
	value += b2;
	value <<= 8;
	value += b3;
	value <<= 8;
	value += b4;
	return value;
}

void UpdateLoopCount() {
	LoopCount = (LoopCount % 1000) + 1;
}

void ReadSonar() {
	unsigned long frontSum = 0;
	unsigned long rearSum = 0;
	for (int i = 0; i < SONAR_SAMPLES; i++) {
		unsigned long frontDuration = pulseIn(FRONT_SONAR, HIGH);
		unsigned long rearDuration = pulseIn(REAR_SONAR, HIGH);
		frontSum += frontDuration / 147.0 * 2.54;
		rearSum += rearDuration / 147.0 * 2.54;
	}
	
	unsigned long frontAvg = frontSum / SONAR_SAMPLES;
	unsigned long rearAvg = rearSum / SONAR_SAMPLES;
	bool forward = LeftSideSpeed > MID_SPEED || RightSideSpeed > MID_SPEED;

	if (HeedSonar && ((forward && frontAvg <= SONAR_TAKE_ACTION_THRESHOLD) || (!forward && rearAvg <= SONAR_TAKE_ACTION_THRESHOLD))) {
		Stop();
	}

	Serial.println(LoopCount);
	Serial.println(frontAvg);
	if (LoopCount % BROADCAST_SONAR_FREQ == 0) {
		SendToAndroid(EvtFrontSonar, frontAvg);
		SendToAndroid(EvtRearSonar, rearAvg);
	}
}

void MoveForward(int speed) {
	SetMotors(speed, speed, 0, 0, true, true, false);
}

void MoveForward(int speed, int cms) {
	SetMotors(speed, speed, cms, cms, true, true, true);
}

void MoveBackward(int speed) {
	SetMotors(-speed, -speed, 0, 0, true, true, false);
}

void MoveBackward(int speed, int cms) {
	SetMotors(-speed, -speed, cms, cms, true, true, true);
}

void SpinLeft(int speed) {
	SetMotors(-speed, speed, 0, 0, true, false, false);
}

void SpinLeft(int speed, int degrees) {
	int cms = (degrees / 3.0);
	int extra = ((cms - 30) / 30.0);
	cms += extra;
	SetMotors(-speed, speed, cms, cms, true, false, true);
}

void SpinRight(int speed) {
	SetMotors(speed, -speed, 0, 0, true, false, false);
}

void SpinRight(int speed, int degrees) {
	int cms = (degrees / 3.0);
	int extra = ((cms - 30) / 30.0) * 2.0;
	cms += extra;
	SetMotors(speed, -speed, cms, cms, true, false, true);
}

void MoveCameraVert(int degrees) {
	CameraVert.write(degrees);
}

void MoveCameraHor(int degrees) {
	CameraHor.write(degrees);
}

void Stop() {
	SetMotors(0, 0, 0, 0, false, true, true);
	if (TEST_CYCLE) {
		TestCycle();
	}
}

int NormalizeSpeed(int speedAsPercent) {
	if (speedAsPercent == 0) {
		return MID_SPEED;
	}
	else {
		return (int)(MID_SPEED + ((MAX_SPEED - MID_SPEED) * (speedAsPercent / 100.0)));
	}
}

long NormalizeDistance(int cms) {
	return (long)((cms * 3200.0) / WHEEL_CIR); //64 counts per rev, 50:1 ratio = 64 * 50 = 3200
}

void SetMotors(int leftSpeed, int rightSpeed, int leftStop, int rightStop, bool synchronize, bool heedSonar, bool watchForStop) {
	LeftSideSpeed = NormalizeSpeed(leftSpeed);
	RightSideSpeed = NormalizeSpeed(rightSpeed);
	LeftSideStop = NormalizeDistance(leftStop);
	RightSideStop = NormalizeDistance(rightStop);
	leftSideMotors.write(LeftSideSpeed);
	rightSideMotors.write(RightSideSpeed);

	encoders.getCountsAndResetRight();
	encoders.getCountsAndResetLeft();
	Synchronize = synchronize;
	HeedSonar = heedSonar;
	WatchForStop = watchForStop;
	RightSideAdv = 0;
}

void NormalizeMotors() {
	if (LeftSideSpeed != MID_SPEED || RightSideSpeed != MID_SPEED) {
		long left = abs(encoders.getCountsLeft());
		long right = abs(encoders.getCountsRight());
		bool stopLeft = left >= LeftSideStop;
		bool stopRight = right >= RightSideStop;

		if (stopLeft && stopRight) {
			Stop();
		}
		else {
			if (stopLeft) {
				LeftSideSpeed = 0;
				leftSideMotors.write(MID_SPEED);
			}
			if (stopRight) {
				RightSideSpeed = 0;
				rightSideMotors.write(MID_SPEED);
			}
		}
	}
}

void SynchronizeMotors() {
	if (Synchronize) {
		long left = abs(encoders.getCountsLeft());
		long right = abs(encoders.getCountsRight());
		int newRightAdv = right - left;
		if (abs(newRightAdv) > RIGHT_ADV_THRES && abs(newRightAdv) > abs(RightSideAdv)) {
			if (newRightAdv > 0) {
				//Serial.println("DOWN");
				if (RightSideSpeed < MID_SPEED) {
					RightSideSpeed += 1;
				}
				else {
					RightSideSpeed -= 1;
				}
			}
			else {
				//Serial.println("UP");
				if (RightSideSpeed < MID_SPEED) {
					RightSideSpeed -= 1;
				}
				else {
					RightSideSpeed += 1;
				}
			}
			rightSideMotors.write(RightSideSpeed);
		}
		RightSideAdv = newRightAdv;
	}
}