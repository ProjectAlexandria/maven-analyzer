package net.kikkirej.alexandria.analyzer.maven

import java.lang.RuntimeException
import java.nio.file.Files
import java.nio.file.Path

fun tgfParent(tgfFile: Path) : TGFMavenNode {
    val allLines = Files.readAllLines(tgfFile)
    val iterator = allLines.iterator()
    val nodeMap = mutableMapOf<String, TGFMavenNode>()
    val parentNode = fileNodeMap(iterator, nodeMap)
    addChildrenToMap(nodeMap, iterator)
    return parentNode
}

class TGFMavenNode(val groupId: String,
                   val artifactId: String,
                   val packaging: String,
                   val version: String,
                   val scope: String?,
                   var children: MutableSet<TGFMavenNode> = mutableSetOf()
){
    override fun toString(): String =
        "TGFMavenNode(groupId='$groupId', artifactId='$artifactId', packaging='$packaging', version='$version', scope=$scope)"
}

private fun fileNodeMap(
    iterator: MutableIterator<String>,
    nodeMap: MutableMap<String, TGFMavenNode>,
): TGFMavenNode{
    var parentNode: TGFMavenNode? = null
    while (iterator.hasNext()) {
        val line = iterator.next()
        if (line.equals("#")) {
            break
        }
        val splittedLine = line.split(" ")
        val key = splittedLine[0]
        val value = splittedLine[1]
        val splitedValue = value.split(":")
        var scope: String? = null
        if (splitedValue.size == 5) {
            scope = splitedValue[4]
        }
        val tgfMavenNode = TGFMavenNode(
            groupId = splitedValue[0],
            artifactId = splitedValue[1],
            packaging = splitedValue[2],
            version = splitedValue[3],
            scope = scope
        )
        if (scope == null) {
            parentNode = tgfMavenNode
        }
        nodeMap.put(key, tgfMavenNode)
    }
    if(parentNode == null){
        throw RuntimeException("couldn't find parent element in dependencies.tgf")
    }else {
        return parentNode
    }
}

fun addChildrenToMap(nodeMap: MutableMap<String, TGFMavenNode>, iterator: MutableIterator<String>) {
    while (iterator.hasNext()){
        val line = iterator.next()
        if(line.isEmpty()){
            continue
        }
        val splittedLine = line.split(" ")
        val parent = nodeMap.get(splittedLine[0])
        val child = nodeMap.get(splittedLine[1])
        child?.let { parent?.children?.add(it) }
    }
}

