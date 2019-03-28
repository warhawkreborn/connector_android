package it.thalhammer.warhawkreborn.networking;

import it.thalhammer.warhawkreborn.Util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.charset.Charset;

public class DiscoveryPacket {
    private byte[] data;

    public DiscoveryPacket(byte[] data) {
        this.data = data;
    }

    public DiscoveryPacket(String str) {
        this.data = Util.hexStringToByteArray(str);
    }

    public String getName() {
        if(data == null) return "";
        int i;
        for(i=0; i<32; i++) {
            if(data[180 + i] == 0) break;
        }
        return new String(data, 180, i, Charset.defaultCharset());
    }

    public String getGameMode() {
        if(data == null) return "";
        switch(data[237]) {
            case 0: return "DM";
            case 1: return "TDM";
            case 2: return "Ctf";
            case 3: return "Zones";
            case 4: return "Hero";
            case 5: return "Collection";
            default: return "Unknown gamemode";
        }
    }

    public String getMap() {
        if(data == null) return "";
        int i;
        for(i=0; i<24; i++) {
            if(data[212 + i] == 0) break;
        }
        return new String(data, 212, i, Charset.defaultCharset());
    }

    public String getMapName() {
        String map = getMap();
        if(map.equals("multi01")) return "Eucadia";
        if(map.equals("multi02")) return "Island Outpost";
        if(map.equals("multi03")) return "The Badlands";
        if(map.equals("multi05")) return "Destroyed Capitol";
        if(map.equals("multi06")) return "Omega Factory";
        if(map.equals("multi07")) return "Archipelago";
        if(map.equals("multi08")) return "Vaporfield Glacier";
        if(map.equals("multi09")) return "Tau Crater";
        return "Unknown";
    }

    public int getMapSize() {
        int i;
        int start = -1;
        for(i=0; i<16; i++) {
            if(data[256 + i] == 0) break;
            if(start == -1 && data[256 + i] < 58 && data[256 + i] > 47) start = i;
        }
        String mode = new String(data, 256, i, Charset.defaultCharset());
        //return Integer.valueOf(mode.substring(start));
        return data[336];
    }

    public int getMaxPlayers() {
        if(data == null) return 0;
        return data[239];
    }

    public int getCurrentPlayers() {
        if(data == null) return 0;
        return data[242];
    }

    public int getMinPlayers() {
        if(data == null) return 0;
        return data[280];
    }

    public int getTimeEclapsed() {
        if(data == null) return 0;
        return data[251];
    }

    public int getTimeLimit() {
        if(data == null) return 0;
        return data[279];
    }

    public int getStartWaitTime() {
        if(data == null) return 0;
        return data[282];
    }

    public int getSpawnWaitTime() {
        if(data == null) return 0;
        return data[283];
    }

    public int getRoundsPlayed() {
        if(data == null) return 0;
        return data[315] & 0xff;
    }

    public int getPointLimit() {
        if(data == null) return 0;
        return data[272] << 8 | data[273];
    }

    public int getCurrentPoints() {
        if(data == null) return 0;
        return data[274] << 8 | data[275];
    }

    public void setPort(int i) {
        int lsb = i & 0xff;
        int msb = (i & 0xff00) >> 8;
        data[120] = (byte)lsb;
        data[121] = (byte)msb;
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
