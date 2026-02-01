package spp.demo;

import spp.core.SpacePacket;
import spp.core.SpacePacketHeader;
import spp.types.CommandType;
import spp.types.SequenceFlags;

public class SppImplementation {

  public static void main(String[] args) {

    SpacePacketHeader spacePacketHeader = new SpacePacketHeader(0, CommandType.TM, false, 100, SequenceFlags.UNSEGMENTED, 1, 4);

    byte[] data = new  byte[3];

    SpacePacket packet = new SpacePacket(spacePacketHeader, data);

  }

}