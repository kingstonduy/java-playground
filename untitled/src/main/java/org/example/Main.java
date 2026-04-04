package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import org.example.dto.TemplateDTO;
import org.example.dto.component.ComponentParser;
import org.example.dto.component.IComponent;
import org.example.zinstant.ZInstantDTO;
import org.example.zinstant.ZInstantParser;
import org.example.zinstant.components.*;

import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        checkZInstant();
    }

    public static boolean checkDTO() {
        String expected = """
                {
                  "type": "CAROUSEL",
                  "items": [
                    {
                      "components": [
                        {
                          "type": "IMAGE",
                          "value": "https://stc-oa.zdn.vn/uploads/2026/01/07/b87d26d164ca5d89e6ccfc0cf6e9f599.jpg"
                        },
                        {
                          "type": "TITLE",
                          "value": "Thẻ Tín Dụng UOB PRVI Miles"
                        },
                        {
                          "type": "PARAGRAPH",
                          "value": "Tích lũy 3X Điểm Thưởng cho mỗi 25.000 VND chi tiêu ngoại tệ."
                        },
                        {
                          "type": "BUTTONS",
                          "items": [
                            {
                              "actionType": "2",
                              "tag": 2,
                              "buttonId": "5536240640450383685",
                              "text": "Quan tâm OA",
                              "data": "https://oa.zalo.me/3971369986576822756"
                            },
                            {
                              "actionType": "1",
                              "tag": 1,
                              "buttonId": "2742805079633053055",
                              "text": "Liên hệ bộ phận CSKH",
                              "data": "0782947788"
                            }
                          ]
                        }
                      ]
                    },
                    {
                      "components": [
                        {
                          "type": "IMAGE",
                          "value": "https://stc-oa.zdn.vn/uploads/2026/01/07/b87d26d164ca5d89e6ccfc0cf6e9f599.jpg"
                        },
                        {
                          "type": "TITLE",
                          "value": "Thẻ Tín Dụng UOB PRVI Miles"
                        },
                        {
                          "type": "PARAGRAPH",
                          "value": "Tích lũy 3X Thưởng cho mỗi 25.000 VND chi tiêu ngoại tệ."
                        },
                        {
                          "type": "BUTTONS",
                          "items": [
                            {
                              "actionType": "2",
                              "tag": 2,
                              "buttonId": "5536240640450383685",
                              "text": "Quan tâm OA",
                              "data": "https://oa.zalo.me/3971369986576822756"
                            },
                            {
                              "actionType": "1",
                              "tag": 1,
                              "buttonId": "2742805079633053055",
                              "text": "Liên hệ bộ phận CSKH",
                              "data": "0782947788"
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
                                """;

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(IComponent.class, new ComponentParser())
                .setPrettyPrinting()
                .create();

        try {
            TemplateDTO dto = gson.fromJson(expected, TemplateDTO.class);
            String actual = gson.toJson(dto);
            // minify the s then compare with actual
            // ---- compare JSON structures ----

            JsonElement actualTree = gson.fromJson(actual, JsonElement.class);
            JsonElement expectedTree = gson.fromJson(expected, JsonElement.class);

            if (!actualTree.equals(expectedTree)) {
                System.out.println("Expected: " + expected);
                System.out.println("Actual: " + actual);
            } else {
                System.out.println("The actual JSON matches the expected JSON structure.");
            }
            return actualTree.equals(expectedTree);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean checkZInstant() {
        String expected = """
                {
                  "sections": [
                    {
                      "logo": {
                        "light": "https://stc-oa.zdn.vn/uploads/2023/06/26/c6780f039e7695ae78c033b7db543d63.png",
                        "dark": "https://stc-oa.zdn.vn/uploads/2023/06/26/17f8e1ce736c9a0fd2f6d49ae79b0d0d.png"
                      }
                    },
                    {
                      "title": "ZBank - Chọn thẻ tín dụng phù hợp với bạn"
                    },
                    {
                      "paragraph": "Chúng tôi rất vui vì trong rất nhiều lựa chọn, bạn đã luôn chọn sử dụng các sản phẩm của <span class=\\"param\\">customer_name</span>."
                    },
                    {
                      "paragraph": "Cam on lan 2"
                    },
                    {
                      "carousel": [
                        {
                          "image": "https://stc-oa.zdn.vn/uploads/2026/01/07/b87d26d164ca5d89e6ccfc0cf6e9f599.jpg",
                          "title": "Thẻ Tín Dụng UOB PRVI Miles",
                          "paragraph": "Tích lũy 3X Điểm Thưởng cho mỗi 25.000 VND chi tiêu ngoại tệ.",
                          "buttons": [
                            {
                              "text": "Quan tâm OA",
                              "data": "{\\"h5_src_open\\":1110,\\"url\\":\\"https://oa.zalo.me/3971369986576822756\\"}",
                              "type": "btn-primary"
                            },
                            {
                              "text": "Liên hệ bộ phận CSKH",
                              "data": "{\\"phoneCode\\":\\"0938938821\\"}",
                              "type": "btn-neutral"
                            }
                          ]
                        },
                        {
                          "image": "https://stc-oa.zdn.vn/uploads/2026/01/07/a9014c457aebb2002f5355dcb34fa8c0.png",
                          "title": "Thẻ Tín Dụng OCB",
                          "paragraph": "Tích lũy 3X Điểm Thưởng cho mỗi 25.000 VND chi tiêu ngoại tệ.",
                          "buttons": [
                            {
                              "text": "Đăng ký mở thẻ 2",
                              "type": "btn-primary"
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
                """;
        // ---- construct components ----

        ZInstantLogo logo = new ZInstantLogo();
        logo.setLight("https://stc-oa.zdn.vn/uploads/2023/06/26/light.png");
        logo.setDark("https://stc-oa.zdn.vn/uploads/2023/06/26/dark.png");

        ZInstantTitle title = new ZInstantTitle();
        title.setData("ZBank - Chọn thẻ tín dụng phù hợp với bạn");

        ZInstantParagraph p1 = new ZInstantParagraph();
        p1.setValue("Chúng tôi rất vui vì trong rất nhiều lựa chọn, bạn đã luôn chọn sử dụng các sản phẩm của <span class=\\\"param\\\">customer_name</span>.");

        ZInstantParagraph p2 = new ZInstantParagraph();
        p2.setValue("Cam on lan 2");

        ZInstantCarousel carousel = new ZInstantCarousel().setValue(Arrays.asList(
                new ZInstantCarousel.Item().setComponents(Arrays.<IZinstantComponent>asList(
                        new ZInstantImage()
                                .setValue("https://stc-oa.zdn.vn/uploads/2026/01/07/b87d26d164ca5d89e6ccfc0cf6e9f599.jpg"),

                        new ZInstantTitle()
                                .setData("Thẻ Tín Dụng UOB PRVI Miles"),

                        new ZInstantParagraph()
                                .setValue("Tích lũy 3X Điểm Thưởng cho mỗi 25.000 VND chi tiêu ngoại tệ."),

                        new ZInstantButton().setValue(Arrays.asList(
                                new ZInstantButton.Item()
                                        .setText("Quan tâm OA")
                                        .setData("{\"h5_src_open\":1110,\"url\":\"https://oa.zalo.me/3971369986576822756\"}")
                                        .setType("btn-primary"),

                                new ZInstantButton.Item()
                                        .setText("Liên hệ bộ phận CSKH")
                                        .setData("{\"phoneCode\":\"0938938821\"}")
                                        .setType("btn-neutral")
                        ))
                ))
        ));


        // ---- construct DTO ----

        ZInstantDTO dto = new ZInstantDTO();
        dto.setSections(List.of(
                logo,
                title,
                p1,
                p2,
                carousel
        ));

        // ---- gson setup ----

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(IZinstantComponent.class, new ZInstantParser())
                .setPrettyPrinting()
                .create();

        String actualJson = gson.toJson(dto);

        // ---- compare JSON structures ----
        try {
            System.out.println(gson.toJson(dto));
            JsonElement actualTree = gson.fromJson(actualJson, JsonElement.class);
            JsonElement expectedTree = gson.fromJson(expected, JsonElement.class);

            if (!actualTree.equals(expectedTree)) {
                System.out.println("Expected: " + expected);
                System.out.println("Actual: " + actualJson);
            } else {
                System.out.println("The actual JSON matches the expected JSON structure.");
            }
            return actualTree.equals(expectedTree);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
