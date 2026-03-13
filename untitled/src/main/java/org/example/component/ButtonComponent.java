package org.example.component;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.zinstant.IZinstantComponent;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class ButtonComponent extends AbstractBaseComponent implements IZInstantConvertible {
    private List<Item> items;

    @Override
    public String getName() {
        return EComponent.BUTTON.getValue();
    }

    @Override
    public IZinstantComponent toZInstant() {
        throw new UnsupportedOperationException("CarouselComponent cannot be converted to ZInstantComponent");
    }


    @Data
    public static class Item {
        private Integer actionType;
        private Integer tag;
        private Long buttonId;
        private String text;
        private String data;
    }
}