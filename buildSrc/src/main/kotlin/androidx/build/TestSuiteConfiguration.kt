/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("UnstableApiUsage") // Incubating AGP APIs

package androidx.build

import androidx.build.dependencyTracker.AffectedModuleDetector
import androidx.build.gradle.getByType
import com.android.build.api.artifact.ArtifactType
import com.android.build.api.artifact.Artifacts
import com.android.build.api.extension.AndroidComponentsExtension
import com.android.build.api.extension.ApplicationAndroidComponentsExtension
import com.android.build.gradle.TestedExtension
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import java.io.File

fun Project.createTestConfigurationGenerationTask(
    variantName: String,
    artifacts: Artifacts,
    minSdk: Int,
    testRunner: String
) {
    val generateTestConfigurationTask = this.tasks.register(
        "${AndroidXPlugin.GENERATE_TEST_CONFIGURATION_TASK}$variantName",
        GenerateTestConfigurationTask::class.java
    ) {
        it.testFolder.set(artifacts.get(ArtifactType.APK))
        it.testLoader.set(artifacts.getBuiltArtifactsLoader())
        it.outputXml.fileValue(
            File(
                this.getTestConfigDirectory(),
                "${this.path.asFilenamePrefix()}$variantName.xml"
            )
        )
        it.minSdk.set(minSdk)
        it.hasBenchmarkPlugin.set(this.hasBenchmarkPlugin())
        it.testRunner.set(testRunner)
        it.projectPath.set(this.path)
        AffectedModuleDetector.configureTaskGuard(it)
    }
    // Disable xml generation for projects that have no test sources
    this.afterEvaluate {
        generateTestConfigurationTask.configure {
            it.enabled = this.hasAndroidTestSourceCode()
        }
    }
    this.rootProject.tasks.findByName(AndroidXPlugin.ZIP_TEST_CONFIGS_WITH_APKS_TASK)!!
        .dependsOn(generateTestConfigurationTask)
}

fun Project.addAppApkToTestConfigGeneration() {
    extensions.getByType<ApplicationAndroidComponentsExtension>().apply {
        onVariants(selector().withBuildType("debug")) { debugVariant ->
            tasks.withType(GenerateTestConfigurationTask::class.java).configureEach {
                it.appFolder.set(debugVariant.artifacts.get(ArtifactType.APK))
                it.appLoader.set(debugVariant.artifacts.getBuiltArtifactsLoader())
            }
        }
    }
}

private fun getOrCreateMediaTestConfigTask(project: Project, isMedia2: Boolean):
    TaskProvider<GenerateMediaTestConfigurationTask> {
        val mediaPrefix = getMediaConfigTaskPrefix(isMedia2)
        if (!project.parent!!.tasks.withType(GenerateMediaTestConfigurationTask::class.java)
            .names.contains(
                    "support-$mediaPrefix-test${
                    AndroidXPlugin
                        .GENERATE_TEST_CONFIGURATION_TASK
                    }"
                )
        ) {
            val task = project.parent!!.tasks.register(
                "support-$mediaPrefix-test${AndroidXPlugin.GENERATE_TEST_CONFIGURATION_TASK}",
                GenerateMediaTestConfigurationTask::class.java
            )
            project.rootProject.tasks.findByName(AndroidXPlugin.ZIP_TEST_CONFIGS_WITH_APKS_TASK)!!
                .dependsOn(task)
            return task
        } else {
            return project.parent!!.tasks.withType(GenerateMediaTestConfigurationTask::class.java)
                .named(
                    "support-$mediaPrefix-test${
                    AndroidXPlugin
                        .GENERATE_TEST_CONFIGURATION_TASK
                    }"
                )
        }
    }

private fun getMediaConfigTaskPrefix(isMedia2: Boolean): String {
    return if (isMedia2) "media2" else "media"
}

fun Project.createOrUpdateMediaTestConfigurationGenerationTask(
    variantName: String,
    artifacts: Artifacts,
    minSdk: Int,
    testRunner: String,
    isMedia2: Boolean
) {
    val mediaPrefix = getMediaConfigTaskPrefix(isMedia2)
    val mediaTask = getOrCreateMediaTestConfigTask(this, isMedia2)
    mediaTask.configure {
        it as GenerateMediaTestConfigurationTask
        if (this.name.contains("client")) {
            if (this.name.contains("previous")) {
                it.clientPreviousFolder.set(artifacts.get(ArtifactType.APK))
                it.clientPreviousLoader.set(artifacts.getBuiltArtifactsLoader())
                it.clientPreviousPath.set(this.path)
            } else {
                it.clientToTFolder.set(artifacts.get(ArtifactType.APK))
                it.clientToTLoader.set(artifacts.getBuiltArtifactsLoader())
                it.clientToTPath.set(this.path)
            }
        } else {
            if (this.name.contains("previous")) {
                it.servicePreviousFolder.set(artifacts.get(ArtifactType.APK))
                it.servicePreviousLoader.set(artifacts.getBuiltArtifactsLoader())
                it.servicePreviousPath.set(this.path)
            } else {
                it.serviceToTFolder.set(artifacts.get(ArtifactType.APK))
                it.serviceToTLoader.set(artifacts.getBuiltArtifactsLoader())
                it.serviceToTPath.set(this.path)
            }
        }
        it.clientPreviousServiceToT.fileValue(
            File(
                this.getTestConfigDirectory(),
                "${mediaPrefix}ClientPreviousServiceToT$variantName.xml"
            )
        )
        it.clientToTServicePrevious.fileValue(
            File(
                this.getTestConfigDirectory(),
                "${mediaPrefix}ClientToTServicePrevious$variantName.xml"
            )
        )
        it.clientToTServiceToT.fileValue(
            File(
                this.getTestConfigDirectory(),
                "${mediaPrefix}ClientToTServiceToT$variantName.xml"
            )
        )
        it.minSdk.set(minSdk)
        it.testRunner.set(testRunner)
        AffectedModuleDetector.configureTaskGuard(it)
    }
}

fun Project.configureTestConfigGeneration(testedExtension: TestedExtension) {
    extensions.getByType<AndroidComponentsExtension<*, *>>().apply {
        androidTest(selector().all()) { androidTest ->
            when {
                path.contains("media2:version-compat-tests") -> {
                    createOrUpdateMediaTestConfigurationGenerationTask(
                        androidTest.name,
                        androidTest.artifacts,
                        testedExtension.defaultConfig.minSdk!!,
                        testedExtension.defaultConfig.testInstrumentationRunner!!,
                        isMedia2 = true
                    )
                }
                path.contains("media:version-compat-tests") -> {
                    createOrUpdateMediaTestConfigurationGenerationTask(
                        androidTest.name,
                        androidTest.artifacts,
                        testedExtension.defaultConfig.minSdk!!,
                        testedExtension.defaultConfig.testInstrumentationRunner!!,
                        isMedia2 = false
                    )
                }
                else -> {
                    createTestConfigurationGenerationTask(
                        androidTest.name,
                        androidTest.artifacts,
                        testedExtension.defaultConfig.minSdk!!,
                        testedExtension.defaultConfig.testInstrumentationRunner!!
                    )
                }
            }
        }
    }
}