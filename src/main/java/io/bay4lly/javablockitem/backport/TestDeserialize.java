package io.bay4lly.javablockitem.backport;

import net.minecraft.client.render.model.json.JsonUnbakedModel;

public class TestDeserialize {
    public static void test() {
        JsonUnbakedModel model = JsonUnbakedModel.deserialize("{}");
    }
}
