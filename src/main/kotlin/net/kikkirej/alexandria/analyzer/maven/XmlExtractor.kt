package net.kikkirej.alexandria.analyzer.maven

import net.kikkirej.alexandria.analyzer.maven.db.*
import org.dom4j.Document
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
        val parentArtifactId = pom.findByXPath("/project/parent/artifactId", null)
        val parentGroupId = pom.findByXPath("/project/parent/groupId", null)
        val parentVersion = pom.findByXPath("/project/parent/version", null)
        val artifactId = pom.findByXPathNonNull("/project/artifactId", null)
        val groupId = pom.findByXPathNonNull("/project/groupId", parentGroupId)
        val version = pom.findByXPathNonNull("/project/version", parentVersion)
        val packaging = pom.findByXPath("/project/packaging", null)
        val repositoryId = pom.findByXPathNonNull("/project/distributionManagement/repository/id", null)
        val repositoryName = pom.findByXPathNonNull("/project/distributionManagement/repository/name", null)
        val repositoryUrl = pom.findByXPathNonNull("/project/distributionManagement/repository/url", null)
        val snapshotRepositoryId = pom.findByXPathNonNull("/project/distributionManagement/snapshotRepository/id", null)
        val snapshotRepositoryName = pom.findByXPathNonNull("/project/distributionManagement/snapshotRepository/name", null)
        val snapshotRepositoryUrl = pom.findByXPathNonNull("/project/distributionManagement/snapshotRepository/url", null)
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
