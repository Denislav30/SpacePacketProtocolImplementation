package spp.demo;

import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import spp.codec.PacketCodec;
import spp.core.SpacePacket;
import spp.core.SpacePacketHeader;
import spp.types.CommandType;
import spp.types.SequenceFlags;

public class SppImplementation {

  private static final Logger LOGGER = Logger.getLogger(SppImplementation.class.getName());

  public static void main(String[] args) {

    byte[] dataField = new byte[] { 1, 2, 3, 4, 5 };
    int c = dataField.length - 1;

    SpacePacketHeader spacePacketHeader = new SpacePacketHeader(0, CommandType.TM, false, 100, SequenceFlags.UNSEGMENTED, 7, c);

    SpacePacket packet = new SpacePacket(spacePacketHeader, dataField);

    PacketCodec packetCodec = new PacketCodec();
    formatLogger();

    // encode demo
    byte[] encoded = packetCodec.encodePacket(packet);
    LOGGER.info("Encoded bytes (" + encoded.length + "): " + Arrays.toString(encoded));

    // decode demo
    SpacePacket decoded = packetCodec.decodePacket(encoded);

    LOGGER.info("DECODED PACKET: ");
    LOGGER.info("  packetVersionNumber = " + decoded.getHeader().getPacketVersionNumber());
    LOGGER.info("  packetType = " + decoded.getHeader().getPacketType());
    LOGGER.info("  secondaryHeaderFlag = " + decoded.getHeader().isSecondaryHeaderFlag());
    LOGGER.info("  APID = " + decoded.getHeader().getApid());
    LOGGER.info("  sequenceFlags = " + decoded.getHeader().getSequenceFlags());
    LOGGER.info("  sequenceCount = " + decoded.getHeader().getPacketSequenceCount());
    LOGGER.info("  C(packetDataLength) = " + decoded.getHeader().getPacketDataLength());
    LOGGER.info("  dataField = " + Arrays.toString(decoded.getPacketDataField()));
  }

  private static void formatLogger() {
    Logger root = Logger.getLogger("");
    for (Handler handler : root.getHandlers()) {
      root.removeHandler(handler);
    }

    ConsoleHandler handler = new ConsoleHandler();
    handler.setLevel(Level.ALL);
    handler.setFormatter(new Formatter() {
      @Override
      public String format(LogRecord record) {
        return record.getMessage() + System.lineSeparator();
      }
    });

    root.addHandler(handler);
    root.setLevel(Level.ALL);
  }
}