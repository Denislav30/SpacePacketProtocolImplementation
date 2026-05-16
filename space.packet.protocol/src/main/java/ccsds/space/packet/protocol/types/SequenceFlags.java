package ccsds.space.packet.protocol.types;

/*
Sequence Flags
  * 00 = continuation segment of user data
  * 01 = first segment of user data
  * 10 = last segment of user data
  * 11 = unsegmented user data
 */
public enum SequenceFlags {
  CONTINUATION(0),
  FIRST(1),
  LAST(2),
  UNSEGMENTED(3);

  private final int value;

  SequenceFlags(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  public static SequenceFlags fromValue(int value) {
    for (SequenceFlags flag : SequenceFlags.values()) {
      if (flag.getValue() == value) {
        return flag;
      }
    }
    throw new IllegalArgumentException("Invalid SequenceFlags value!" + value);
  }
}