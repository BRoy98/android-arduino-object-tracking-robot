/*
  PololuWheelEncoders.cpp - Library for using Pololu Wheel Encoders.
*/
	
/*
 * Copyright (c) 2009 Pololu Corporation. For more information, see
 *
 *   http://www.pololu.com
 *   http://forum.pololu.com
 *   http://www.pololu.com/docs/0J18
 *
 * You may freely modify and share this code, as long as you keep this
 * notice intact (including the two links above).  Licensed under the
 * Creative Commons BY-SA 3.0 license:
 *
 *   http://creativecommons.org/licenses/by-sa/3.0/
 *
 * Disclaimer: To the extent permitted by law, Pololu provides this work
 * without any warranty.  It might be defective, in which case you agree
 * to be responsible for all resulting costs and damages.
 */

#ifndef F_CPU
#define F_CPU 20000000UL
#endif
#include <avr/io.h>
#include <stdlib.h>
#include "PololuWheelEncoders2.h"

#ifdef LIB_POLOLU

#include "../OrangutanLEDs/OrangutanLEDs.h"

extern "C" void encoders_init(unsigned char rightA, unsigned char rightB, unsigned char leftA, unsigned char leftB)
{
	PololuWheelEncoders::init(rightA,rightB,leftA,leftB);
}

extern "C" int encoders_get_counts_right()
{
	return PololuWheelEncoders::getCountsRight();
}

extern "C" int encoders_get_counts_left()
{
	return PololuWheelEncoders::getCountsLeft();
}

extern "C" int encoders_get_counts_and_reset_right()
{
	return PololuWheelEncoders::getCountsAndResetRight();
}

extern "C" int encoders_get_counts_and_reset_left()
{
	return PololuWheelEncoders::getCountsAndResetLeft();
}

extern "C" int encoders_check_error_right()
{
	return PololuWheelEncoders::checkErrorRight();
}

extern "C" int encoders_check_error_left()
{
	return PololuWheelEncoders::checkErrorLeft();
}

#endif

/*
 * Pin Change interrupts
 * PCI0 triggers on PCINT7..0
 * PCI1 triggers on PCINT14..8
 * PCI2 triggers on PCINT23..16
 * PCMSK2, PCMSK1, PCMSK0 registers control which pins contribute.
 *
 * The following table is useful:
 *
 * Arduino pin  AVR pin    PCINT #            PCI #
 * -----------  ---------  -----------------  -----
 * 0 - 7        PD0 - PD7  PCINT16 - PCINT23  PCI2
 * 8 - 13       PB0 - PB5  PCINT0 - PCINT5    PCI0
 * 14 - 19      PC0 - PC5  PCINT8 - PCINT13   PCI1
 *
 */

static char global_rightA;
static char global_leftA;
static char global_rightB;
static char global_leftB;

static long global_counts_right;
static long global_counts_left;

static char global_error_right;
static char global_error_left;

static char global_last_rightA_val;
static char global_last_leftA_val;
static char global_last_rightB_val;
static char global_last_leftB_val;

inline unsigned char get_val(unsigned char pin)
{
   // Note: get_val will work (i.e. always return the same value)
   // even with invalid pin values, since the bit shift on the final
   // return will cause the port value to be shifted all the way to
   // 0.
/*   if(pin <= 7)
      return (PIND >> pin) & 1;   OLD CODE
   if(pin <= 13)
      return (PINB >> (pin-8)) & 1;*/

   if(pin <=13)
      return (PINB >> (pin-6)) & 1;
   if(pin <= 50)
      return (PINB >> 3) & 1;
   if(pin <= 51)
      return (PINB >> 2) & 1;
   if(pin <= 52)
      return (PINB >> 1) & 1;
   if(pin <= 53)
      return (PINB >> 0) & 1;
}

ISR(PCINT0_vect)
{
	unsigned char rightA_val = get_val(global_rightA);
	unsigned char leftA_val = get_val(global_leftA);
	unsigned char rightB_val = get_val(global_rightB);
	unsigned char leftB_val = get_val(global_leftB);

	char plus_right = rightA_val ^ global_last_rightB_val;
	char minus_right = rightB_val ^ global_last_rightA_val;
	char plus_left = leftA_val ^ global_last_leftB_val;
	char minus_left = leftB_val ^ global_last_leftA_val;

	if(plus_right)
		global_counts_right += 1;
	if(minus_right)
		global_counts_right -= 1;

	if(plus_left)
		global_counts_left += 1;
	if(minus_left)
		global_counts_left -= 1;

	if(rightA_val != global_last_rightA_val && rightB_val != global_last_rightB_val)
		global_error_right = 1;
	if(leftA_val != global_last_leftA_val && leftB_val != global_last_leftB_val)
		global_error_left = 1;

	global_last_rightA_val = rightA_val;
	global_last_rightB_val = rightB_val;
	global_last_leftA_val = leftA_val;
	global_last_leftB_val = leftB_val;
}

