package net.astralya.solareum.client.screen;

import java.util.Locale;
import net.astralya.solareum.menu.RadiantOvenMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public final class RadiantOvenScreen extends AbstractContainerScreen<RadiantOvenMenu> {
  private static final ResourceLocation BACKGROUND =
      ResourceLocation.withDefaultNamespace("textures/gui/container/furnace.png");
  private static final ResourceLocation FLAME =
      ResourceLocation.withDefaultNamespace("container/furnace/lit_progress");
  private static final ResourceLocation ARROW =
      ResourceLocation.withDefaultNamespace("container/furnace/burn_progress");
  private float previousLuxFraction;
  private float displayedLuxFraction;

  public RadiantOvenScreen(RadiantOvenMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
  }

  @Override
  protected void init() {
    super.init();
    titleLabelX = (imageWidth - font.width(title)) / 2;
  }

  private static float fraction(int current, int maximum) {
    return maximum <= 0 ? 0 : Mth.clamp((float) current / maximum, 0, 1);
  }

  @Override
  protected void containerTick() {
    super.containerTick();
    previousLuxFraction = displayedLuxFraction;
    float target = fraction(menu.getStoredLux(), menu.getLuxCapacity());
    displayedLuxFraction = Mth.lerp(0.25F, displayedLuxFraction, target);
    if (Math.abs(displayedLuxFraction - target) < 0.001F) displayedLuxFraction = target;
  }

  @Override
  protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
    graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    // Cover the unused vanilla fuel-slot recess with plain panel background from the same texture.
    graphics.blit(BACKGROUND, leftPos + 55, topPos + 52, 8, 6, 18, 18);
    float displayed =
        Mth.lerp(Mth.clamp(partialTick, 0, 1), previousLuxFraction, displayedLuxFraction);
    int height = Math.round(displayed * 14);
    if (height > 0) {
      graphics.blitSprite(
          FLAME, 14, 14, 0, 14 - height, leftPos + 56, topPos + 50 - height, 14, height);
    }
    int width =
        Math.round(fraction(menu.getProcessingProgress(), menu.getMaxProcessingProgress()) * 24);
    if (width > 0) {
      graphics.blitSprite(ARROW, 24, 16, 0, 0, leftPos + 79, topPos + 34, width, 16);
    }
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    super.render(graphics, mouseX, mouseY, partialTick);
    renderTooltip(graphics, mouseX, mouseY);
    if (isHovering(56, 36, 14, 14, mouseX, mouseY)) {
      graphics.renderTooltip(
          font,
          Component.literal(
              String.format(
                  Locale.ROOT, "%,d / %,d Lux", menu.getStoredLux(), menu.getLuxCapacity())),
          mouseX,
          mouseY);
    }
  }
}
