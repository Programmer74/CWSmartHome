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

#define DIGITAL_WRITE 0
#define DIGITAL_READ 1
#define ANALOG_WRITE 2
#define ANALOG_READ 3

typedef struct {
    uint8_t message_type;
    uint8_t reg_id;
    uint8_t payload[8];
} nrf_message_t;

void setup(void)
{

  Serial.begin(115200);
  Serial.println("========================");
 
  SPI.begin();
  radio.begin();
  printf_begin();
  radio.printDetails();
 
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
        out_msg.payload[0] = in_msg.reg_id * 2;
        break;
      case MSG_TYPE_SET_RQ:
        Serial.print("Set rq on reg "); Serial.print(in_msg.reg_id); Serial.println();
        out_msg.message_type = MSG_TYPE_SET_RP;
        out_msg.reg_id = in_msg.reg_id;
        out_msg.payload[0] = in_msg.payload[0];

        if (in_msg.reg_id == DIGITAL_WRITE) {
          Serial.print("Digital write "); Serial.print(in_msg.payload[0]); Serial.print(" to "); Serial.println(in_msg.payload[1]);
          digitalWrite(in_msg.payload[0], in_msg.payload[1]);
        } else {
          Serial.println("Unknown reg");
        }
        
        break;
    }

    network.write(out_header, &out_msg, sizeof(out_msg));
  }
  
}
