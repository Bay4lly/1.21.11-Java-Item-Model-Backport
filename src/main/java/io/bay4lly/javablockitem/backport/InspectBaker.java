package io.bay4lly.javablockitem.backport;

import net.minecraft.client.render.model.Baker;
import java.lang.reflect.Method;

public class InspectBaker {
    public static void main(String[] args) {
        for (Method m : Baker.class.getMethods()) {
            System.out.println(m.toString());
        }
    }
}
