package spp.types;

/*
CommandType
  * Telemetry packet = 0
  * Telecommand packet = 1
 */
public enum CommandType {
  TM(0),
  TC(1);

  private final int value;

  CommandType(int value) {
    this.value = value;
  }

  public CommandType getCommandType(int value) {
    switch (value) {
      case 0:
          return TM;
      case 1:
          return TC;
      default:
        throw new IllegalArgumentException("Command type must be 0 (TM) or 1 (TC)!");
    }
  }
}