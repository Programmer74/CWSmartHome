#include <SPI.h>
#include <RF24Network.h>
#include <Wire.h>
#include "RF24.h"
#include "printf.h"
#include <OneWire.h>


/*************  USER Configuration *****************************/
                                          // Hardware configuration
RF24 radio(10,9);                        // Set up nRF24L01 radio on SPI bus plus pins 7 & 8

/***************************************************************/

RF24Network network(radio);          // Network uses that radio

const uint16_t this_node = 04;        // Address of our node in Octal format

//Adafruit_BMP280 bmp; // I2C

#define MSG_TYPE_GET_RQ 0
#define MSG_TYPE_SET_RQ 1
#define MSG_TYPE_GET_RP 2
#define MSG_TYPE_SET_RP 3
#define MSG_TYPE_PING 4
#define MSG_TYPE_PONG 5

#define REG_DIGITAL_WRITE 0
#define REG_DIGITAL_READ 1
#define REG_ANALOG_WRITE 2
#define REG_ANALOG_READ 3
#define REG_TEMP_1 4
#define REG_TEMP_2 5
#define REG_TEMP 6
#define REG_PRESS 7

#define MAX_REWRITE_ATTEMPTS 3


typedef struct {
    uint8_t message_type;
    uint8_t reg_id;
    uint8_t payload[8];
} nrf_message_t;

OneWire  ds(2);
byte ds_outside_addr[] = {0x28, 0xFF, 0x86, 0x29, 0xC3, 0x16, 0x03, 0xEF};
byte ds_inside_addr[]  = {0x28, 0xFF, 0xEE, 0xAD, 0x53, 0x17, 0x04, 0x78};

void find_ds_sensors() {
    byte addr[8];
    while (ds.search(addr)) {
        Serial.print("\n\r\n\rFound \'1-Wire\' device with address:\n\r");
        for( int i = 0; i < 8; i++) {
            Serial.print("0x");
            if (addr[i] < 16) {
                Serial.print('0');
            }
            Serial.print(addr[i], HEX);
            if (i < 7) {
                Serial.print(", ");
            }
        }
    }
        
    Serial.println("No more addresses.");
}

void setup_ds_temp(byte* addr) {     
    if (OneWire::crc8(addr, 7) != addr[7]) {
        Serial.println("DS CRC is not valid!");
        return;
    }
    ds.reset();
    ds.select(addr);
    ds.write(0x44, 1);        // start conversion, without parasite power on at the end
    
    //delay(1000);     // maybe 750ms is enough, maybe not
    // we might do a ds.depower() here, but the reset will take care of it.
    ds.reset();
    //ds.reset_search();
    //Serial.println("Setup DS18B20 done.");
}

float get_ds_temp(byte* addr) {
    setup_ds_temp(addr);
    byte i;
    byte present = 0;
    byte type_s;
    byte data[12];
    float celsius, fahrenheit;
    
    present = ds.reset();
    ds.select(addr);    
    ds.write(0xBE);         // Read Scratchpad
    
    for ( i = 0; i < 9; i++) {           // we need 9 bytes
        data[i] = ds.read();
    }
    
    // Convert the data to actual temperature
    // because the result is a 16 bit signed integer, it should
    // be stored to an "int16_t" type, which is always 16 bits
    // even when compiled on a 32 bit processor.
    int16_t raw = (data[1] << 8) | data[0];
    if (type_s) {
        raw = raw << 3; // 9 bit resolution default
        if (data[7] == 0x10) {
            // "count remain" gives full 12 bit resolution
            raw = (raw & 0xFFF0) + 12 - data[6];
        }
    } else {
        byte cfg = (data[4] & 0x60);
        // at lower res, the low bits are undefined, so let's zero them
        if (cfg == 0x00) raw = raw & ~7;  // 9 bit resolution, 93.75 ms
        else if (cfg == 0x20) raw = raw & ~3; // 10 bit res, 187.5 ms
        else if (cfg == 0x40) raw = raw & ~1; // 11 bit res, 375 ms
        //// default is 12 bit resolution, 750 ms conversion time
    }
    celsius = (float)raw / 16.0;
    fahrenheit = celsius * 1.8 + 32.0;
    return celsius;
}

float temp_inside = 0;
float temp_outside = 0;
bool setup_ds_done = false;
long temp_counter = 0;

