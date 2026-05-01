package io.bay4lly.javablockitem.backport.loader;

import io.bay4lly.javablockitem.backport.model.CustomModel;
import io.bay4lly.javablockitem.backport.model.ModelCache;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class BackportModelLoadingPlugin implements ModelLoadingPlugin {

    private static final String VIRTUAL_NS = "backport_stitch";

    @Override
    public void onInitializeModelLoader(ModelLoadingPlugin.Context pluginContext) {
        // 1. Force textures into the atlas by registering them as virtual models
        List<Identifier> virtualModels = new ArrayList<>();
        for (Identifier texId : ModelCache.getAllUsedTextures()) {
            virtualModels.add(new Identifier(VIRTUAL_NS, texId.getNamespace() + "/" + texId.getPath()));
        }
        pluginContext.addModels(virtualModels.toArray(new Identifier[0]));

        // 2. Resolve models
        pluginContext.resolveModel().register(context -> {
            Identifier id = context.id();
            
            // Handle virtual stitching models
            if (id.getNamespace().equals(VIRTUAL_NS)) {
                String fullPath = id.getPath();
                int firstSlash = fullPath.indexOf('/');
                if (firstSlash != -1) {
                    String origNS = fullPath.substring(0, firstSlash);
                    String origPath = fullPath.substring(firstSlash + 1);
                    Identifier texId = new Identifier(origNS, origPath);
                    String dummyJson = "{ \"textures\": { \"layer0\": \"" + texId.toString() + "\" } }";
                    return JsonUnbakedModel.deserialize(dummyJson);
                }
            }
            
            // Handle our custom models
            Identifier cacheKey = getCacheKey(id);
            CustomModel customModel = ModelCache.get(cacheKey);

            if (customModel != null && ModelCache.requiresCustomRenderer(cacheKey)) {
                String sanitizedJson = customModel.getSanitizedJson();
                if (sanitizedJson == null) sanitizedJson = "{ \"textures\": { } }";
                JsonUnbakedModel sanitized = JsonUnbakedModel.deserialize(sanitizedJson);
                return new BackportUnbakedModel(customModel, sanitized);
            }

            return null;
        });
    }

    private Identifier getCacheKey(Identifier id) {
        String path = id.getPath();
        if (path.startsWith("item/")) return id;
        if (path.startsWith("block/")) return id;
        return new Identifier(id.getNamespace(), "item/" + path);
    }
}
