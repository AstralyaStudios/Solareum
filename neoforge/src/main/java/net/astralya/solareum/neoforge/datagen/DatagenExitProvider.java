package net.astralya.solareum.neoforge.datagen;

import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

final class DatagenExitProvider implements DataProvider {
  private static final long EXIT_DELAY_MILLIS = 5_000L;

  @Override
  public CompletableFuture<?> run(CachedOutput output) {
    Thread exitThread =
        new Thread(
            () -> {
              try {
                Thread.sleep(EXIT_DELAY_MILLIS);
              } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
              }
              System.exit(0);
            },
            "solareum-datagen-exit");
    exitThread.setDaemon(true);
    exitThread.start();
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public String getName() {
    return "Solareum Datagen Exit";
  }
}
