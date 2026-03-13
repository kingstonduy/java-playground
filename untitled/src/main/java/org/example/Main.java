package org.example;

import org.example.component.TemplateDTO;

public class Main {

    public static void main(String[] args) {
        String requestStr = "{\n" +
                "    \"header\": {\n" +
                "      \"components\": [\n" +
                "        {\n" +
                "          \"type\": \"LOGO\",\n" +
                "          \"urlLight\": \"https://stc-oa.zdn.vn/uploads/2023/06/26/c6780f039e7695ae78c033b7db543d63.png\",\n" +
                "          \"urlDark\": \"https://stc-oa.zdn.vn/uploads/2023/06/26/17f8e1ce736c9a0fd2f6d49ae79b0d0d.png\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    \"body\": {\n" +
                "      \"components\": [\n" +
                "        {\n" +
                "          \"type\": \"TITLE\",\n" +
                "          \"value\": \"ZBank - Chọn thẻ tín dụng phù hợp với bạn\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"type\": \"PARAGRAPH\",\n" +
                "          \"value\": \"Chúng tôi rất vui vì trong rất nhiều lựa chọn, bạn đã luôn chọn sử dụng các sản phẩm của <span class=\\\"param\\\">customer_name</span>.\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"type\": \"PARAGRAPH\",\n" +
                "          \"value\": \"Cam on lan 2\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"type\": \"CAROUSEL\",\n" +
                "          \"items\": [\n" +
                "            {\n" +
                "              \"components\": [\n" +
                "                {\n" +
                "                  \"type\": \"IMAGE\",\n" +
                "                  \"value\": \"https://stc-oa.zdn.vn/uploads/2026/01/07/b87d26d164ca5d89e6ccfc0cf6e9f599.jpg\"\n" +
                "                },\n" +
                "                {\n" +
                "                  \"type\": \"TITLE\",\n" +
                "                  \"value\": \"Thẻ Tín Dụng UOB PRVI Miles\"\n" +
                "                },\n" +
                "                {\n" +
                "                  \"type\": \"PARAGRAPH\",\n" +
                "                  \"value\": \"Tích lũy 3X Điểm Thưởng cho mỗi 25.000 VND chi tiêu ngoại tệ.\"\n" +
                "                },\n" +
                "                {\n" +
                "                  \"type\": \"BUTTONS\",\n" +
                "                  \"items\": [\n" +
                "                    {\n" +
                "                      \"actionType\": \"2\",\n" +
                "                      \"tag\": 2,\n" +
                "                      \"buttonId\": \"5536240640450383685\",\n" +
                "                      \"text\": \"Quan tâm OA\",\n" +
                "                      \"data\": \"https://oa.zalo.me/3971369986576822756\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                      \"actionType\": \"1\",\n" +
                "                      \"tag\": 1,\n" +
                "                      \"buttonId\": \"2742805079633053055\",\n" +
                "                      \"text\": \"Liên hệ bộ phận CSKH\",\n" +
                "                      \"data\": \"0782947788\"\n" +
                "                    }\n" +
                "                  ]\n" +
                "                }\n" +
                "              ]\n" +
                "            },\n" +
                "            {\n" +
                "              \"components\": [\n" +
                "                {\n" +
                "                  \"type\": \"IMAGE\",\n" +
                "                  \"value\": \"https://stc-oa.zdn.vn/uploads/2026/01/07/b87d26d164ca5d89e6ccfc0cf6e9f599.jpg\"\n" +
                "                },\n" +
                "                {\n" +
                "                  \"type\": \"TITLE\",\n" +
                "                  \"value\": \"Thẻ Tín Dụng UOB PRVI Miles\"\n" +
                "                },\n" +
                "                {\n" +
                "                  \"type\": \"PARAGRAPH\",\n" +
                "                  \"value\": \"Tích lũy 3X Điểm Thưởng cho mỗi 25.000 VND chi tiêu ngoại tệ.\"\n" +
                "                },\n" +
                "                {\n" +
                "                  \"type\": \"BUTTONS\",\n" +
                "                  \"items\": [\n" +
                "                    {\n" +
                "                      \"actionType\": \"2\",\n" +
                "                      \"tag\": 2,\n" +
                "                      \"buttonId\": \"5536240640450383685\",\n" +
                "                      \"text\": \"Quan tâm OA\",\n" +
                "                      \"data\": \"https://oa.zalo.me/3971369986576822756\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                      \"actionType\": \"1\",\n" +
                "                      \"tag\": 1,\n" +
                "                      \"buttonId\": \"2742805079633053055\",\n" +
                "                      \"text\": \"Liên hệ bộ phận CSKH\",\n" +
                "                      \"data\": \"0782947788\"\n" +
                "                    }\n" +
                "                  ]\n" +
                "                }\n" +
                "              ]\n" +
                "            }\n" +
                "          ]\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    \"footer\": {\n" +
                "      \"components\": []\n" +
                "    },\n" +
                "    \"params\": [\n" +
                "      {\n" +
                "        \"name\": \"sample_title \",\n" +
                "        \"type\": 0,\n" +
                "        \"maxLength\": 30,\n" +
                "        \"isRequired\": true\n" +
                "      },\n" +
                "      {\n" +
                "        \"name\": \"sample_param_1\",\n" +
                "        \"type\": 0,\n" +
                "        \"maxLength\": 30,\n" +
                "        \"isRequired\": true\n" +
                "      },\n" +
                "      {\n" +
                "        \"name\": \"sample_param_1\",\n" +
                "        \"type\": 0,\n" +
                "        \"maxLength\": 30,\n" +
                "        \"isRequired\": true\n" +
                "      }\n" +
                "    ]\n" +
                "  }";

        try {
            TemplateDTO dto = TemplateParser.getInstance().parse(requestStr);
            String actual = TemplateParser.getInstance().toJson(dto);
            // minify the s then compare with actual
            String expected = requestStr.replaceAll("\\s+", "");
            if (!expected.equals(actual)) {
                System.out.println("Expected: " + expected);
                System.out.println("Actual: " + actual);
            }
            int debug = 1;


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    boolean checkZInstant(TemplateDTO dto) {
        String expected = "{\n" +
                "    \"sections\": [\n" +
                "        {\n" +
                "            \"logo\": {\n" +
                "                \"light\": \"https://stc-oa.zdn.vn/uploads/2023/06/26/c6780f039e7695ae78c033b7db543d63.png\",\n" +
                "                \"dark\": \"https://stc-oa.zdn.vn/uploads/2023/06/26/17f8e1ce736c9a0fd2f6d49ae79b0d0d.png\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"title\": \"ZBank - Chọn thẻ tín dụng phù hợp với bạn\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"paragraph\": \"Chúng tôi rất vui vì trong rất nhiều lựa chọn, bạn đã luôn chọn sử dụng các sản phẩm của <span class=\\\"param\\\">customer_name</span>.\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"paragraph\": \"Cam on lan 2\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"carousel\": [\n" +
                "                {\n" +
                "                    \"image\": \"https://stc-oa.zdn.vn/uploads/2026/01/07/b87d26d164ca5d89e6ccfc0cf6e9f599.jpg\",\n" +
                "                    \"title\": \"Thẻ Tín Dụng UOB PRVI Miles\",\n" +
                "                    \"paragraph\": \"Tích lũy 3X Điểm Thưởng cho mỗi 25.000 VND chi tiêu ngoại tệ.\",\n" +
                "                    \"buttons\": [\n" +
                "                        {\n" +
                "                            \"text\": \"Quan tâm OA\",\n" +
                "                            \"data\": \"{\\\"h5_src_open\\\":1110,\\\"url\\\":\\\"https://oa.zalo.me/3971369986576822756\\\"}\",\n" +
                "                            \"type\": \"btn-primary\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"text\": \"Liên hệ bộ phận CSKH\",\n" +
                "                            \"data\": \"{\\\"phoneCode\\\":\\\"0938938821\\\"}\",\n" +
                "                            \"type\": \"btn-neutral\"\n" +
                "                        }\n" +
                "                    ]\n" +
                "                },\n" +
                "                {\n" +
                "                    \"image\": \"https://stc-oa.zdn.vn/uploads/2026/01/07/a9014c457aebb2002f5355dcb34fa8c0.png\",\n" +
                "                    \"title\": \"Thẻ Tín Dụng OCB\",\n" +
                "                    \"paragraph\": \"Tích lũy 3X Điểm Thưởng cho mỗi 25.000 VND chi tiêu ngoại tệ.\",\n" +
                "                    \"buttons\": [\n" +
                "                        {\n" +
                "                            \"text\": \"Đăng ký mở thẻ 2\",\n" +
                "                            \"type\": \"btn-primary\"\n" +
                "                        }\n" +
                "                    ]\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    ]\n" +
                "}\n{\n" +
                "    \"sections\": [\n" +
                "        {\n" +
                "            \"logo\": {\n" +
                "                \"light\": \"https://stc-oa.zdn.vn/uploads/2023/06/26/c6780f039e7695ae78c033b7db543d63.png\",\n" +
                "                \"dark\": \"https://stc-oa.zdn.vn/uploads/2023/06/26/17f8e1ce736c9a0fd2f6d49ae79b0d0d.png\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"title\": \"ZBank - Chọn thẻ tín dụng phù hợp với bạn\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"paragraph\": \"Chúng tôi rất vui vì trong rất nhiều lựa chọn, bạn đã luôn chọn sử dụng các sản phẩm của <span class=\\\"param\\\">customer_name</span>.\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"paragraph\": \"Cam on lan 2\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"carousel\": [\n" +
                "                {\n" +
                "                    \"image\": \"https://stc-oa.zdn.vn/uploads/2026/01/07/b87d26d164ca5d89e6ccfc0cf6e9f599.jpg\",\n" +
                "                    \"title\": \"Thẻ Tín Dụng UOB PRVI Miles\",\n" +
                "                    \"paragraph\": \"Tích lũy 3X Điểm Thưởng cho mỗi 25.000 VND chi tiêu ngoại tệ.\",\n" +
                "                    \"buttons\": [\n" +
                "                        {\n" +
                "                            \"text\": \"Quan tâm OA\",\n" +
                "                            \"data\": \"{\\\"h5_src_open\\\":1110,\\\"url\\\":\\\"https://oa.zalo.me/3971369986576822756\\\"}\",\n" +
                "                            \"type\": \"btn-primary\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"text\": \"Liên hệ bộ phận CSKH\",\n" +
                "                            \"data\": \"{\\\"phoneCode\\\":\\\"0938938821\\\"}\",\n" +
                "                            \"type\": \"btn-neutral\"\n" +
                "                        }\n" +
                "                    ]\n" +
                "                },\n" +
                "                {\n" +
                "                    \"image\": \"https://stc-oa.zdn.vn/uploads/2026/01/07/a9014c457aebb2002f5355dcb34fa8c0.png\",\n" +
                "                    \"title\": \"Thẻ Tín Dụng OCB\",\n" +
                "                    \"paragraph\": \"Tích lũy 3X Điểm Thưởng cho mỗi 25.000 VND chi tiêu ngoại tệ.\",\n" +
                "                    \"buttons\": [\n" +
                "                        {\n" +
                "                            \"text\": \"Đăng ký mở thẻ 2\",\n" +
                "                            \"type\": \"btn-primary\"\n" +
                "                        }\n" +
                "                    ]\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    ]\n" +
                "}\n";
        String actual = TemplateParser.getInstance().toJson(dto);
        // minify the s then compare with actual
        expected = expected.replaceAll("\\s+", "");
        return expected.equals(actual);
    }
}
