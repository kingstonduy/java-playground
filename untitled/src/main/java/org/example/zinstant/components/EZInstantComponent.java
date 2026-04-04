package org.example.zinstant.components;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum EZInstantComponent {

    LOGO("logo", ZInstantLogo.class),
    TITLE("title", ZInstantTitle.class),
    PARAGRAPH("paragraph", ZInstantParagraph.class),
    CAROUSEL("carousel", ZInstantCarousel.class),
    BUTTONS("buttons", ZInstantButton.class),
    IMAGE("image", ZInstantImage.class),
    ;

    private static final Map<String, EZInstantComponent> LOOKUP = new HashMap<>();

    static {
        for (EZInstantComponent c : values()) {
            LOOKUP.put(c.value, c);
        }
    }

    private final String value;
    private final Class<? extends IZinstantComponent> clazz;

    public static EZInstantComponent findByValue(String key) {
        return LOOKUP.get(key);
    }
}