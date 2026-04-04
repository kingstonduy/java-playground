package org.example.dto.component;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.zinstant.components.IZinstantComponent;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class CarouselComponent extends AbstractBaseComponent implements IZInstantConvertible {
    private List<Item> items;

    @Override
    public String getName() {
        return EComponent.CAROUSEL.getValue();
    }

    @Override
    public IZinstantComponent toZInstant() {
        throw new UnsupportedOperationException("CarouselComponent cannot be converted to ZInstantComponent");
    }

    @Data
    public static class Item {
        private List<IComponent> components;
    }
}
