package tooling

import domain.model.CpuCoreCluster
import domain.model.PowerProfile
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

class PowerProfileParser(private val file: File) {

    companion object {
        private const val MIN_SPEED_VALUE = 10000
    }

    fun parseXML(): PowerProfile {
        val path = file.canonicalPath
        val clusters = ArrayList<CpuCoreCluster>()

        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        val xPath = XPathFactory.newInstance().newXPath()

        val clusterExpr = xPath.compile("//array[contains(@name, 'clusters.cores')]")
        val clusterNode = clusterExpr.evaluate(doc, XPathConstants.NODE) as Node
        val clusterNodeList = xPath.evaluate("value", clusterNode, XPathConstants.NODESET) as NodeList

        for (i in 0 until clusterNodeList.length) {
            val cluster = CpuCoreCluster(clusterNodeList.item(i).textContent.toInt())
            clusters.add(cluster)
        }

        for (i in 0 until clusters.size) {
            val speedsExpr = xPath.compile("//array[contains(@name, 'speeds.cluster$i')]")
            val speedsNode = speedsExpr.evaluate(doc, XPathConstants.NODE) as Node
            val speedsNodeList = xPath.evaluate("value", speedsNode, XPathConstants.NODESET) as NodeList

            for (j in 0 until speedsNodeList.length) {
                val speed = (speedsNodeList.item(j).textContent.toLong())
                if (speed < MIN_SPEED_VALUE) {
                    continue
                }

                clusters[i].speeds.add(speed)
            }

            val powersExpr = xPath.compile("//array[contains(@name, 'active.cluster$i')]")
            val powersNode = powersExpr.evaluate(doc, XPathConstants.NODE) as Node
            val powersNodeList = xPath.evaluate("value", powersNode, XPathConstants.NODESET) as NodeList

            for (j in 0 until powersNodeList.length) {
                val power = (powersNodeList.item(j).textContent.toFloat())
                clusters[i].powers.add(power)
            }
        }

        return PowerProfile(path, clusters)
    }
}