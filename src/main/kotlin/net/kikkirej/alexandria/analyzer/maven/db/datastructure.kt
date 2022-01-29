package net.kikkirej.alexandria.analyzer.maven.db

import javax.persistence.*

@Entity(name = "analysis")
class Analysis(@Id var id: Long)

@Entity(name="maven_module")
class MavenModule(
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) var id: Long = 0,
    var artifactId: String,
    var groupId: String,
    var version: String,
    var packaging: String?,
    var parentArtifactId: String?,
    var parentGroupId: String?,
    var parentVersion: String?,
    @ManyToOne var analysis: Analysis,
    @ManyToOne var repository: MavenDistributionManagement?,
    @ManyToOne var snapshotRepository: MavenDistributionManagement?,
){
    override fun toString(): String {
        return "MavenModule(id=$id, artifactId='$artifactId', groupId='$groupId', version='$version', packaging=$packaging, parentArtifactId=$parentArtifactId, parentGroupId=$parentGroupId, parentVersion=$parentVersion, analysis=$analysis, repository=$repository, snapshotRepository=$snapshotRepository)"
    }
}
@Entity(name = "maven_distribution_management")
class MavenDistributionManagement(@Id @GeneratedValue var id: Long = 0,
                                  var repoId: String?,
                                  var name: String?,
                                  var url: String?,
)

@Entity(name = "maven_module_dependency")
class MavenModuleDependency(@Id @GeneratedValue var id: Long = 0,
                            var scope: String?,
                            var version: String,
                            var packaging: String,
                            var depth: Long,
                            @ManyToOne var dependency: MavenDependency,
                            @ManyToOne var module: MavenModule,
                            @ManyToOne var parent: MavenModuleDependency?,
)

@Entity(name = "maven_dependency")
class MavenDependency(@Id @GeneratedValue var id: Long = 0,
                      var groupId: String,
                      var artifactId: String,
)