ISR(PCINT1_vect,ISR_ALIASOF(PCINT0_vect));
ISR(PCINT2_vect,ISR_ALIASOF(PCINT0_vect));

static void enable_interrupts_for_pin(unsigned char p)
{
   // check what block it's in and do the right thing
/*   if(p <= 7)
   {
      PCICR |= 1 << PCIE2;    OLD CODE
      DDRD &= ~(1 << p);
      PCMSK2 |= 1 << p;
   }*/
   if((p >= 10) && (p <= 13))
   {
      PCICR |= 1 << PCIE0;
      DDRB &= ~(1 << (p - 6));
      PCMSK0 |= 1 << (p - 6);
   }
   else if(p == 50)
   {
      PCICR |= 1 << PCIE0;
      DDRB &= ~(1 << (3));
      PCMSK0 |= 1 << (3);
   }
   else if(p == 51)
   {
      PCICR |= 1 << PCIE0;
      DDRB &= ~(1 << (2));
      PCMSK0 |= 1 << (2);
   }
   else if(p == 52)
   {
      PCICR |= 1 << PCIE0;
      DDRB &= ~(1 << (1));
      PCMSK0 |= 1 << (1);
   }
   else if(p == 53)
   {
      PCICR |= 1 << PCIE0;
      DDRB &= ~(1 << (0));
      PCMSK0 |= 1 << (0);
   }
   else if(p <= 69)
   {
      PCICR |= 1 << PCIE2;
      DDRC &= ~(1 << (p - 62));
      PCMSK2 |= 1 << (p - 62);
   }
   // Note: this will work with invalid port numbers, since there is
   // no final "else" clause.
}

void PololuWheelEncoders::init(unsigned char rightA, unsigned char rightB, unsigned char leftA, unsigned char leftB)
{
	global_rightA = rightA;
	global_rightB = rightB;
	global_leftA = leftA;
	global_leftB = leftB;

	// disable interrupts while initializing
	cli();

	enable_interrupts_for_pin(rightA);
	enable_interrupts_for_pin(rightB);
	enable_interrupts_for_pin(leftA);
	enable_interrupts_for_pin(leftB);

	// initialize the global state
	global_counts_right = 0;
	global_counts_left = 0;
	global_error_right = 0;
	global_error_left = 0;

	global_last_rightA_val = get_val(global_rightA);
	global_last_rightB_val = get_val(global_rightB);
	global_last_leftA_val = get_val(global_leftA);
	global_last_leftB_val = get_val(global_leftB);

	// Clear the interrupt flags in case they were set before for any reason.
	// On the AVR, interrupt flags are cleared by writing a logical 1
	// to them.
	PCIFR |= (1 << PCIF0) | (1 << PCIF1) | (1 << PCIF2);

	// enable interrupts
	sei();
}

long PololuWheelEncoders::getCountsRight()
{
	cli();
	long tmp = global_counts_right;
	sei();
	return tmp;
}

long PololuWheelEncoders::getCountsLeft()
{
	cli();
	long tmp = global_counts_left;
	sei();
	return tmp;
}

long PololuWheelEncoders::getCountsAndResetRight()
{
	cli();
	long tmp = global_counts_right;
	global_counts_right = 0;
	sei();
	return tmp;
}

long PololuWheelEncoders::getCountsAndResetLeft()
{
	cli();
	long tmp = global_counts_left;
	global_counts_left = 0;
	sei();
	return tmp;
}

unsigned char PololuWheelEncoders::checkErrorRight()
{
	unsigned char tmp = global_error_right;
	global_error_right = 0;
	return tmp;
}

unsigned char PololuWheelEncoders::checkErrorLeft()
{
	unsigned char tmp = global_error_left;
	global_error_left = 0;
	return tmp;
}

// Local Variables: **
// mode: C++ **
// c-basic-offset: 4 **
// tab-width: 4 **
// indent-tabs-mode: t **
// end: **
