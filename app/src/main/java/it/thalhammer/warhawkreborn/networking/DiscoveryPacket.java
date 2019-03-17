package it.thalhammer.warhawkreborn.networking;

import it.thalhammer.warhawkreborn.Util;

import java.net.Inet4Address;
import java.net.InetAddress;

public class DiscoveryPacket {
    private byte[] data;

    public DiscoveryPacket(byte[] data) {
        this.data = data;
    }

    public DiscoveryPacket(String str) {
        this.data = Util.hexStringToByteArray(str);
    }

    public void setIP(Inet4Address addr) {
        byte[] addrBytes = addr.getAddress();
        this.setIP(addrBytes);
    }

    public void setIP(InetAddress addr) {
        if(addr instanceof Inet4Address)
            setIP((Inet4Address)addr);
        else throw new IllegalArgumentException("Only ipv4 addresses are supported");
    }

    public void setIP(byte[] addrBytes) {
        if(addrBytes == null || addrBytes.length != 4)
            throw new IllegalArgumentException("Invalid address bytes");
        data[112] = addrBytes[0];
        data[113] = addrBytes[1];
        data[114] = addrBytes[2];
        data[115] = addrBytes[3];
        data[176] = addrBytes[0];
        data[177] = addrBytes[1];
        data[178] = addrBytes[2];
        data[179] = addrBytes[3];
    }

    public byte[] getBytes() {
        return data;
    }

    public String getHexString() {
        return Util.byteArrayToHexString(getBytes());
    }
}
