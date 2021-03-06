package net.kikkirej.alexandria.analyzer.maven

import net.kikkirej.alexandria.analyzer.maven.db.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.File
import kotlin.io.path.Path

@Component
class DependencyTreeAnalyzer (@Autowired val mavenDependencyRepository: MavenDependencyRepository,
                              @Autowired val mavenModuleDependencyRepository: MavenModuleDependencyRepository,
) {
    val log = LoggerFactory.getLogger(javaClass)

    fun analyze(modulePath: File, module: MavenModule) {
        runDependencyTreeCommandIn(modulePath, module.artifactId)
        val tgfPath = Path("${modulePath.absolutePath + File.separator}dependencies.tgf")
        log.info("Path for generated tgf-File: ${tgfPath}")
        val tgfParent = tgfParent(tgfPath)
        handleNode(tgfParent, null ,0, module)

    }

    private fun handleNode(node: TGFMavenNode, parent: MavenModuleDependency?, depth: Long, module: MavenModule) {
        val mavenModuleDependency: MavenModuleDependency = getMavenModuleDependency(node, parent, depth, module)
        for (child in node.children){
            handleNode(child, mavenModuleDependency, depth+1, module)
        }
    }

    private fun getMavenModuleDependency(
        node: TGFMavenNode,
        parent: MavenModuleDependency?,
        depth: Long,
        module: MavenModule
    ): MavenModuleDependency {
        val dependency: MavenDependency = getDependency(node.groupId, node.artifactId)
        val mavenModuleDependency = MavenModuleDependency(
            depth = depth,
            scope = node.scope,
            version = node.version,
            packaging = node.packaging,
            dependency = dependency,
            parent = parent,
            module = module,
        )
        mavenModuleDependencyRepository.save(mavenModuleDependency)
        return mavenModuleDependency
    }

    private fun getDependency(groupId: String, artifactId: String): MavenDependency {
        val dbResult = mavenDependencyRepository.findByGroupIdAndArtifactId(groupId, artifactId)
        if(dbResult.isPresent){
            return dbResult.get()
        }else{
            log.info("Creating maven_dependency for $groupId:$artifactId")
            val mavenDependency = MavenDependency(groupId = groupId, artifactId = artifactId)
            mavenDependencyRepository.save(mavenDependency)
            return mavenDependency
        }
    }

    private fun runDependencyTreeCommandIn(modulePath: File, artifactId: String) {
        val process = ProcessBuilder(
            "mvn",
            "-pl",
            ":$artifactId",
            "dependency:tree",
            "-DoutputEncoding=utf-8",
            "-DoutputFile=${modulePath.absolutePath + File.separator}dependencies.tgf",
            "-DoutputType=tgf",
        ).directory(modulePath).redirectErrorStream(true).redirectOutput(File("/mvn.log")).start()
        log.info("Started Process ${process.info()} in ${modulePath.absoluteFile}.")
        process.waitFor()
        log.info("Finished Process in ${modulePath.absoluteFile}.")
    }
}