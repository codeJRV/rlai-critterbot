#include "lib_mi.h"
#include "lib_ui.h"
#include "armio.h"
#include "lib_motor.h"
#include "lib_adc.h"
#include "lib_adcspi.h"
#include "lib_accel.h"
#include "lib_events.h"
#include "lib_leddrive.h"
#include "lib_thermo.h"
#include "lib_monitor.h"

struct command_packet robot_command;
extern unsigned short crctable[256];

unsigned short crc;
unsigned char mi_test;

unsigned char mi_disabled_commands = 0;
unsigned char commands_packet_read = 0;

void mi_start(void) {
  leddrive_mi();
  ui_set_handler(mi_event);
  error_clear(0xFFFFFFFF);
}

void mi_stop(void) {
  leddrive_ui();
  ui_clear_handler(mi_event);
}

void mi_disable_commands() { 
  mi_disabled_commands = 1;
}

void mi_enable_commands() {
  mi_disabled_commands = 0;
}

int mi_event(void) {
  mi_get_commands();
  mi_send_status();
  error_clear(0xFFFFFFFF);
  return 0;
}

void putwcrc(unsigned char data) {
 
  crc = (crc<<8) ^ crctable[(crc >> 8) ^ data];
  armputchar(data);
}

void mi_send_status(void) {

  int i;
 
  crc = 0; 
  armputchar(MI_HEADER1);
  armputchar(MI_HEADER2);
  armputchar(MI_HEADER3);
  armputchar(MI_HEADER4);
  putwcrc(power_get_voltage());
  putwcrc(power_get_charge_state());
  putwcrc(power_get_bat40());
  putwcrc(power_get_bat160());
  putwcrc(power_get_bat280());
  for(i = 0; i < MOTOR_NUM_MOTORS; i++) {
    putwcrc(motor_voltage(i));
    putwcrc(motor_clicks(i));
    putwcrc(motor_current(i));
    putwcrc(motor_temp(i));
  }
  // Accelerometer
  putwcrc(accel_output[0] >> 4);
  putwcrc(accel_output[1] >> 4);
  putwcrc(accel_output[2] >> 4);
  // Least significant nibbles of the above three
  putwcrc((accel_output[0] << 4) | (accel_output[1] & 0x0F));
  putwcrc(accel_output[2] << 4);
  // Magnetometer
  putwcrc(adc_output[4] >> 2);
  putwcrc(adc_output[5] >> 2);
  putwcrc(adc_output[6] >> 2);
  // Gyroscope
  putwcrc((adcspi_get_output(3, 12) >> 2) - 128);
  // Least significant 2 bits from the above 4
  putwcrc(((adc_output[4] & 0x03) << 6) | 
      ((adc_output[5] & 0x03) << 4) |
      ((adc_output[6] & 0x03) << 2) | 
      (adcspi_get_output(3, 12) & 0x03));
  // IR Distance
  for(i = 0; i < 10; i++)
    putwcrc(adcspi_get_output(0, i) >> 2);
  // Least significant 2 bits from the above 10
  putwcrc(((adcspi_get_output(0, 0) & 0x03) << 6) |
      ((adcspi_get_output(0, 1) & 0x03) << 4) |
      ((adcspi_get_output(0, 2) & 0x03) << 2) |
      (adcspi_get_output(0, 3) & 0x03));
  putwcrc(((adcspi_get_output(0, 4) & 0x03) << 6) |
      ((adcspi_get_output(0, 5) & 0x03) << 4) |
      ((adcspi_get_output(0, 6) & 0x03) << 2) |
      (adcspi_get_output(0, 7) & 0x03));
  putwcrc(((adcspi_get_output(0, 8) & 0x03) << 6) |
      ((adcspi_get_output(0, 9) & 0x03) << 4));
  // Light sensors
  putwcrc(adcspi_get_output(3,8) >> 2);
  putwcrc(adcspi_get_output(3,9) >> 2);
  putwcrc(adcspi_get_output(3,10) >> 2);
  putwcrc(adcspi_get_output(3,11) >> 2);
  // Least significant bits from the above 4
  putwcrc(((adcspi_get_output(3, 8) & 0x03) << 6) |
      ((adcspi_get_output(3, 9) & 0x03) << 4) |
      ((adcspi_get_output(3, 10) & 0x03) << 2) |
      (adcspi_get_output(3, 11) & 0x03));
/*   // We are going to put our status info into the thermal bytes. */
/*   putwcrc((unsigned char)(motor_is_charging_enabled())); */
/*   putwcrc((unsigned char)(0)); //pad */
/*   putwcrc((unsigned char)(motor_is_drive_enabled())); */
/*   putwcrc((unsigned char)(0)); //pad */
/*   putwcrc((unsigned char)(power_get_charge_state())); */
/*   putwcrc((unsigned char)(0)); //pad */
/*   putwcrc((unsigned char)(power_get_byte6())); */
/*   putwcrc((unsigned char)(0)); //pad */
/*  // Should start from 0 instead of 3/4 when we remove the above debug info */
  // Thermal sensors
  for(i = 0; i < 8; i++) {
    putwcrc((unsigned char)(thermo_get_val(i)));
    putwcrc((unsigned char)(thermo_get_val(i)>>8));
  }
  // IR light sensors
  for(i = 0; i < 8; i++) {
    putwcrc((unsigned char)adcspi_get_output(3,i) >> 2);
  }
  // Least significant bits from the above 8
  putwcrc(((adcspi_get_output(3, 0) & 0x03) << 6) |
      ((adcspi_get_output(3, 1) & 0x03) << 4) |
      ((adcspi_get_output(3, 2) & 0x03) << 2) |
      (adcspi_get_output(3, 3) & 0x03));
  putwcrc(((adcspi_get_output(3, 4) & 0x03) << 6) |
      ((adcspi_get_output(3, 5) & 0x03) << 4) |
      ((adcspi_get_output(3, 6) & 0x03) << 2) |
      (adcspi_get_output(3, 7) & 0x03));
  //putwcrc(error_reg >> 24);
  putwcrc(power_get_charge_state());// The top byte of the error flags is not used, and the charge state changes from the top of the packet to the bottom in charger error conditions.
  putwcrc(error_reg >> 16);
  putwcrc(error_reg >> 8);
  putwcrc(error_reg);
  putwcrc(commands_packet_read);
  putwcrc( (motor_is_charging_enabled()? 1:0) << 7 | 
	   (motor_is_drive_enabled()? 1:0) << 6 | 
	   (monitor_status() ? 1: 0));

  armputchar(crc >> 8);
  armputchar(crc & 0xFF);
  return;
}

