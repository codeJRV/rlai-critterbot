/*
 * avr_motor.h
 *
 *  Created on: Jun 5, 2009
 *      Author: sokolsky
 */

#ifndef AVR_MOTOR_H_
#define AVR_MOTOR_H_

#define MTR_LOW_A_PIN   _BV(1)
#define MTR_LOW_B_PIN _BV(0)
#define MTR_EN_PIN  _BV(4)
#define MTR_A_PIN   _BV(5)
#define MTR_B_PIN   _BV(6)
#define MOTOR_OFF 127


volatile int8_t motor_setpoint;
int16_t limited_drive_speed;
int16_t clicks;

void motor_init(void);
void set_voltage(int8_t);
int8_t power_limit(int8_t);
int8_t current_limit(int8_t);
int8_t speed_limit(int8_t);
int8_t pid_control(int8_t);
int8_t soft_pid_control(int8_t);

#endif /* AVR_MOTOR_H_ */
