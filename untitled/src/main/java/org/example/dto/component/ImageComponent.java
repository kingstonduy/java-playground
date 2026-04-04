package org.example.dto.component;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.zinstant.components.IZinstantComponent;

@EqualsAndHashCode(callSuper = true)
@Data
public class ImageComponent extends AbstractBaseComponent implements IZInstantConvertible {
    private String value;

    @Override
    public String getName() {
        return EComponent.IMAGE.getValue();
    }

    @Override
    public IZinstantComponent toZInstant() {
        throw new UnsupportedOperationException("ImageComponent cannot be converted to ZInstantComponent");
    }
}
