package org.example.component;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.zinstant.IZinstantComponent;

@EqualsAndHashCode(callSuper = true)
@Data
public class ParagraphComponent extends AbstractBaseComponent implements IZInstantConvertible {
    private String value;

    @Override
    public String getName() {
        return EComponent.PARAGRAPH.getValue();
    }

    @Override
    public IZinstantComponent toZInstant() {
        throw new UnsupportedOperationException("ParagraphComponent cannot be converted to ZInstantComponent");
    }
}
