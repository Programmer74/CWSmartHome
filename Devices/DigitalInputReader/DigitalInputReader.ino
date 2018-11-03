#include <SPI.h>
#include <RF24Network.h>
#include "RF24.h"
#include "printf.h"

/*************  USER Configuration *****************************/
                                          // Hardware configuration
RF24 radio(8,9);                        // Set up nRF24L01 radio on SPI bus plus pins 7 & 8

/***************************************************************/

RF24Network network(radio);          // Network uses that radio

const uint16_t this_node = 02;        // Address of our node in Octal format

#define MSG_TYPE_GET_RQ 0
#define MSG_TYPE_SET_RQ 1
#define MSG_TYPE_GET_RP 2
#define MSG_TYPE_SET_RP 3
#define MSG_TYPE_PING 4
#define MSG_TYPE_PONG 5
#define MSG_TYPE_TRIGGER 6

#define LED_PIN 5
#define SENSOR_PIN 3

#define REG_SENSOR 1

#define MAX_REWRITE_ATTEMPTS 5

typedef struct {
    uint8_t message_type;
    uint8_t reg_id;
    uint8_t payload[8];
} nrf_message_t;

void sendMsg(RF24NetworkHeader out_header, nrf_message_t out_msg) {
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

void setup(void)
{

  Serial.begin(115200);
  while (!Serial); 
  Serial.println("========================");
 
  SPI.begin();
  radio.begin();
  printf_begin();
  radio.printDetails();
 
  network.begin(/*channel*/ 90, /*node address*/ this_node);

}

bool triggered = false;

void loop() {


  digitalWrite(LED_PIN, digitalRead(SENSOR_PIN));

  if (digitalRead(SENSOR_PIN) != 0) {
    if (!triggered) {
      RF24NetworkHeader out_header(0);
      nrf_message_t out_msg;
      out_msg.message_type = MSG_TYPE_TRIGGER;
      out_msg.reg_id = REG_SENSOR;
      out_msg.payload[0] = digitalRead(SENSOR_PIN);
      sendMsg(out_header, out_msg);
    }
    triggered = true;
  } else {
    triggered = false;
  }
  
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
        if (in_msg.reg_id == REG_SENSOR) {
          out_msg.payload[0] = digitalRead(SENSOR_PIN);
        } else {
          out_msg.payload[0] = 0;
        }
        break;
      case MSG_TYPE_SET_RQ:
        Serial.print("Set rq on reg "); Serial.print(in_msg.reg_id); Serial.println();
        out_msg.message_type = MSG_TYPE_SET_RP;
        out_msg.reg_id = in_msg.reg_id;
        out_msg.payload[0] = in_msg.payload[0];
        break;
      case MSG_TYPE_PING:
        Serial.println("Ping rq");
        out_msg.message_type = MSG_TYPE_PONG;
        out_msg.reg_id = in_msg.reg_id;
        for (int i = 0; i < 8; i++) {
          out_msg.payload[i] = in_msg.payload[i];
        }
    }

    sendMsg(out_header, out_msg);
  }
  
}
