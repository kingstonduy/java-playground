package org.example;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.dto.component.IComponent;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Section {
    @SerializedName("components")
    protected List<IComponent> components;
}
