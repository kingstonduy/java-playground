package org.example.dto.component;

import com.google.gson.annotations.SerializedName;

public class AbstractBaseComponent implements IComponent {
    @SerializedName("type")
    protected String name;

    @Override
    public String getName() {
        return name;
    }
}
