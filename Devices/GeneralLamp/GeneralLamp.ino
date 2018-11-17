#include <SPI.h>
#include <RF24Network.h>
#include "RF24.h"
#include "printf.h"

/*************  USER Configuration *****************************/
                                          // Hardware configuration
RF24 radio(9,8);                        // Set up nRF24L01 radio on SPI bus plus pins 7 & 8

/***************************************************************/

RF24Network network(radio);          // Network uses that radio

const uint16_t this_node = 01;        // Address of our node in Octal format

#define MSG_TYPE_GET_RQ 0
#define MSG_TYPE_SET_RQ 1
#define MSG_TYPE_GET_RP 2
#define MSG_TYPE_SET_RP 3
#define MSG_TYPE_PING 4
#define MSG_TYPE_PONG 5

#define DIGITAL_WRITE 0
#define DIGITAL_READ 1
#define ANALOG_WRITE 2
#define ANALOG_READ 3
#define RELAY_ON 4
#define RELAY_OFF 5

#define MAX_REWRITE_ATTEMPTS 5

uint8_t pin_states[16] = {0};

typedef struct {
    uint8_t message_type;
    uint8_t reg_id;
    uint8_t payload[8];
} nrf_message_t;

void setup(void)
{

  Serial.begin(115200);
  while (!Serial); 
  Serial.println("========================");
 
  SPI.begin();
  radio.begin();
  printf_begin();
  radio.printDetails();

  for (int i = 0; i < 16; i++) {
    pin_states[i] = 128;
  }
 
  network.begin(/*channel*/ 90, /*node address*/ this_node);
}

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
        if (pin_states[in_msg.reg_id] != 128) {
          Serial.println("Requested relay pin");
          out_msg.payload[0] = pin_states[in_msg.reg_id];
        } else {
          out_msg.payload[0] = in_msg.reg_id * 2;
        }
        break;
      case MSG_TYPE_SET_RQ:
        Serial.print("Set rq on reg "); Serial.print(in_msg.reg_id); Serial.println();
        out_msg.message_type = MSG_TYPE_SET_RP;
        out_msg.reg_id = in_msg.reg_id;
        out_msg.payload[0] = in_msg.payload[0];

        if (in_msg.reg_id == DIGITAL_WRITE) {
          Serial.print("Digital write "); Serial.print(in_msg.payload[0]); Serial.print(" to "); Serial.println(in_msg.payload[1]);
          digitalWrite(in_msg.payload[0], in_msg.payload[1]);
        } else if (in_msg.reg_id == ANALOG_WRITE) {
          Serial.print("Analog write "); Serial.print(in_msg.payload[0]); Serial.print(" to "); Serial.println(in_msg.payload[1]);
          analogWrite(in_msg.payload[0], in_msg.payload[1]);
        } else if (in_msg.reg_id == RELAY_ON) {
          Serial.print("Relay on on "); Serial.println(in_msg.payload[0]);
          digitalWrite(in_msg.payload[0], LOW);
          pin_states[in_msg.payload[0]] = 1;
        } else if (in_msg.reg_id == RELAY_OFF) {
          Serial.print("Relay off on "); Serial.println(in_msg.payload[0]);
          digitalWrite(in_msg.payload[0], HIGH);
          pin_states[in_msg.payload[0]] = 0;
        } else {
          Serial.println("Unknown reg");
        }
        break;
      case MSG_TYPE_PING:
        Serial.println("Ping rq");
        out_msg.message_type = MSG_TYPE_PONG;
        out_msg.reg_id = in_msg.reg_id;
        for (int i = 0; i < 8; i++) {
          out_msg.payload[i] = in_msg.payload[i];
        }
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
  
}
