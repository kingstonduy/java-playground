package org.example.dto.component;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EComponent {
    TITLE("TITLE", TitleComponent.class),
    PARAGRAPH("PARAGRAPH", ParagraphComponent.class),
    IMAGE("IMAGE", ImageComponent.class),
    BUTTON("BUTTONS", ButtonComponent.class),
    LOGO("LOGO", LogoComponent.class),
    CAROUSEL("CAROUSEL", CarouselComponent.class);

    private final String value;
    private final Class<? extends IComponent> clazz;

    public static EComponent findByValue(String value) {
        for (EComponent component : EComponent.values()) {
            if (component.getValue().equals(value)) {
                return component;
            }
        }
        throw new IllegalArgumentException("Unknown component type: " + value);
    }
}
