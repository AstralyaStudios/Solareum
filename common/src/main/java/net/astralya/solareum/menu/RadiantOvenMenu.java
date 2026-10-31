package net.astralya.solareum.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public final class RadiantOvenMenu extends AbstractContainerMenu {
  public static final int DATA_COUNT = 4;
  private static final int PLAYER_START = 2;
  private static final int HOTBAR_START = 29;
  private static final int PLAYER_END = 38;
  private final Container container;
  private final ContainerData data;
  private final Level level;

  public RadiantOvenMenu(int id, Inventory inventory) {
    this(id, inventory, new SimpleContainer(2), new SimpleContainerData(DATA_COUNT));
  }

  public RadiantOvenMenu(int id, Inventory inventory, Container container, ContainerData data) {
    super(ModMenuTypes.RADIANT_OVEN.get(), id);
    checkContainerSize(container, 2);
    checkContainerDataCount(data, DATA_COUNT);
    this.container = container;
    this.data = data;
    level = inventory.player.level();
    addSlot(new Slot(container, 0, 56, 17));
    addSlot(
        new Slot(container, 1, 116, 35) {
          @Override
          public boolean mayPlace(ItemStack stack) {
            return false;
          }
        });
    for (int row = 0; row < 3; row++) {
      for (int column = 0; column < 9; column++) {
        addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
      }
    }
    for (int column = 0; column < 9; column++) {
      addSlot(new Slot(inventory, column, 8 + column * 18, 142));
    }
    addDataSlots(data);
  }

  public int getStoredLux() {
    return data.get(0);
  }

  public int getLuxCapacity() {
    return data.get(1);
  }

  public int getProcessingProgress() {
    return data.get(2);
  }

  public int getMaxProcessingProgress() {
    return data.get(3);
  }

  private boolean canSmelt(ItemStack stack) {
    return level
        .getRecipeManager()
        .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), level)
        .isPresent();
  }

  @Override
  public boolean stillValid(Player player) {
    return container.stillValid(player);
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
    Slot slot = slots.get(index);
    if (!slot.hasItem()) return ItemStack.EMPTY;
    ItemStack stack = slot.getItem();
    ItemStack original = stack.copy();
    if (index < PLAYER_START) {
      if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) return ItemStack.EMPTY;
    } else if (canSmelt(stack)) {
      if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
    } else if (index < HOTBAR_START) {
      if (!moveItemStackTo(stack, HOTBAR_START, PLAYER_END, false)) return ItemStack.EMPTY;
    } else if (!moveItemStackTo(stack, PLAYER_START, HOTBAR_START, false)) {
      return ItemStack.EMPTY;
    }
    if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
    else slot.setChanged();
    if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
    slot.onTake(player, stack);
    return original;
  }
}
