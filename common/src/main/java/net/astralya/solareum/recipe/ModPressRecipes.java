package net.astralya.solareum.recipe;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.astralya.solareum.Solareum;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public final class ModPressRecipes {
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Solareum.MOD_ID, Registries.RECIPE_SERIALIZER);
    private static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Solareum.MOD_ID, Registries.RECIPE_TYPE);
    public static final RegistrySupplier<RecipeSerializer<HandCrankPressRecipe>> SERIALIZER =
            SERIALIZERS.register("hand_crank_press", HandCrankPressRecipeSerializer::new);
    public static final RegistrySupplier<RecipeType<HandCrankPressRecipe>> TYPE =
            TYPES.register("hand_crank_press", () -> new RecipeType<>() {
                @Override public String toString() { return "solareum:hand_crank_press"; }
            });

    private ModPressRecipes() {}

    public static void init() {
        SERIALIZERS.register();
        TYPES.register();
    }
}
