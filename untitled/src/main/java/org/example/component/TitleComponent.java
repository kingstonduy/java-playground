package org.example.component;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.zinstant.IZinstantComponent;

@EqualsAndHashCode(callSuper = true)
@Data
public class TitleComponent extends AbstractBaseComponent implements IZInstantConvertible {
    private String value;

    @Override
    public String getName() {
        return EComponent.TITLE.getValue();
    }

    @Override
    public IZinstantComponent toZInstant() {
        throw new UnsupportedOperationException("TitleComponent cannot be converted to ZInstantComponent");
    }
}
