package org.example.zinstant.components;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ZInstantParagraph implements IZinstantComponent, ISingleAttribute {
    private String value;

    @Override
    public String getName() {
        return EZInstantComponent.PARAGRAPH.getValue();
    }

    @Override
    public Object getAttribute() {
        return this.value;
    }
}
