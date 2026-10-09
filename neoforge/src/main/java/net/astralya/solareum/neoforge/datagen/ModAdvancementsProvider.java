package net.astralya.solareum.neoforge.datagen;

import java.util.function.Consumer;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.AdvancementSubProvider;

public final class ModAdvancementsProvider implements AdvancementSubProvider {
  @Override
  public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> writer) {}
}
