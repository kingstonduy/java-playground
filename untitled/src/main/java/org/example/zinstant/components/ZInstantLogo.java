package org.example.zinstant.components;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ZInstantLogo implements IZinstantComponent, IObjectAttribute {
    private String light;
    private String dark;

    @Override
    public String getName() {
        return EZInstantComponent.LOGO.getValue();
    }

    @Override
    public Object getObject() {
        return this;
    }
}
