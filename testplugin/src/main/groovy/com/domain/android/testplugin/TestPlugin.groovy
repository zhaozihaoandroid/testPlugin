package com.domain.android.testplugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

public class TestPlugin implements Plugin<Project>{

    @Override
    void apply(Project project) {

        def transform = new TestPluginTransform(project)

        def extension = project.extensions.findByType(AppExtension.class)


        extension.registerTransform(transform)


    }
}