void setup(void)
{

  Serial.begin(115200);
  while (!Serial); 
  Serial.println("========================");
 
  SPI.begin();
  radio.begin();
  radio.setPALevel(RF24_PA_MAX);
  printf_begin();
  
  radio.setPALevel(RF24_PA_MAX);
  radio.printDetails();
 
  network.begin(/*channel*/ 90, /*node address*/ this_node);
  delay(2000);
}

void floatToBytes(float y, uint8_t* bytes) {
  uint32_t x = *((uint32_t*)&y);
  bytes[0] = (x >> 24) & 0xff;
  bytes[1] = (x >> 16) & 0xff;
  bytes[2] = (x >> 8) & 0xff;
  bytes[3] = (x >> 0) & 0xff;
}

uint64_t last_temp_scan = millis();

void loop() {
  
  network.update();
 
  if (network.available()) {
    RF24NetworkHeader in_header;
    nrf_message_t in_msg;
    network.read(in_header, &in_msg, sizeof(in_msg));

    RF24NetworkHeader out_header(in_header.from_node);
    nrf_message_t out_msg;
    
    Serial.print("Incoming msg from "); 
    Serial.print((int)in_header.from_node); 
    Serial.print(" msgId: ");
    Serial.println(in_msg.message_type);
    
    switch (in_msg.message_type) {
      case MSG_TYPE_GET_RQ:
        Serial.print("Get rq on reg "); Serial.print(in_msg.reg_id); Serial.println();
        out_msg.message_type = MSG_TYPE_GET_RP;
        out_msg.reg_id = in_msg.reg_id;
        if (in_msg.reg_id == REG_TEMP_1) {
          float temp = temp_inside;
          floatToBytes(temp, out_msg.payload);
        } else if (in_msg.reg_id == REG_TEMP_2) {
          float temp = temp_outside;
          floatToBytes(temp, out_msg.payload);
        }
        break;
      case MSG_TYPE_SET_RQ:
        Serial.print("Set rq on reg "); Serial.print(in_msg.reg_id); Serial.println();
        out_msg.message_type = MSG_TYPE_SET_RP;
        out_msg.reg_id = in_msg.reg_id;
        if (in_msg.reg_id == REG_DIGITAL_WRITE) {
          Serial.print("Digital write "); Serial.print(in_msg.payload[0]); Serial.print(" to "); Serial.println(in_msg.payload[1]);
          pinMode(in_msg.payload[0], OUTPUT);
          digitalWrite(in_msg.payload[0], in_msg.payload[1]);
        } else if (in_msg.reg_id == REG_ANALOG_WRITE) {
          Serial.print("Analog write "); Serial.print(in_msg.payload[0]); Serial.print(" to "); Serial.println(in_msg.payload[1]);
          pinMode(in_msg.payload[0], OUTPUT);
          analogWrite(in_msg.payload[0], in_msg.payload[1]);
        } else {
          Serial.println("Unknown reg");
        }
        out_msg.payload[0] = in_msg.payload[0];
        out_msg.payload[1] = in_msg.payload[1];
        break;
      case MSG_TYPE_PING:
        Serial.println("Ping rq");
        out_msg.message_type = MSG_TYPE_PONG;
        out_msg.reg_id = in_msg.reg_id;
        out_msg.payload[0] = in_msg.payload[0];
    }

    bool ok = false;
    int attempts = 0;

    while (!ok) {
      ok = network.write(out_header, &out_msg, sizeof(out_msg));
       if (ok)
        Serial.println("ok.");
      else
        Serial.println("failed.");
      attempts++;
      if (attempts > MAX_REWRITE_ATTEMPTS) {
        ok = true;
        Serial.println("Failed too many times.");
      }
    }
  }

  if ((!setup_ds_done) && ((millis() - last_temp_scan) > 3000)) {
    setup_ds_temp(ds_inside_addr);
    setup_ds_temp(ds_outside_addr);
    setup_ds_done = true;
    last_temp_scan = millis();
  } else if ((setup_ds_done) && ((millis() - last_temp_scan) > 1000)) {
    temp_inside = get_ds_temp(ds_inside_addr);
    temp_outside = get_ds_temp(ds_outside_addr);
    setup_ds_done = false;
    last_temp_scan = millis();
    Serial.println("Temp updated.");
    Serial.println(temp_inside);
    Serial.println(temp_outside);
    temp_counter++;
    Serial.println(temp_counter);
  }
}
