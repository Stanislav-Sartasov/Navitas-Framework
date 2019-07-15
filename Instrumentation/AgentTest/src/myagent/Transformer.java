package myagent;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class Transformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        byte[] byteCode = classfileBuffer;

        //if (className.contains("jarhacking")) {
            System.out.println("Instrumenting");
            try {
                ClassPool classPool = ClassPool.getDefault();
                CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
                CtMethod[] methods = ctClass.getDeclaredMethods();

                for (CtMethod method : methods) {
                    //method.insertBefore("System.out.println(\"This is instrumented code\");");
                    System.out.println("-->[" + ctClass.getName() + "] invoked [" + method.getName() + "]");
                }

                byteCode = ctClass.toBytecode();
                ctClass.detach();
                System.out.println("Instrumentation completed");
            } catch (Throwable ex) {
                System.out.println("==Exception " + ex);
                ex.printStackTrace();
            }
        //}
        return byteCode;
    }
}
