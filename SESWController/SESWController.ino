#include <Servo.h>
#include <MorpheusSlave.h>
#include <Wire.h>
#include <SPI.h>
#include <Ethernet.h>

// Enter a MAC address and IP address for your controller below.
// The IP address will be dependent on your local network.
// gateway and subnet are optional:
//MAC Address 90-A2-DA-0D-14-AB
byte mac[] = { 0x90, 0xA2, 0xDA, 0x0D, 0x14, 0xAB };
byte ip[] = { 192, 168, 1, 55 };    
byte gateway[] = { 192, 168, 1, 254 };
byte subnet[] = { 255, 255, 255, 0 };

EthernetServer server(7379);

long lastStats = 0;
long activeConnections = 0;
long connections = 0;
long onTimes = 0;
long offTimes = 0;

Servo sesw;
boolean seswOn = false;

MorpheusSlave slave = MorpheusSlave();
String reply = "";

void setup() {
  // open the serial port
  Serial.begin(115200);
  Serial.print("SESW startup...");

  sesw.attach(9);
  
  // initialize the ethernet device
//  Ethernet.begin(mac);
  Ethernet.begin(mac, ip, gateway, subnet);
  
  // start listening for clients
  server.begin();
  
  Serial.print("SESW server address: ");
  Serial.println(Ethernet.localIP());
}

void loop() {
  printStats();
  
  receiveSerial();
  receiveEthernet();
  
  if ( slave.newCommand() ) {
    processCommand();
    slave.reset();
  }  
}

void printStats() {
  if  ( millis() > lastStats + 5000 ) {
    Serial.print("c=");
    Serial.print(connections);
    Serial.print(" on=");
    Serial.print(onTimes);
    Serial.print(" off=");
    Serial.print(offTimes);
    Serial.println("");
    lastStats = millis();
  }
}

void processCommand() {
  reply = "BAD";
  switch ( slave.command ) {
    case 'h':
    case 'H':
      activeConnections++;
      connections++;
      reply = "OK:H";
      break;
      
    case 'q':
    case 'Q':
      activeConnections--;
      reply = "OK:Q";
      break;
      
    case 's':
    case 'S':
      seswOn = slave.getAscii09(0);
      if ( seswOn ) {
        sesw.write(0);
        onTimes++;
      }
      else {
        sesw.write(180);
        offTimes++;
      }
      reply = "OK:";
      reply += slave.command;
      reply += slave.data;
      break;
  }
    
  sendReply(reply);
}

void sendReply(String& data) {
  Serial.println(data);
  server.println(data);
}

boolean receiveSerial() {
  slave.receiveSerial();
  return slave.newCommand();
}

boolean receiveEthernet() {
    // wait for a new client:
  EthernetClient client = server.available();

  // when the client sends the first byte, say hello:
  if (client) {
    slave.receiveEthernet(client);
  }
  return slave.newCommand();
}
