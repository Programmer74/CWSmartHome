#include <iostream>
#include <cstdlib>
#include <sstream>
#include <string>
#include <unistd.h>
#include "udp.h"

#define BUF_SIZE 32

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

typedef struct {
	uint8_t message_type;
	uint8_t reg_id;
	uint8_t payload[8];
} nrf_message_t;

int main() {
	std::cout << "Hi there!" << std::endl;

	udp_client_server::udp_server srv("0.0.0.0", 1337);
	udp_client_server::udp_client cli("localhost", 1338);

	char buf[BUF_SIZE];
	int rq_len, res;

	while (true) {
		rq_len = srv.recv(buf, BUF_SIZE);
		if (rq_len > 0) {
			int nodeID = (int)buf[0];
			int msgType = (int)buf[1];
			int regID = (int)buf[2];

			std::cout << "UDP rq: nodeID = " << nodeID << std::endl;
			std::cout << " type = " << msgType << std::endl;
			std::cout << " reg = " << regID << std::endl;

			if ((nodeID == 01) || (nodeID == 03)) {
				//only online nodes

				if (msgType == MSG_TYPE_PING) {
					buf[1] = (uint8_t)MSG_TYPE_PONG;
					res = cli.send(buf, 11);
					if (res < 0) {
						std::cerr << "UDP send back " << (int)errno << std::endl;
					}
				}
			}
		}
	}
}