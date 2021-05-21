package com.arvifox.typesoraconverter

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.net.URLDecoder
import java.util.*

@Serializable
data class OneAsset(val symbol: String, val name: String, val address: String, val decimals: Int, val icon: String)

@Serializable
data class OneResAsset(val symbol: String, val name: String, val address: String, val decimals: Int)

/**
 * arg 0 - xml file to parse
 * arg 1 - jar file to convert svg to xml
 */
fun main(args: Array<String>) {
    println("Hello World!")
    val inputFile = File(args.first())
    val inputText = inputFile.readText()
    val list = Json.decodeFromString<List<OneAsset>>(inputText)
    val resultList = list.map { it.address }
    val svgDirPath = inputFile.parentFile.absolutePath + "/svg/"
    File(svgDirPath).mkdir()
    val resultImageDirPath = inputFile.parentFile.absolutePath + "/res/drawable/"
    val resultJsonDirPath = inputFile.parentFile.absolutePath + "/assets/whitelist.json"
    File(inputFile.parentFile.absolutePath + "/res/").mkdir()
    File(inputFile.parentFile.absolutePath + "/res/drawable/").mkdir()
    File(inputFile.parentFile.absolutePath + "/assets/").mkdir()
    File(resultJsonDirPath).writeText(Json.encodeToString(resultList))
    var svgFilesCount = 0
    list.forEach {
        val icon = it.icon.substringAfter(",", "")
        if (icon.isEmpty()) throw IllegalArgumentException("The ${it.symbol} icon can not be parsed")
        when {
            it.icon.startsWith("data:image/svg") -> {
                val parsed = URLDecoder.decode(icon, "UTF-8")
                val path = svgDirPath + "${it.address}.svg"
                File(path).writeText(parsed)
                svgFilesCount++
            }
            it.icon.startsWith("data:image/png") -> {
                val parsed = Base64.getDecoder().decode(icon)
                val path = resultImageDirPath + "${it.address}.png"
                File(path).writeBytes(parsed)
            }
            else -> {
                throw IllegalArgumentException("The ${it.symbol} icon can not be parsed")
            }
        }
    }
    Runtime.getRuntime().exec("java -jar ${args[1]} $svgDirPath")
    var svgXmlCount = 0
    File(svgDirPath, "ProcessedSVG").walkTopDown().forEach {
        if (!it.isDirectory) {
            it.copyTo(File(resultImageDirPath, it.name), true)
            svgXmlCount++
        }
    }
    if (svgFilesCount != svgXmlCount) throw IllegalArgumentException("SVG count error $svgFilesCount - $svgXmlCount")
    println("Hello World! done")
}
