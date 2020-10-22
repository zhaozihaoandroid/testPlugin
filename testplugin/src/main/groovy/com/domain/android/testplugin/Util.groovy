package com.domain.android.testplugin


import org.objectweb.asm.*

class Util{

    public static byte[] scanClass(File file,String path){
        def inputStream = new FileInputStream(file)
        ClassReader cr = new ClassReader(inputStream)
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
        ScanClassVisitor cv = new ScanClassVisitor(Opcodes.ASM5, cw,path)
        cr.accept(cv, ClassReader.EXPAND_FRAMES)

        def byteArray = cw.toByteArray()
        inputStream.close()
        return byteArray
    }


    static class ScanClassVisitor extends ClassVisitor{
        private String superName
        String path
        boolean isHasOnCreate=false
        ScanClassVisitor(int api, ClassVisitor cv,String path) {
            super(api, cv)
            this.path=path


        }

        @Override
        void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.superName=superName
            super.visit(version, access, name, signature, superName, interfaces)
        }

        @Override
        MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv=super.visitMethod(access, name, desc, signature, exceptions)
            println("method name"+name+"...desc="+desc)

            if (isActivity(superName)&&name=="onCreate"&&desc=="(Landroid/os/Bundle;)V"){
                println("superName"+superName)
                mv=new InsertCodeClassVisitor(Opcodes.ASM5,mv,path)
                isHasOnCreate=true
            }

            return mv
        }

        @Override
        void visitEnd() {
            if (isActivity(superName)&&!isHasOnCreate){
                println("activity end")
                MethodVisitor methodVisitor = visitMethod(Opcodes.ACC_PUBLIC, "onCreate", "(Landroid/os/Bundle;)V", null, null)
                methodVisitor.visitVarInsn(Opcodes.ALOAD,0)
                methodVisitor.visitVarInsn(Opcodes.ALOAD,1)
                methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL,
                superName,"onCreate","(Landroid/os/Bundle;)V",false)

                methodVisitor.visitInsn(Opcodes.RETURN)

                methodVisitor.visitMaxs(2, 2)
                isHasOnCreate=true
                methodVisitor.visitEnd()
            }
            super.visitEnd()
        }

        private static boolean isActivity(String name){
            return name=="androidx/appcompat/app/AppCompatActivity" || name=="com/domain/android/test/MainActivity"
        }
    }

    static class InsertCodeClassVisitor extends MethodVisitor{

        String path

        InsertCodeClassVisitor(int api, MethodVisitor mv,String path) {
            super(api, mv)
            this.path=path
        }

        @Override
        void visitInsn(int opcode) {
            if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)) {
                mv.visitLdcInsn("TAG")
                mv.visitLdcInsn(path)
                mv.visitMethodInsn(Opcodes.INVOKESTATIC
                ,"android/util/log"
                ,"i"
                ,"(Ljava/lang/String;Ljava/lang/String;)I"
                ,false)


                mv.visitVarInsn(Opcodes.ALOAD,0)
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL
                        ,"java/lang/Object","getClass","()Ljava/lang/Class;",false)

                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL
                ,"java/lang/Class","getSimpleName","()Ljava/lang/String;",false)
                mv.visitMethodInsn(Opcodes.INVOKESTATIC
                        ,"com/domain/android/test/trace/Trace"
                        ,"traceInPage"
                        ,"(Ljava/lang/String;)V"
                        ,false)

            }


            super.visitInsn(opcode)
        }

    }



}