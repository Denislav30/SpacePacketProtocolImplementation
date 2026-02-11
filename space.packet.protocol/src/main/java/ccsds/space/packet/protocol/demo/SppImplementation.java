package ccsds.space.packet.protocol.demo;

import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import ccsds.space.packet.protocol.codec.PacketCodec;
import ccsds.space.packet.protocol.core.SpacePacket;
import ccsds.space.packet.protocol.core.SpacePacketHeader;
import ccsds.space.packet.protocol.services.octetStringService.OctetStringChannel;
import ccsds.space.packet.protocol.services.octetStringService.OctetStringRequest;
import ccsds.space.packet.protocol.services.packetService.PacketRequest;
import ccsds.space.packet.protocol.services.packetService.PacketServiceProvider;
import ccsds.space.packet.protocol.types.CommandType;
import ccsds.space.packet.protocol.types.SequenceFlags;

public class SppImplementation {

  private static final Logger LOGGER = Logger.getLogger(SppImplementation.class.getName());

  public static void main(String[] args) {
    formatLogger();

    byte[] dataField = new byte[] { 1, 2, 3, 4, 5 };
    int c = dataField.length - 1;

    SpacePacketHeader spacePacketHeader = new SpacePacketHeader(0, CommandType.TM, false, 100, SequenceFlags.UNSEGMENTED, 7, c);

    byte[] primaryHeaderBytes = spacePacketHeader.convertToPacketPrimaryHeaderBytes();
    SpacePacketHeader parsedPacketPrimaryHeader = SpacePacketHeader.parsePacketPrimaryHeader(primaryHeaderBytes);

    LOGGER.info("Primary Header bytes (6): " + Arrays.toString(primaryHeaderBytes));
    LOGGER.info("Original APID = " + spacePacketHeader.getApid() + ", Parsed APID = " + parsedPacketPrimaryHeader.getApid());
    LOGGER.info("Original SequenceCount = " + spacePacketHeader.getPacketSequenceCount() + ", Parsed SequenceCount = " + parsedPacketPrimaryHeader.getPacketSequenceCount());
    LOGGER.info("Original C = " + spacePacketHeader.getPacketDataLength() + ", Parsed C = " + parsedPacketPrimaryHeader.getPacketDataLength());

    // PacketService demo
    SpacePacket spacePacket = new SpacePacket(spacePacketHeader, dataField);
    PacketCodec packetCodec = new PacketCodec();

    OctetStringChannel channel = new OctetStringChannel();
    PacketServiceProvider packetServiceProvider = new PacketServiceProvider(packetCodec, channel);

    packetServiceProvider.setPacketListener(packetIndication -> {
      SpacePacket packet = packetIndication.spacePacket();
      LOGGER.info("");
      LOGGER.info("PACKET.indication:");
      LOGGER.info("  APID = " + packetIndication.apid());
      LOGGER.info("  packetVersionNumber = " + packet.getHeader().getPacketVersionNumber());
      LOGGER.info("  packetType = " + packet.getHeader().getPacketType());
      LOGGER.info("  secondaryHeaderFlag = " + packet.getHeader().isSecondaryHeaderFlag());
      LOGGER.info("  sequenceFlags = " + packet.getHeader().getSequenceFlags());
      LOGGER.info("  sequenceCount = " + packet.getHeader().getPacketSequenceCount());
      LOGGER.info("  C(packetDataLength) = " + packet.getHeader().getPacketDataLength());
      LOGGER.info("  dataField = " + Arrays.toString(packet.getPacketDataField()));
    });

    packetServiceProvider.request(new PacketRequest(spacePacket, spacePacketHeader.getApid(), null));

    // OctetStringService demo
    OctetStringChannel octetStringChannel = new OctetStringChannel();

    octetStringChannel.setOctetStringListener(octetStringIndication -> {
      LOGGER.info("");
      LOGGER.info("OCTET_STRING.indication:");
      LOGGER.info("  APID = " + octetStringIndication.apid());
      LOGGER.info("  secondaryHeaderIndicator = " + octetStringIndication.secondaryHeaderIndicator());
      LOGGER.info("  octetString(bytes) = " + Arrays.toString(octetStringIndication.octetString()));
    });

    byte[] someOctets = new byte[] {9, 9, 9, 9};
    octetStringChannel.request(new OctetStringRequest(someOctets, 200, false, CommandType.TM, 1));
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