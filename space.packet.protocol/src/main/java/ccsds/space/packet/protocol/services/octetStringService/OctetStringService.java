package ccsds.space.packet.protocol.services.octetStringService;

/*
  OctetStringService class contains method request which represents the primitive PACKET.request
 */
public interface OctetStringService {
  void request(OctetStringRequest octetStringRequest);
  void setOctetStringListener(OctetStringListener octetStringListener);
}
