package net.astralya.solareum.neoforge.datagen;

import java.util.concurrent.CompletableFuture;
import net.astralya.solareum.Solareum;
import net.astralya.solareum.item.ModItems;
import net.astralya.solareum.recipe.HandCrankPressRecipe;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public final class ModRecipeProvider extends RecipeProvider {
  public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
    super(output, registries);
  }

  @Override
  protected void buildRecipes(RecipeOutput recipeOutput) {
    press(
        recipeOutput,
        "bio_fiber_from_leaves",
        Ingredient.of(ItemTags.LEAVES),
        4,
        new ItemStack(ModItems.BIO_FIBER.get()));
    press(
        recipeOutput,
        "bio_fiber_from_vines",
        Ingredient.of(Items.VINE),
        2,
        new ItemStack(ModItems.BIO_FIBER.get()));
    press(
        recipeOutput,
        "bio_fiber_from_bamboo",
        Ingredient.of(Items.BAMBOO),
        2,
        new ItemStack(ModItems.BIO_FIBER.get()));
    press(
        recipeOutput,
        "bio_fiber_sheet",
        Ingredient.of(ModItems.BIO_FIBER.get()),
        2,
        new ItemStack(ModItems.BIO_FIBER_SHEET.get()));
    ResourceLocation plateRecipeId =
        ResourceLocation.fromNamespaceAndPath(Solareum.MOD_ID, "hand_crank_press/copper_plate");
    recipeOutput.accept(
        plateRecipeId,
        new HandCrankPressRecipe(
            Ingredient.of(Items.COPPER_INGOT), 1, new ItemStack(ModItems.COPPER_PLATE.get())),
        Advancement.Builder.recipeAdvancement()
            .parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT)
            .addCriterion(
                "has_copper_ingot",
                InventoryChangeTrigger.TriggerInstance.hasItems(Items.COPPER_INGOT))
            .rewards(AdvancementRewards.Builder.recipe(plateRecipeId))
            .build(
                ResourceLocation.fromNamespaceAndPath(
                    Solareum.MOD_ID, "recipes/misc/hand_crank_press/copper_plate")));
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COPPER_COIL.get())
        .pattern("CSC")
        .define('C', ModItems.COPPER_PLATE.get())
        .define('S', Items.STICK)
        .unlockedBy(
            "has_copper_plate",
            InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.COPPER_PLATE.get()))
        .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(Solareum.MOD_ID, "copper_coil"));
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.HAND_CRANK_PRESS.get())
        .pattern("PCP")
        .pattern("PBP")
        .pattern(" S ")
        .define('P', ItemTags.PLANKS)
        .define('C', Items.COPPER_INGOT)
        .define('B', Items.COPPER_BLOCK)
        .define('S', Items.STICK)
        .unlockedBy(
            "has_copper_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(Items.COPPER_INGOT))
        .save(
            recipeOutput,
            ResourceLocation.fromNamespaceAndPath(Solareum.MOD_ID, "hand_crank_press"));
  }

  private static void press(
      RecipeOutput output, String name, Ingredient ingredient, int count, ItemStack result) {
    output.accept(
        ResourceLocation.fromNamespaceAndPath(Solareum.MOD_ID, "hand_crank_press/" + name),
        new HandCrankPressRecipe(ingredient, count, result),
        null);
  }
}
