package org.example.zinstant.components;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ZInstantTitle implements IZinstantComponent, ISingleAttribute {
    private String data;

    @Override
    public String getName() {
        return EZInstantComponent.TITLE.getValue();
    }

    @Override
    public Object getAttribute() {
        return this.data;
    }
}
