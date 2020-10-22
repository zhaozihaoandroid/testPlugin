package com.domain.android.testplugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

class TestPluginTransform extends Transform{

    Project project

    TestPluginTransform(Project project){
        this.project=project
    }

    @Override
    String getName() {
        return "TestPluginTransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        super.transform(context, inputs, referencedInputs, outputProvider, isIncremental)

        println("transform old")
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)

        println("transform")

        def addCode='android.util.Log.i("plugin","plugin info");'


        def outputProvider=transformInvocation.outputProvider

        transformInvocation.inputs.each { TransformInput it ->
            // scan all jars
            it.jarInputs.each { JarInput jarInput ->
                String destName = jarInput.name
                // rename jar files
                def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath)
                if (destName.endsWith(".jar")) {
                    destName = destName.substring(0, destName.length() - 4)
                }
                // input file
                File src = jarInput.file
                // output file
                File dest = outputProvider.getContentLocation(destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR)

                FileUtils.copyFile(src, dest)

            }
            println("transform jars end")
            // scan class files
            it.directoryInputs.each { DirectoryInput directoryInput ->
                File dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                println("transform Directory add before")
//                AddCodeUtils.addCode(directoryInput.file.absolutePath, project, addCode)
                String root = directoryInput.file.absolutePath
                if (!root.endsWith(File.separator))
                    root += File.separator
                directoryInput.file.eachFileRecurse { File f->
                    def path=f.getAbsolutePath().replace(root,"")

                    if (f.isFile()&& path.startsWith("com/domain/android/test/")) {
                        println(path)
                        def bytes = Util.scanClass(f, path)
                        def outputStream = new FileOutputStream(f.path)
                        outputStream.write(bytes)
                        outputStream.close()
                    }
                }

                // copy to dest
                FileUtils.copyDirectory(directoryInput.file, dest)
            }
        }
    }
}