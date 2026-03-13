package org.example.component;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.zinstant.IZinstantComponent;

@EqualsAndHashCode(callSuper = true)
@Data
public class LogoComponent extends AbstractBaseComponent implements IZInstantConvertible {
    private String urlLight;
    private String urlDark;

    @Override
    public String getName() {
        return EComponent.LOGO.getValue();
    }

    @Override
    public IZinstantComponent toZInstant() {
        throw new UnsupportedOperationException("LogoComponent cannot be converted to ZInstantComponent");
    }
}
