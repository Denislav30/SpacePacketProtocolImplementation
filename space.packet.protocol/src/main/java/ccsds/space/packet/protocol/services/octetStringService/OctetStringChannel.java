package ccsds.space.packet.protocol.services.octetStringService;

public class OctetStringChannel implements OctetStringService {
  private OctetStringListener octetStringListener;

  @Override
  public void request(OctetStringRequest octetStringRequest) {
    if (octetStringListener != null) {
      octetStringListener.indication(new OctetStringIndication(
          octetStringRequest.octetString(),
          octetStringRequest.apid(),
          octetStringRequest.secondaryHeaderIndicator(),
          null
      ));
    }
  }

  @Override
  public void setOctetStringListener(OctetStringListener octetStringListener) {
    this.octetStringListener = octetStringListener;
  }
}