void mi_get_commands(void) {
    static enum {
    	HEADER1, HEADER2, HEADER3, HEADER4, MOTORDATA, LEDDATA
    } state = HEADER1;
	unsigned char packet_size[] = { 1, 1, 1, 1, 5, 3 * 16 };
	signed char m1 = 0, m2 = 0, m3 = 0;
	char motor_data_read = 0;
	char led_data_read = 0;
	int num_chars_read = 0;
	int led_index = 0;
	while (armgetnumchars() >= packet_size[state]) {
		if (num_chars_read > SER_RX_BUF_SIZE)
			break;
		switch (state) {
		case HEADER1:
			state = ( armgetchar() == MI_HEADER1 ) ? HEADER2 : HEADER1;
      num_chars_read++;
			break;
		case HEADER2:
			state = ( armgetchar() == MI_HEADER2 ) ? HEADER3 : HEADER1;
      num_chars_read++;
			break;
		case HEADER3:
			state = ( armgetchar() == MI_HEADER3 ) ? HEADER4 : HEADER1;
      num_chars_read++;
			break;
		case HEADER4:
			state = ( armgetchar() == MI_HEADER4 ) ? MOTORDATA : HEADER1;
      num_chars_read++;
			break;
		case MOTORDATA:
			robot_command.motor_mode = armgetchar();
			m1 = ((signed char)((unsigned char)armgetchar()));
			m2 = ((signed char)((unsigned char)armgetchar()));
			m3 = ((signed char)((unsigned char)armgetchar()));
			robot_command.led_mode = armgetchar();
      num_chars_read += 5;
			motor_data_read = 1;
		    if (robot_command.led_mode == CCUSTOM)
		    	state = LEDDATA;
		    else
		    	state = HEADER1;
			break;
		case LEDDATA:
			for(led_index = 0; led_index < LED_NUM_LEDS; led_index++) {
			  LED[led_index].r = armgetchar();
			  LED[led_index].g = armgetchar();
			  LED[led_index].b = armgetchar();
			}
      num_chars_read += 48;
			state = HEADER1;
			break;
			led_data_read = 1;
		}
	}
  if (motor_data_read) {
    switch (robot_command.motor_mode) {
      // The various minus signs here are to correct the coordinate system.
      // Really this is an easy way to do it, and should be corrected both
      // in the signs of the XYT->Wheel transform and the wheel drivers
      // themselves.
      case WHEEL_SPACE:
    	  if (!mi_disabled_commands)
    		  motor_set_speed_slew(m1, m2, m3);
    	  break;
      case XYTHETA_SPACE:
    	  if (!mi_disabled_commands)
    		  motor_set_speed_xytheta(m1, m2, m3);
    	  break;
      case WHEEL_VOLTAGE:
    	  if (!mi_disabled_commands)
    		  motor_set_voltage(m1, m2, m3);
    	  break;
      case MOTOR_EXIT:
        if(robot_command.led_mode == LED_EXIT)
          mi_stop();
        break;
      case MOTOR_MI_AVR_ENABLE_CHARGING:
    	  motor_enable_charging();
    	  break;
      case MOTOR_MI_AVR_DISABLE_CHARGING:
    	  if (!mi_disabled_commands)
    		  motor_disable_charging();
    	  break;
      case MOTOR_MI_AVR_ENABLE_VREF:
    	  vref_enable();
    	  break;
      case MOTOR_MI_AVR_DISABLE_VREF:
    	  vref_disable();
    	  break;
      case MOTOR_MI_AVR_ENABLE_AMP:
    	  amplifier_enable();
    	  break;
      case MOTOR_MI_AVR_DISABLE_AMP:
    	  amplifier_disable();
    	  break;
      default:
    	  if (!mi_disabled_commands) {
    		  robot_command.motor_mode = WHEEL_SPACE;
    		  motor_set_speed(0, 0);
    		  motor_set_speed(1, 0);
    		  motor_set_speed(2, 0);
    	  }
        break;
    }
  }
    if (!mi_disabled_commands) {
      switch(robot_command.led_mode) {
        case CCLEAR:
          leddrive_clear();
          break;
        case CBATTERY:
          leddrive_batstatus();
          break;
        case CBALL:
          leddrive_ball();
          break;
        case CERROR:
          leddrive_error();
          break;
        case CBUSY:
          leddrive_busy();
          break;
        case CEMERGENCY:
          leddrive_emerg();
          break;
        case CNONE:
        case CCUSTOM:
          if(led_data_read)
            leddrive_custom(); 
          break;
        default:
          break;
      }
    }
}
