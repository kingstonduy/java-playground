package org.example.zinstant;

import lombok.Data;
import org.example.zinstant.components.IZinstantComponent;

import java.util.List;

@Data
public class ZInstantDTO {
    private List<IZinstantComponent> sections;
}
