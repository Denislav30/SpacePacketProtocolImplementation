package spp;

public class Main {

  public static void main(String[] args) {

    SpacePacketHeader spacePacketHeader = new SpacePacketHeader(0, 0, false, 100, 3, 1, 4);

    byte[] data = new  byte[3];

    SpacePacket packet = new SpacePacket(spacePacketHeader, data);

  }

}
