package org.example.zinstant.components;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ZInstantButton implements IZinstantComponent, IArrayAttribute {
    private List<Item> value;

    @Override
    public String getName() {
        return EZInstantComponent.BUTTONS.getValue();
    }

    @Override
    public List<?> getArray() {
        return this.value;
    }

    @Data
    @Accessors(chain = true)
    public static class Item {
        private String text;
        private String data;
        private String type;
    }
}
