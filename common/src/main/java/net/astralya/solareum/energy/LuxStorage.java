package net.astralya.solareum.energy;

/** Loader-independent Lux buffer. Rates limit each receive/extract operation. */
public final class LuxStorage {
  private final long capacity;
  private final long maxReceive;
  private final long maxExtract;
  private long storedLux;

  public LuxStorage(long capacity, long maxReceive, long maxExtract) {
    this.capacity = Math.max(0, capacity);
    this.maxReceive = Math.max(0, maxReceive);
    this.maxExtract = Math.max(0, maxExtract);
  }

  public long getStoredLux() {
    return storedLux;
  }

  public long getCapacity() {
    return capacity;
  }

  public long getMaxReceive() {
    return maxReceive;
  }

  public long getMaxExtract() {
    return maxExtract;
  }

  /** Restores saved state without applying transfer limits. */
  public void setStoredLux(long amount) {
    storedLux = Math.max(0, Math.min(capacity, amount));
  }

  public long receiveLux(long amount, boolean simulate) {
    long received = Math.min(Math.max(0, amount), Math.min(maxReceive, capacity - storedLux));
    if (!simulate) storedLux += received;
    return received;
  }

  public long extractLux(long amount, boolean simulate) {
    long extracted = Math.min(Math.max(0, amount), Math.min(maxExtract, storedLux));
    if (!simulate) storedLux -= extracted;
    return extracted;
  }
}
