package org.example.dto;

import com.google.gson.annotations.SerializedName;
import org.example.ParamSection;
import org.example.Section;

import java.util.List;

public class TemplateDTO {
    @SerializedName("header")
    private Section headerSection;

    @SerializedName("body")
    private Section bodySection;

    @SerializedName("footer")
    private Section footerSection;

    @SerializedName("params")
    private List<ParamSection> paramSectionList;
}
