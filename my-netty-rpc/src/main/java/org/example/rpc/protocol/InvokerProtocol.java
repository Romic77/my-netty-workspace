package org.example.rpc.protocol;

import java.io.Serializable;

import lombok.Data;

/**
 * @author romic
 * @date 2022-01-08
 * @description
 */
@Data
public class InvokerProtocol implements Serializable {
    /**
     * 全限定类名
     */
    private String className;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 形参列表
     */
    private Class<?>[] params;

    /**
     * 实参列表
     */
    private Object[] values;
}
