#include <Wire.h>
#include <MorpheusSlave.h>

MorpheusSlave slave = MorpheusSlave();
boolean supportedPins[] = {false, false, true, false, false, false, false, false, false, false};
int pin;
int val;

//For reading from the UART (MP3 Trigger)
String uartRX = "";
boolean uartLastRX = 0;

void setup() {
  setupSerial();
  setupLightning();
}

void setupLightning() {
  pinMode(2, OUTPUT);    
  digitalWrite(2, HIGH);
}

void setupSerial() {
  Serial.begin(38400); //USB
  Serial1.begin(38400); //UART
}

void loop() {
  procInput();
  procUART();
}

void procUART() {
  if ( readUART() ) {
    //Echo whatever the MP3 trigger sends out to USB serial
    Serial.print("U:"); 
    Serial.println(uartRX); 
    uartRX = "";
  }
}

void procInput() {
  slave.receiveSerial();  
  
  if ( slave.newCommand() ) {
    boolean valid = false;
    switch (slave.command) {
      case 'D':
        pin = slave.getAscii09(0);        
        val = slave.getAscii09(1);
        if (supportedPins[pin] && (val == 0 || val == 1)) {
          digitalWrite(pin, val);
          valid = true;
          Serial.println("OK:D"); 
        }
        break;
      case 'U':
        writeUART(slave.data);
        valid = true;
        Serial.println("OK:U"); 
        break;
    }
    slave.reset();    
    if ( !valid )
      Serial.println("BAD"); 
  }    
}

void writeUART(String data) {
  Serial1.print(data);
}

boolean readUART() {
  while (Serial1.available()) {
    uartLastRX = millis();
    
    // get the new byte:
    char inChar = (char)Serial1.read(); 
    // add it to the inputString:
    uartRX += inChar;
  }

  if ( uartRX != "" && millis() > (uartLastRX + 100) )
    return true;
  else
    return false;
}


