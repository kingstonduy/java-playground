package org.example.zinstant.components;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ZInstantCarousel implements IZinstantComponent, IArrayAttribute {
    private List<Item> value;

    @Override
    public String getName() {
        return EZInstantComponent.CAROUSEL.getValue();
    }

    @Override
    public List<?> getArray() {
        return this.value;
    }

    @Data
    @Accessors(chain = true)
    public static class Item {
        private List<IZinstantComponent> components;
    }
}
