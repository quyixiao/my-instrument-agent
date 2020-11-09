package com.spring.test;


import javassist.*;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;


public class PerfMonXTransformer implements ClassFileTransformer {


    @Override
    public final byte[] transform(final ClassLoader loader, final String classFile, final Class<?> classBeingRedefined,
                                  final ProtectionDomain protectionDomain, final byte[] classFileBuffer) {
        if (Utils.isNotNull(classFile)) {

            try {
                final CtClass ctClass = Utils.getCtClass(classFileBuffer, loader);

                for (CtBehavior method : ctClass.getDeclaredBehaviors()) {

                    if (method.getName().contains("$")) {
                        continue;
                    }
                    // init variable tracer
                    if ("testMyName".equals(method.getName())) {
                        System.out.println(method.getName());
                        doMethod(ctClass,method);
                        // addTiming(ctClass,method.getName());
                    }
                }
                if(classFile.contains("App")){
                    System.out.println("=======================" + classFile);
                    ctClass.writeFile("/Users/quyixiao/git/my-instrument-agent/src/main/java/com/spring/test"); //将上面构造好的类写入到:/Temp中
                }

                return ctClass.toBytecode();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static void doMethod(CtClass clazz, CtBehavior ctBehavior) throws NotFoundException, CannotCompileException {
        String method = ctBehavior.getName();
        //获取方法信息,如果方法不存在，则抛出异常
        CtMethod ctMethod = clazz.getDeclaredMethod(method);
        //将旧的方法名称进行重新命名，并生成一个方法的副本，该副本方法采用了过滤器的方式
        String nname = method + "$impl";
        ctMethod.setName(nname);
        CtMethod newCtMethod = CtNewMethod.copy(ctMethod, method, clazz, null);

        /*
         * 为该方法添加时间过滤器，用来计算时间。
         * 这就需要我们去判断获取时间的方法是否具有返回值
         */
        String type = ctMethod.getReturnType().getName();
        StringBuffer body = new StringBuffer();
        body.append("{\n long start = System.nanoTime();\n");

        if(!"void".equals(type)) {
            body.append(type + " result = ");
        }

        //可以通过$$将传递给拦截器的参数，传递给原来的方法
        body.append(nname + "($$);\n");

        //  finish body text generation with call to print the timing
        //  information, and return saved value (if not void)
        body.append("System.out.println(\"Call to method " + nname + " took \" + \n (System.nanoTime()-start) + " +  "\" ms.\");\n");
        if(!"void".equals(type)) {
            body.append("return result;\n");
        }

        body.append("}");

        //替换拦截器方法的主体内容，并将该方法添加到class之中
        newCtMethod.setBody(body.toString());
        clazz.addMethod(newCtMethod);

        //输出拦截器的代码块
        System.out.println("拦截器方法的主体:");
        System.out.println(body.toString());
    }

    private void doMethodBak(CtClass ctClass,CtBehavior method ) throws NotFoundException, CannotCompileException {
        StringBuffer body = new StringBuffer();
        body.append("{\n long time = System.nanoTime();\n");
        body.append("System.out.println(\"Call to method  took \" + \n (System.nanoTime()-time) + " +  "\" ms.\");\n");
        body.append("}");
        method.setBody(body.toString());
        //method.insertBefore("long time = System.nanoTime();");
        //method.insertAfter("System.out.println(System.nanoTime());");
    }
}
