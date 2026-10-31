package net.astralya.solareum.block.entity.custom;

import net.astralya.solareum.block.custom.RadiantOvenBlock;
import net.astralya.solareum.block.entity.ModBlockEntityTypes;
import net.astralya.solareum.energy.LuxContainer;
import net.astralya.solareum.energy.LuxStorage;
import net.astralya.solareum.menu.RadiantOvenMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class RadiantOvenBlockEntity extends BlockEntity
    implements Container, MenuProvider, LuxContainer {
  public static final long CAPACITY = 2_000;
  public static final long MAX_RECEIVE = 20;
  public static final int LUX_PER_TICK = 20;
  public static final int REQUIRED_WORK = 2_000;

  // Extraction is internal consumption only; no external energy access is exposed.
  private final LuxStorage storage = new LuxStorage(CAPACITY, MAX_RECEIVE, LUX_PER_TICK);
  private long nativeReceiveTick = Long.MIN_VALUE;
  private long nativeReceived;
  // Logical input slot 0 and output slot 1; no sided automation rules.
  private ItemStack input = ItemStack.EMPTY;
  private ItemStack output = ItemStack.EMPTY;
  private int processingWork;
  private RecipeHolder<? extends AbstractCookingRecipe> activeRecipe;
  private ItemStack workInput = ItemStack.EMPTY;

  public RadiantOvenBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntityTypes.RADIANT_OVEN.get(), pos, state);
  }

  public long getStoredLux() {
    return storage.getStoredLux();
  }

  public long getLuxCapacity() {
    return storage.getCapacity();
  }

  @Override
  public LuxStorage getLuxStorage() {
    return storage;
  }

  @Override
  public boolean canReceiveLux(Direction face) {
    return true;
  }

  @Override
  public long receiveLux(long amount, boolean simulate, Direction face) {
    if (!(level instanceof ServerLevel serverLevel)) return 0;
    long tick = serverLevel.getGameTime();
    long receivedThisTick = nativeReceiveTick == tick ? nativeReceived : 0;
    long received = storage.receiveLux(Math.min(amount, MAX_RECEIVE - receivedThisTick), simulate);
    if (!simulate && received > 0) {
      nativeReceiveTick = tick;
      nativeReceived = receivedThisTick + received;
      setChanged();
    }
    return received;
  }

  /** Integer work units, each representing one consumed Lux. */
  public int getProcessingProgress() {
    return processingWork;
  }

  public int getMaxProcessingProgress() {
    return REQUIRED_WORK;
  }

  public boolean isProcessing() {
    return getBlockState().getValue(RadiantOvenBlock.LIT);
  }

  private record CookingMatch(
      RecipeHolder<? extends AbstractCookingRecipe> holder, ItemStack result) {}

  private CookingMatch findRecipe(ItemStack stack) {
    if (!(level instanceof ServerLevel) || stack.isEmpty()) return null;
    var holder =
        level
            .getRecipeManager()
            .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), level)
            .orElse(null);
    if (holder == null) return null;
    ItemStack result =
        holder.value().assemble(new SingleRecipeInput(stack), level.registryAccess());
    return result.isEmpty() ? null : new CookingMatch(holder, result);
  }

  private boolean canAccept(ItemStack result) {
    if (result.getCount() > result.getMaxStackSize()) return false;
    return output.isEmpty()
        || (ItemStack.isSameItemSameComponents(output, result)
            && result.getCount() <= output.getMaxStackSize() - output.getCount());
  }

  public void serverTick() {
    if (!(level instanceof ServerLevel)) return;
    CookingMatch match = findRecipe(input);
    if (match == null) {
      resetWork();
      setLit(false);
      return;
    }
    if ((!workInput.isEmpty() && !ItemStack.isSameItemSameComponents(workInput, input))
        || (activeRecipe != null && !activeRecipe.equals(match.holder()))) {
      resetWork();
    }
    activeRecipe = match.holder();
    workInput = input.copyWithCount(1);
    if (!canAccept(match.result())) {
      setLit(false);
      return;
    }
    int consumed =
        (int) storage.extractLux(Math.min(LUX_PER_TICK, REQUIRED_WORK - processingWork), false);
    if (consumed > 0) {
      processingWork += consumed;
      if (processingWork == REQUIRED_WORK) {
        // Vanilla cooking recipes consume exactly one item from their SingleRecipeInput.
        input.shrink(1);
        if (output.isEmpty()) output = match.result().copy();
        else output.grow(match.result().getCount());
        processingWork = 0;
        activeRecipe = null;
        workInput = ItemStack.EMPTY;
      }
      setChanged();
    }
    setLit(consumed > 0);
  }

  private void resetWork() {
    if (processingWork != 0) {
      processingWork = 0;
      setChanged();
    }
    activeRecipe = null;
    workInput = ItemStack.EMPTY;
  }

  private void setLit(boolean lit) {
    BlockState state = getBlockState();
    if (state.getValue(RadiantOvenBlock.LIT) != lit) {
      level.setBlock(
          worldPosition, state.setValue(RadiantOvenBlock.LIT, lit), Block.UPDATE_CLIENTS);
    }
  }

  @Override
  public Component getDisplayName() {
    return Component.translatable("block.solareum.radiant_oven");
  }

  @Override
  public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
    return new RadiantOvenMenu(
        id,
        inventory,
        this,
        new ContainerData() {
          @Override
          public int get(int index) {
            return switch (index) {
              case 0 -> (int) getStoredLux();
              case 1 -> (int) getLuxCapacity();
              case 2 -> getProcessingProgress();
              case 3 -> getMaxProcessingProgress();
              default -> 0;
            };
          }

          @Override
          public void set(int index, int value) {
            // Server values are authoritative. The client uses SimpleContainerData instead.
          }

          @Override
          public int getCount() {
            return RadiantOvenMenu.DATA_COUNT;
          }
        });
  }

  @Override
  public int getContainerSize() {
    return 2;
  }

  @Override
  public boolean isEmpty() {
    return input.isEmpty() && output.isEmpty();
  }

  @Override
  public ItemStack getItem(int slot) {
    return switch (slot) {
      case 0 -> input;
      case 1 -> output;
      default -> throw new IndexOutOfBoundsException(slot);
    };
  }

  @Override
  public ItemStack removeItem(int slot, int count) {
    ItemStack removed = getItem(slot).split(Math.max(0, count));
    if (!removed.isEmpty()) {
      if (slot == 0 && input.isEmpty()) resetWork();
      setChanged();
    }
    return removed;
  }

  @Override
  public ItemStack removeItemNoUpdate(int slot) {
    ItemStack stack = getItem(slot);
    if (slot == 0) {
      input = ItemStack.EMPTY;
      resetWork();
    } else output = ItemStack.EMPTY;
    return stack;
  }

  @Override
  public void setItem(int slot, ItemStack stack) {
    stack.limitSize(getMaxStackSize(stack));
    if (slot == 0) {
      if (!ItemStack.isSameItemSameComponents(input, stack)) resetWork();
      input = stack;
    } else if (slot == 1) output = stack;
    else throw new IndexOutOfBoundsException(slot);
    setChanged();
  }

  @Override
  public boolean canPlaceItem(int slot, ItemStack stack) {
    return slot == 0;
  }

  @Override
  public boolean stillValid(Player player) {
    return Container.stillValidBlockEntity(this, player);
  }

  @Override
  public void clearContent() {
    input = ItemStack.EMPTY;
    output = ItemStack.EMPTY;
    resetWork();
    setChanged();
  }

  public void dropContents() {
    if (!(level instanceof ServerLevel)) return;
    if (!input.isEmpty()) Block.popResource(level, worldPosition, input);
    if (!output.isEmpty()) Block.popResource(level, worldPosition, output);
    input = ItemStack.EMPTY;
    output = ItemStack.EMPTY;
    resetWork();
    setChanged();
  }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    tag.putLong("StoredLux", storage.getStoredLux());
    tag.putInt("ProcessingWork", processingWork);
    if (!input.isEmpty()) tag.put("Input", input.save(registries));
    if (!output.isEmpty()) tag.put("Output", output.save(registries));
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    ItemStack loadedInput = ItemStack.parseOptional(registries, tag.getCompound("Input"));
    int loadedWork = Math.max(0, Math.min(REQUIRED_WORK - 1, tag.getInt("ProcessingWork")));
    // /data can reload an existing entity as well as initial world loading.
    if (!input.isEmpty() && !ItemStack.isSameItemSameComponents(input, loadedInput)) loadedWork = 0;
    input = loadedInput;
    output = ItemStack.parseOptional(registries, tag.getCompound("Output"));
    storage.setStoredLux(tag.getLong("StoredLux"));
    processingWork = input.isEmpty() ? 0 : loadedWork;
    workInput = input.copyWithCount(1);
    // Re-resolve the recipe after loading; recipe objects are never serialized.
  }
}
