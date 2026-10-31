package net.astralya.solareum.energy;

import net.minecraft.core.Direction;

/** Native Lux access. Storage access alone does not grant external input or output permission. */
public interface LuxContainer {
  enum Role {
    SOURCE,
    STORAGE,
    CONSUMER
  }

  LuxStorage getLuxStorage();

  /** Routing semantics are separate from face permissions. Consumers never recharge storage. */
  default Role getLuxRole() {
    return Role.CONSUMER;
  }

  /** Input permission is independent of available buffer space. */
  default boolean canReceiveLux(Direction face) {
    return false;
  }

  /** Whether a sender may extract from this face. Consumers retain internal extraction access. */
  default boolean canExtractLux(Direction face) {
    return false;
  }

  /**
   * Native sender access. Actual extraction must be server-only, respect the aggregate per-tick
   * output limit, and mark persistence dirty only on change. Simulation must not mutate state, and
   * a receiver callback must not mutate its sender between simulation and debit. External output is
   * disabled unless explicitly implemented by the machine.
   */
  default long extractLux(long amount, boolean simulate, Direction face) {
    return 0;
  }

  /**
   * Receives native Lux through a face, returning an amount between zero and the nonnegative
   * request. Simulation must not mutate state; actual receipt must be server-only and mark
   * persistence dirty. Implementations must not mutate the sender or exceed their storage capacity
   * and receive rate. Input is disabled unless explicitly implemented by the machine.
   */
  default long receiveLux(long amount, boolean simulate, Direction face) {
    return 0;
  }
}
