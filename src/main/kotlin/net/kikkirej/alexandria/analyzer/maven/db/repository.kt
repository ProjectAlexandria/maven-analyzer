package net.kikkirej.alexandria.analyzer.maven.db

import org.springframework.data.repository.CrudRepository
import java.util.*

interface AnalysisRepository: CrudRepository<Analysis, Long>

interface MavenModuleRepository: CrudRepository<MavenModule, Long>

interface MavenDistributionManagementRepository: CrudRepository<MavenDistributionManagement, Long>{
    fun findByRepoIdAndNameAndUrl(repoId: String?, name: String?, id: String?): Optional<MavenDistributionManagement>
}

interface MavenModuleDependencyRepository: CrudRepository<MavenModuleDependency, Long>

interface MavenDependencyRepository: CrudRepository<MavenDependency, Long>{
    fun findByGroupIdAndArtifactId(groupId: String, artifactId: String): Optional<MavenDependency>
}