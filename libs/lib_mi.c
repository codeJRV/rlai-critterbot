#include "lib_mi.h"
#include "lib_ui.h"
#include "armio.h"
#include "lib_motor.h"
#include "lib_adc.h"
#include "lib_adcspi.h"
#include "lib_accel.h"
#include "lib_events.h"

struct command_packet robot_command;
extern unsigned short crctable[256];

unsigned short crc;
unsigned char mi_test;

void mi_start(void) {
  ui_set_handler(mi_event);
  leddrive_byte(&mi_test);
}

void mi_stop(void) {
  ui_clear_handler(mi_event);
}

int mi_event(void) {

  mi_send_status();
  mi_get_commands();

  return 0;
}

void putwcrc(unsigned char data) {
 
  crc = (crc<<8) ^ crctable[(crc >> 8) ^ data];
  armputchar(data);
}

void mi_send_status(void) {

  int i;
 
  crc = 0; 
  putwcrc(MI_HEADER1);
  putwcrc(MI_HEADER2);
  putwcrc(MI_HEADER3);
  putwcrc(MI_HEADER4);
  /*for(i = 0; i < MOTOR_NUM_MOTORS; i++) {
    putwcrc(motor_clicks(i));
    putwcrc(motor_current(i));
    putwcrc(motor_temp(i));
  }
  putwcrc(accel_output[0] >> 4);
  putwcrc(accel_output[1] >> 4);
  putwcrc(accel_output[2] >> 4);
  putwcrc(1);//adc_output[0] >> 2);
  putwcrc(2);//adc_output[1] >> 2);
  putwcrc(3);//adc_output[3] >> 2);
  putwcrc(4);//adcspi_get_output(12) >> 4);
*/  for(i = 0; i < 35; i++)
    putwcrc(0);
  /*for(i = 0; i < 4; i++)
    putwcrc(3);
  putwcrc(error_reg >> 24);
  putwcrc(error_reg >> 16);
  putwcrc(error_reg >> 8);
  putwcrc(error_reg);
  putwcrc(events_time());
*/
  armputchar(crc >> 8);
  armputchar(crc & 0xFF);
}

void mi_get_commands(void) {
  
  int max;
  int xvel, yvel, tvel;
  int m100, m220, m340;
  
  ALIGNMENT_ERROR:

  if(MI_COMMAND_LENGTH > armgetnumchars())
    return;
    
  if(MI_HEADER1 != armgetchar())
    goto ALIGNMENT_ERROR;
  if(MI_HEADER2 != armgetchar())
    goto ALIGNMENT_ERROR;
  if(MI_HEADER3 != armgetchar())
    goto ALIGNMENT_ERROR;
  if(MI_HEADER4 != armgetchar())
    goto ALIGNMENT_ERROR;
  
  robot_command.motor_mode = armgetchar();
  switch(robot_command.motor_mode) {
    case WHEEL_SPACE:
      motor_set_speed(0, (char)armgetchar());
      motor_set_speed(1, (char)armgetchar());
      motor_set_speed(2, (char)armgetchar());
      break;
    case XYTHETA_SPACE:
      xvel = (signed int)((signed char)((unsigned char)armgetchar()));
      yvel = (signed int)((signed char)((unsigned char)armgetchar()));
      tvel = (signed int)((signed char)((unsigned char)armgetchar()));
  
      m100 = (xvel * XSC100 + yvel * YSC100 + tvel * TSC100) / 1024;
      m220 = (xvel * XSC220 + yvel * YSC220 + tvel * TSC220) / 1024;
      m340 = (xvel * XSC340 + yvel * YSC340 + tvel * TSC340) / 1024;  

      max = ABS(m100);
      if(ABS(m220) > max)
        max = ABS(m220);
      if(ABS(m340) > max)
        max = ABS(m340);
      if(max > MOTOR_MAX_SPEED) {
        m100 = (m100 * MOTOR_MAX_SPEED) / max;
        m220 = (m220 * MOTOR_MAX_SPEED) / max;
        m340 = (m340 * MOTOR_MAX_SPEED) / max;
      }

      mi_test = (signed char)m100;
      motor_set_speed(0, (signed char)m100);
      motor_set_speed(1, (signed char)m220);
      motor_set_speed(2, (signed char)m340);
      break;
    default:
      robot_command.motor_mode = WHEEL_SPACE;
      motor_set_speed(0, 0); 
      motor_set_speed(1, 0);
      motor_set_speed(2, 0);
      break;
  }
  robot_command.led_mode = armgetchar();
  return;
}
