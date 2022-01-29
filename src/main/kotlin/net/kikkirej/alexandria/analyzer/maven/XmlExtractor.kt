package net.kikkirej.alexandria.analyzer.maven

import net.kikkirej.alexandria.analyzer.maven.db.*
import org.dom4j.Document
import org.dom4j.DocumentFactory
import org.dom4j.io.SAXReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.File
import java.nio.file.Path

@Component
class XmlExtractor(@Autowired val analysisRepository: AnalysisRepository,
                   @Autowired val mavenDistributionManagementRepository: MavenDistributionManagementRepository,
                   @Autowired val mavenModuleRepository: MavenModuleRepository,
) {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    fun getModuleFromXML(modulePath: File, businessKey: String?) : MavenModule{
        val pom: Document = getDocumentForPomIn(modulePath)
        val analysis = analysisRepository.findById(businessKey!!.toLong()).get()
        val parentArtifactId = pom.findByXPath("/p:project/p:parent/p:artifactId", null)
        val parentGroupId = pom.findByXPath("/p:project/p:parent/p:groupId", null)
        val parentVersion = pom.findByXPath("/p:project/p:parent/p:version", null)
        val artifactId = pom.findByXPathNonNull("/p:project/p:artifactId", null)
        val groupId = pom.findByXPathNonNull("/p:project/p:groupId", parentGroupId)
        val version = pom.findByXPathNonNull("/p:project/p:version", parentVersion)
        val packaging = pom.findByXPath("/p:project/p:packaging", null)
        val repositoryId = pom.findByXPathNonNull("/p:project/p:distributionManagement/p:repository/p:id", null)
        val repositoryName = pom.findByXPathNonNull("/p:project/p:distributionManagement/p:repository/p:name", null)
        val repositoryUrl = pom.findByXPathNonNull("/p:project/p:distributionManagement/p:repository/p:url", null)
        val snapshotRepositoryId = pom.findByXPathNonNull("/p:project/p:distributionManagement/p:snapshotRepository/p:id", null)
        val snapshotRepositoryName = pom.findByXPathNonNull("/p:project/p:distributionManagement/p:snapshotRepository/p:name", null)
        val snapshotRepositoryUrl = pom.findByXPathNonNull("/p:project/p:distributionManagement/p:snapshotRepository/p:url", null)
        val repository = getRepository(repositoryId, repositoryName, repositoryUrl)
        val snapshotRepository = getRepository(snapshotRepositoryId, snapshotRepositoryName, snapshotRepositoryUrl)
        val result = MavenModule(analysis = analysis,
            artifactId = artifactId,
            groupId = groupId,
            version = version,
            packaging = packaging,
            parentArtifactId = parentArtifactId,
            parentGroupId = parentGroupId,
            parentVersion = parentVersion,
            repository = repository,
            snapshotRepository = snapshotRepository,
        )
        mavenModuleRepository.save(result)
        return result
    }

    private fun getDocumentForPomIn(modulePath: File): Document {
        val pomFilePathString = modulePath.absolutePath + File.separator + "pom.xml"
        log.info("pom.xml path is: $pomFilePathString")
        val pomFileUrl = Path.of(pomFilePathString).toUri().toURL()
        val map = mapOf("p" to "http://maven.apache.org/POM/4.0.0")
        DocumentFactory.getInstance().xPathNamespaceURIs=map
        return SAXReader().read(pomFileUrl)
    }

    private fun getRepository(
        id: String?,
        name: String?,
        url: String?
    ): MavenDistributionManagement? {
        if(id.isNullOrBlank() && name.isNullOrBlank() && url.isNullOrBlank()){
            return null
        }
        val dbResult = mavenDistributionManagementRepository.findByRepoIdAndNameAndUrl(id, name, url)
        if(dbResult.isPresent){
            return dbResult.get()
        }else{
            val mavenDistributionManagement = MavenDistributionManagement(repoId = id, name = name, url = url)
            mavenDistributionManagementRepository.save(mavenDistributionManagement)
            return mavenDistributionManagement
        }
    }
}

private fun Document.findByXPathNonNull(xpath: String, defaultValue: String?): String {
    return findByXPath(xpath, defaultValue) ?: return ""
}

private fun Document.findByXPath(xpath: String, defaultValue: String?): String? {
    val node = selectSingleNode(xpath) ?: return defaultValue
    val stringValue = node.stringValue
    if(stringValue.isNullOrBlank()){
        return defaultValue
    }else{
        return stringValue
    }
}
