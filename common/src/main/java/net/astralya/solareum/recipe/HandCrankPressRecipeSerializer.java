package net.astralya.solareum.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class HandCrankPressRecipeSerializer implements RecipeSerializer<HandCrankPressRecipe> {
    private static final MapCodec<HandCrankPressRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(HandCrankPressRecipe::ingredient),
            com.mojang.serialization.Codec.intRange(1, 64).fieldOf("input_count").forGetter(HandCrankPressRecipe::inputCount),
            ItemStack.STRICT_CODEC.fieldOf("output").forGetter(HandCrankPressRecipe::output)
    ).apply(instance, HandCrankPressRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, HandCrankPressRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, HandCrankPressRecipe::ingredient,
            ByteBufCodecs.VAR_INT, HandCrankPressRecipe::inputCount,
            ItemStack.STREAM_CODEC, HandCrankPressRecipe::output,
            HandCrankPressRecipe::new);

    @Override
    public MapCodec<HandCrankPressRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, HandCrankPressRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
