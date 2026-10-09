package net.astralya.solareum.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public record HandCrankPressRecipe(Ingredient ingredient, int inputCount, ItemStack output)
    implements Recipe<SingleRecipeInput> {
  @Override
  public boolean matches(SingleRecipeInput input, Level level) {
    return ingredient.test(input.item()) && input.item().getCount() >= inputCount;
  }

  @Override
  public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
    return output.copy();
  }

  @Override
  public boolean canCraftInDimensions(int width, int height) {
    return true;
  }

  @Override
  public ItemStack getResultItem(HolderLookup.Provider registries) {
    return output;
  }

  @Override
  public NonNullList<Ingredient> getIngredients() {
    return NonNullList.of(Ingredient.EMPTY, ingredient);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return ModPressRecipes.SERIALIZER.get();
  }

  @Override
  public RecipeType<?> getType() {
    return ModPressRecipes.TYPE.get();
  }
}
