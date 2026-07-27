package com.nageoffer.shortlink.admin.common.serialize;

import cn.hutool.core.util.DesensitizedUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * 身份证号脱敏序列化（输出JSON时自动脱敏）
 */
//序列化 (Serialize)：Java 对象 → JSON 字符串
//反序列化 (Deserialize)：JSON 字符串 → Java 对象
//
public class IdCardDesensitizationSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String idCard, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        String phoneDesensitization = DesensitizedUtil.idCardNum(idCard, 4, 4);
        jsonGenerator.writeString(phoneDesensitization);
    }
}
