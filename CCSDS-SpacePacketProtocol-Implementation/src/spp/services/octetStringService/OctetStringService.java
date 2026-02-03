package spp.services.octetStringService;

/*
  OctetStringService class contains method request which represents the primitive PACKET.request
 */
public interface OctetStringService {
  void request(byte[] data);
  void setListener(OctetStringListener octetStringListener);
}
