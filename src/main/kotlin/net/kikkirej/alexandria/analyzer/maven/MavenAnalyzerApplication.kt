package net.kikkirej.alexandria.analyzer.maven

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MavenAnalyzerApplication

fun main(args: Array<String>) {
	runApplication<MavenAnalyzerApplication>(*args)
}
