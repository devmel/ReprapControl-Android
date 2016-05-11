# ReprapControl-Android
This app is a G-code sender with graphical user interface to manually control your reprap 3D printer machine.

Compatibility :
 - Bluetooth
    * Module HC-05, HC-06
    * Any Bluetooth-Serial adapter (UUID 00001101-0000-1000-8000-00805f9b34fb)
 
 - USB with OTG adapter and USB Bridge chips 
    * Silicon Laboratories CP210X,
    * FTDI FT232 
    * Prolific PL2303
    * Arduino
    * CDC/ACM

 - LinkBus (Ethernet to UART device)
    * see http://devmel.com/linkbus
    

Communication settings :
 - Baud Rate: 1200 ~ 250000
 - Data Bits : 5, 6, 7 or 8
 - Stop Bits 1 or 2
 - Parity Bit: none, even or odd


Notes : 
 - Marlin firmware is advised
 - There is no advertisement
 - USB Host support can be activated (rooted devices only)


# Library
- usb-serial-for-android from https://github.com/mik3y/usb-serial-for-android
- Devmel from http://devmel.com/apps


# Source
Java source licensed under the Apache License, Version 2.0
