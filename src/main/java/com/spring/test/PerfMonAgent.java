package com.spring.test;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

public class PerfMonAgent {
    static private Instrumentation inst = null;

    public static void premain(String agentArgs,Instrumentation _inst){
        System.out.println("PerfMonAgent.premain() was called.");
        // 初始化静态变量
        inst = _inst;
        //设置 class-file transformer
        ClassFileTransformer trans = new PerfMonXTransformer();
        System.out.println("Adding a PerMonXformer instance to the JVM 。");
        inst.addTransformer(trans);

    }
}
