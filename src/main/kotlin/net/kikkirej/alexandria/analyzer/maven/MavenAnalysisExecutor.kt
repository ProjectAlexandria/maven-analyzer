package net.kikkirej.alexandria.analyzer.maven

import net.kikkirej.alexandria.analyzer.maven.config.GeneralProperties
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription
import org.camunda.bpm.client.task.ExternalTask
import org.camunda.bpm.client.task.ExternalTaskHandler
import org.camunda.bpm.client.task.ExternalTaskService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.File

@Component
@ExternalTaskSubscription("maven-analysis")
class MavenAnalysisExecutor(@Autowired val generalProperties: GeneralProperties,
                            @Autowired val xmlExtractor: XmlExtractor,
                            @Autowired val dependencyTreeAnalyzer: DependencyTreeAnalyzer,
) : ExternalTaskHandler {
    override fun execute(externalTask: ExternalTask?, externalTaskService: ExternalTaskService?) {
        try {
            val modulePath = modulePathOf(externalTask!!)
            val module = xmlExtractor.getModuleFromXML(modulePath, externalTask.businessKey)
            dependencyTreeAnalyzer.analyze(modulePath, module)
            externalTaskService!!.complete(externalTask)
        }catch (exception: Exception){
            externalTaskService!!.handleBpmnError(externalTask, "unspecified", exception.message)
        }


    }

    private fun modulePathOf(externalTask: ExternalTask): File {
        val mavenModulePath = externalTask.getVariable<String>("maven_module_path");
        val businessKey = externalTask.businessKey
        val modulePath =
            File(File(generalProperties.sharedfolder).absolutePath + File.separator + businessKey + mavenModulePath)
        return modulePath
    }
}