import logging
import random
import socket
import sys
import time
import usb.core
import usb.util

FORMAT = '%(asctime)s %(levelname)s %(module)s:%(lineno)d %(message)s'
logging.basicConfig(format=FORMAT, level=logging.DEBUG)

def usb_thing():
    # find our device
    devices = usb.core.find(find_all=True)

    # was it found?
    if devices is None:
        raise ValueError('Device not found')

    for dev in devices:
        for cfg in dev:
            sys.stdout.write(str(cfg.bConfigurationValue) + '\n')
            for intf in cfg:
                sys.stdout.write('\t' + \
                                 str(intf.bInterfaceNumber) + \
                                 ',' + \
                                 str(intf.bAlternateSetting) + \
                                 '\n')
                for ep in intf:
                    sys.stdout.write('\t\t' + \
                                     str(ep.bEndpointAddress) + \
                                     '\n')

class SESWClient(object):
    host = None
    port = None
    connected = False

    def connect(self, host, port=7379):
        self.host = host
        self.port = port
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.socket.connect((host, port))
        self.connected = True
        logging.info('connected to %s:%s', host, port)
        self.tx('H')
        self.rx()

    def disconnect(self):
        if self.connected:
            self.tx('Q')
            self.rx()
            self.socket.close()
            logging.info('disconnected')

        self.host = None
        self.port = None
        self.connected = False

    def switch_on(self):
        self.tx('S1')
        self.rx()

    def switch_off(self):
        self.tx('S0')
        self.rx()

    def tx(self, data):
        if not data.endswith('\n'):
            data += '\n'
        self.socket.sendall(data)
        logging.debug("TX: " + data.strip())

    def rx(self):
        data = ""
        while not data.endswith('\n'):
            data += self.socket.recv(1024)
        logging.debug("RX: " + data.strip())

def test_sesw_connections(n=sys.maxint):
    import random
    for i in xrange(n):
        client = SESWClient()
        client.connect('192.168.1.113')
        client.switch_on()
#        time.sleep(random.random())
        client.switch_off()
        client.disconnect()
        logging.info('completed: %s', i)

test_sesw_connections(1)
