package io.bay4lly.javablockitem.backport.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelElement;
import net.minecraft.client.render.model.json.ModelOverride;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Mixin(JsonUnbakedModel.class)
public interface JsonUnbakedModelAccessor {
    @Accessor("parentId")
    Identifier getParentId();

    @Accessor("textureMap")
    Map<String, Either<SpriteIdentifier, String>> getTextureMap();
}
