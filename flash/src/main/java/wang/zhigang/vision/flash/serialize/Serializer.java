/******************************************************************
 *
 *    Powered By hhdd.com.
 *
 *    Copyright (c) 2001-2023
 *    https://kada.hhdd.com/
 *
 *    Package:     wang.zhigang.vision.serialize
 *
 *    Filename:    Serializer
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
 *    Create at:   2026/2/11 11:34
 *
 *    Revision:
 *
 *    2026/2/11 11:34
 *        - first revision
 *
 *****************************************************************/
package wang.zhigang.vision.flash.serialize;

import wang.zhigang.vision.flash.serialize.impl.JSONSerializer;

/**
 * Serializer
 *
 * @author wangzg
 * @version 1.0.0
 * @create 2026/2/11 11:34
 */
public interface Serializer {

    Serializer DEFAULT = new JSONSerializer();

    /**
     * 序列化算法
     * @return
     */
    byte getSerializerAlgorithm();

    byte[] serialize(Object object);

    <T> T deserialize(Class<T> clazz, byte[] bytes);


}