/******************************************************************
 *
 *    Powered By hhdd.com.
 *
 *    Copyright (c) 2001-2023
 *    https://kada.hhdd.com/
 *
 *    Package:     wang.zhigang.vision.serialize
 *
 *    Filename:    JSONSerializer
 *
 *    Description: 
 *
 *    Copyright:   Copyright (c) 2001-2023
 *
 *    Company:     hhdd.com
 *
 *    @author: 王志刚
 *
 *    @version: 1.0.0
 *
 *    Create at:   2026/2/11 11:36
 *
 *    Revision:
 *
 *    2026/2/11 11:36
 *        - first revision
 *
 *****************************************************************/
package wang.zhigang.vision.flash.serialize.impl;

import com.alibaba.fastjson2.JSON;
import wang.zhigang.vision.flash.serialize.Serializer;
import wang.zhigang.vision.flash.serialize.SerializerAlgorithm;

/**
 * JSONSerializer
 *
 * @author wangzg
 * @version 1.0.0
 * @create 2026/2/11 11:36
 */
public class JSONSerializer implements Serializer {
    @Override
    public byte getSerializerAlgorithm() {
        return SerializerAlgorithm.JSON;
    }

    @Override
    public byte[] serialize(Object object) {
        return JSON.toJSONBytes(object);
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        return JSON.to(clazz, bytes);
    }
}