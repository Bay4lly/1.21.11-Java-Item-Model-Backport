package io.bay4lly.javablockitem.backport.loader;

import io.bay4lly.javablockitem.backport.mixin.JsonUnbakedModelAccessor;
import io.bay4lly.javablockitem.backport.model.CustomModel;
import io.bay4lly.javablockitem.backport.renderer.BackportBakedModel;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Simplified BackportUnbakedModel.
 * Texture dependencies are now handled via the ModelLoadingPlugin to avoid mapping issues.
 */
public class BackportUnbakedModel extends JsonUnbakedModel {

    private final CustomModel customModel;

    public BackportUnbakedModel(CustomModel customModel, JsonUnbakedModel sanitizedModel) {
        super(
            ((JsonUnbakedModelAccessor)sanitizedModel).getParentId(),
            sanitizedModel.getElements(),
            ((JsonUnbakedModelAccessor)sanitizedModel).getTextureMap(),
            sanitizedModel.useAmbientOcclusion(),
            sanitizedModel.getGuiLight(),
            sanitizedModel.getTransformations(),
            sanitizedModel.getOverrides()
        );
        this.customModel = customModel;
    }

    @Nullable
    @Override
    public BakedModel bake(Baker baker, Function<SpriteIdentifier, Sprite> spriteGetter, ModelBakeSettings settings, Identifier modelId) {
        // Fallback to vanilla bake logic for the basic quads
        BakedModel fallback = super.bake(baker, spriteGetter, settings, modelId);
        
        // Wrap with our custom renderer for advanced 3D rotations
        return new BackportBakedModel(customModel, fallback, modelId, texId -> {
            SpriteIdentifier spriteId = new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, texId);
            return spriteGetter.apply(spriteId);
        });
    }
}
