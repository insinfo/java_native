package org.example

import com.github.dkorotych.gradle.maven.exec.MavenExec
import org.gradle.api.Plugin
import org.gradle.api.artifacts.ConfigurablePublishArtifact
import org.gradle.api.initialization.Settings

class ExternalMavenBuilder implements Plugin<Settings> {
    void apply(Settings settings) {
        settings.with {
            rootProject.name = 'jsch' // [1] Deve corresponder ao nome do módulo Maven
            gradle.rootProject {
                group = "org.example" //[2] Deve corresponder ao grupo de módulos Maven
                pluginManager.apply("base") // [3] Define tarefas como "construir" e "limpar"
                pluginManager.apply("com.github.dkorotych.gradle-maven-exec") // [4] O plugin de terceiros que torna mais fácil invocar o Maven

                def mavenBuild = tasks.register("mavenBuild", MavenExec) {
                    goals('clean', 'package') // [5] Para opções, consulte https://github.com/dkorotych/gradle-maven-exec-plugin
                }

                artifacts.add("default", file("$projectDir/target/jsch-0.2.1-SNAPSHOT.jar")) { ConfigurablePublishArtifact a ->
                    a.builtBy(mavenBuild) // [6]  Adiciona a saída do Maven como um artefato na configuração "padrão"
                }
            }
        }
    }
}