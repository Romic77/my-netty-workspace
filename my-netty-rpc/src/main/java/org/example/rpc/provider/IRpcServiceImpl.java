package org.example.rpc.provider;

import org.example.rpc.api.IRpcService;

/**
 * @author romic
 * @date 2022-01-08
 * @description
 */
public class IRpcServiceImpl implements IRpcService {
    /**
     * 加
     *
     * @param a
     * @param b
     * @return
     */
    @Override
    public int add(int a, int b) {
        return a + b;
    }

    /**
     * 减
     *
     * @param a
     * @param b
     * @return
     */
    @Override
    public int sub(int a, int b) {
        return a - b;
    }

    /**
     * 乘
     *
     * @param a
     * @param b
     * @return
     */
    @Override
    public int mult(int a, int b) {
        return a * b;
    }

    /**
     * 除
     *
     * @param a
     * @param b
     * @return
     */
    @Override
    public int div(int a, int b) {
        return a / b;
    }
}
