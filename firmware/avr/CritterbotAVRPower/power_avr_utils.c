/*
 * power_avr_utils.c
 *
 *  Created on: Jun 22, 2009
 *      Author: sokolsky
 */

#include "include/pin_names.h"
#include "include/critterbot_power.h"
#include "include/power_avr_charge.h"
#include "include/power_avr_utils.h"
#include <avr/interrupt.h>

void bat40_enable() {
  BATEN_PORT |= BAT40EN;
}

void bat40_disable() {
  BATEN_PORT &= ~BAT40EN;
}

void bat160_enable() {
  BATEN_PORT |= BAT160EN;
}

void bat160_disable() {
  BATEN_PORT &= ~BAT160EN;
}

void bat280_enable() {
  BATEN_PORT |= BAT280EN;
}

void bat280_disable() {
  BATEN_PORT &= ~BAT280EN;
}

void cpu_enable() {
  CPUEN_PORT |= CPUEN;
}

void cpu_disable() {
  CPUEN_PORT &= ~CPUEN;
}

void v3_bus_enable() {
  V3INHIB_PORT &= ~V3INHIB;
}

void v3_bus_disable() {
  V3INHIB_PORT |= V3INHIB;
}

void charger40_disable(void) {
  SHDWN_PORT &= ~SHDWN40;
  SHDWN_DDR |= SHDWN40;
}

void charger40_enable(void) {
  SHDWN_DDR &= ~SHDWN40;
}

void charger160_disable(void) {
  SHDWN_PORT &= ~SHDWN160;
  SHDWN_DDR |= SHDWN160;
}

void charger160_enable(void) {
  SHDWN_DDR &= ~SHDWN160;
}

void charger280_disable(void) {
  SHDWN_PORT &= ~SHDWN280;
  SHDWN_DDR |= SHDWN280;
}

void charger280_enable(void) {
  SHDWN_DDR &= ~SHDWN280;
}

void charger_init(void) {
  charger40_disable();
  charger160_disable();
  charger280_disable();
}

/**
 * returns:
 * 0 - Charging
 * 1 - Invalid state
 * 2 - Top-off charge
 * 3 - Shutdown
 */
uint8_t charger40_status(void) {
  uint8_t retval;

  cli();
  retval = (CHGSTAT_PIN & CHGSTAT40) >> 1;
  CHGSW_PORT |= CHGSW40;
  CHGSW_DDR |= CHGSW40;
  CHGSW_PORT |= CHGSW40;
  __asm__ volatile("nop");
  __asm__ volatile("nop");
  __asm__ volatile("nop");
  __asm__ volatile("nop");
  retval += (CHGSTAT_PIN & CHGSTAT40);
  CHGSW_DDR &= ~CHGSW40;
  CHGSW_PORT &= ~CHGSW40;
  sei();
  return retval;
}

uint8_t charger160_status(void) {
  uint8_t retval;

  cli();
  retval = (CHGSTAT_PIN & CHGSTAT160) >> 3;
  CHGSW_PORT |= CHGSW160;
  CHGSW_DDR |= CHGSW160;
  CHGSW_PORT |= CHGSW160;
  __asm__ volatile("nop");
  __asm__ volatile("nop");
  __asm__ volatile("nop");
  __asm__ volatile("nop");
  retval += (CHGSTAT_PIN & CHGSTAT160) >> 2;
  CHGSW_DDR &= ~CHGSW160;
  CHGSW_PORT &= ~CHGSW160;
  sei();
  return retval;
}

uint8_t charger280_status(void) {
  uint8_t retval;

  cli();
  retval = (CHGSTAT_PIN & CHGSTAT280) >> 5;
  CHGSW_PORT |= CHGSW280;
  CHGSW_DDR |= CHGSW280;
  CHGSW_PORT |= CHGSW280;
  __asm__ volatile("nop");
  __asm__ volatile("nop");
  __asm__ volatile("nop");
  __asm__ volatile("nop");
  retval += (CHGSTAT_PIN & CHGSTAT280) >> 4;
  CHGSW_DDR &= ~CHGSW280;
  CHGSW_PORT &= ~CHGSW280;
  sei();
  return retval;
}

uint8_t get_adc(uint8_t channel) {
  uint8_t temp;
  ADMUX = 0x20 | channel;
  ADCSRA = 0xC3;
  while( ADCSRA & 0x40 );
  temp = ADCH;
  return temp;
}

uint8_t get_bat40_voltage(void) {
  return get_adc(BAT40SENSE) - 3;
}

uint8_t get_bat160_voltage(void) {
  return get_adc(BAT160SENSE) - 2;
}

uint8_t get_bat280_voltage(void) {
  return get_adc(BAT280SENSE) - 2;
}

uint8_t get_bat40_current(void) {
  return get_adc(IMON40);
}

uint8_t get_bat160_current(void) {
  return get_adc(IMON280);
}

uint8_t get_bat280_current(void) {
  return get_adc(IMON280);
}

uint8_t get_vsys(void) {
  uint16_t temp = get_adc(VSYS);
  // Correction to actual voltages
  temp = (((temp * 69) >> 6) + 1);
  return temp;
}

void set_cpu_fan(uint8_t vsys) {
  OCR2B = 30 - (vsys / 10);
}

void set_motor_fan(uint8_t vsys) {
  if(vsys > 170 && charge_state > 0 && charge_state < 200)
    OCR1AL = 0xB0;
  else
    OCR1AL = 0x00;
}