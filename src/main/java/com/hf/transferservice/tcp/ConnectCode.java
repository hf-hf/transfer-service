package com.hf.transferservice.tcp;

public class ConnectCode {

    static final byte BIND = 2;

    static final byte SEND_DATA = 3;

    static final byte CHECK = 0x20;

    static final byte[] CHECK_RESPONSE = {0x21, 0x21, 0x21, 0x21};

}
