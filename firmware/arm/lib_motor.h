/*
 * lib_motor.h
 *
 * Created by Michael Sokolsky
 * Last modified: 30 April 09
 */

#ifndef LIB_MOTOR_H
#define LIB_MOTOR_H

#include "armio.h"
#include "lib_error.h"
#include "lib_events.h"
#include "lib_spi.h"

#define MOTOR_NUM_MOTORS 3

// This limited the value you can set the motors to, and also
// provides scaling for incoming commands
#define MOTOR_MAX_SPEED 100

#define MOTOR_MAX_PWM 127
// Number of bytes in a motor packet (including header)
#define MOTOR_NUM_BYTES 5
// Number of bytes in a power packet (including header)
#define MOTOR_PWR_BYTES 6

#define MOTOR_PACKET_HEADER 0x7A
#define MOTOR_PWM_HEADER 0x7B
#define MOTOR_SPI_PADDING 0x7D

// Rate of slew to new speed (ratio based on one step per cycle)
#define MOTOR_SLEW_RATE 1

// Time (in cycles) to timeout if no commands received
#define MOTOR_TIMEOUT 50

// Constants for converting from X-Y-Theta to Wheel space
#define XSC100 (-1008) // cos(90+100) * 1024
#define YSC100 (178) // sin(90+100) * 1024
#define TSC100 (1096) // 1.07 * 1024
#define XSC220 (658) // cos(90+220) * 1024
#define YSC220 (784) // sin(90+220) * 1024
#define TSC220 (1096) // 1.07 * 1024
#define XSC340 (350) // cos(90+340) * 1024
#define YSC340 (-962) // sin(90+340) * 1024
#define TSC340 (1096) // 1.07 * 1024

#define ABS(a) (((int)(a)) >= 0 ? (int)(a) : -(int)(a))

extern unsigned int power_rx_data[MOTOR_PWR_BYTES];

/*
 * Initialize motor driver
 */
int motor_init( void );

/*
 * Sets the speed of motor, within +/-MOTOR_MAX_SPEED
 */
void motor_set_speed(int motor, signed char speed);

/*
 * Sets the speed of motors, within +/-MOTOR_MAX_SPEED
 * with a slew-rate limit on the change
 */
void motor_set_speed_slew(signed char speed100,
    signed char speed220, signed char speed340);

/*
 * Sets the speed of the motors with slew-rate limit in
 * x-y-theta space;
 */
void motor_set_speed_xytheta(signed char xvel, signed char yvel, 
    signed char tvel);

/*
 * Sets a raw PWM motor value in range +/-127
 */
void motor_set_pwm(int motor, int pwm);

/*
 * Sets raw PWM values to all motors as a normal control signal
 */
void motor_set_voltage(int pwm100, int pwm220, int pwm340);

/*
 * Returns the current system bus voltage in 1/10ths of a volt.
 */
unsigned char power_get_voltage();

/*
 * Returns the state of the battery charger.  0 is off,
 * 200 is error, others are various states of charge.
 */
unsigned char power_get_charge_state();

/*
 * Returns the current voltage of each battery.  While running on battery
 * power these will all be the same.  They are in 1/10th of a volt,
 * however accuracy is rough, and may not be better than 500mV.
 */ 
unsigned char power_get_bat40();
unsigned char power_get_bat160();
unsigned char power_get_bat280();

/* 
 * We will use an extra byte for information like the power_avr system_state
 */
unsigned char power_get_byte6();


/*
 * Initialize a motor packet
 */
void motor_init_packet(int motor);

/*
 * Initialize power packet
 */
void power_init_packet();

/* 
 * Main motor event loop.  Sends speeds to motors and gets their status.
 * Also collects system voltage (for now, this should move to lib_power)
 */
int motor_event( void );

/*
 * Returns number of clicks (speed) for the last cycle of the motor
 */
signed char motor_clicks(int motor);

/*
 * Returns raw current sense value for the last cycle of the motor
 */
unsigned char motor_current(int motor);

/*
 * Returns raw temperature sense value for the last cycle of the motor
 */
unsigned char motor_temp(int motor);

/*
 * Returns the user commanded velocity of the motor
 */
// signed char motor_command(int motor);

/*
 * Returns the current voltage of the motor in unknown units
 */
signed char motor_voltage(int motor);

/** Disables/enables sending motor commands to the motors. */
int motor_disable_drive();
int motor_enable_drive();

/** Disables charging (via a notification to the power AVR). Charging will
  * remain disabled until enable_charging() is called.
  */
int motor_disable_charging();
/** Enables charging */
int motor_enable_charging();

int motor_is_drive_enabled();
int motor_is_charging_enabled();

#endif /* LIB_MOTOR_H */
