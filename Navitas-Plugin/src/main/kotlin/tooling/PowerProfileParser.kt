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

        val clusterExpr = xPath.compile("//array[contains(@name, 'cpu.clusters.cores')]")
        val clusterNode = clusterExpr.evaluate(doc, XPathConstants.NODE)
        if(clusterNode != null) {
            val clusterNodeList = xPath.evaluate("value", clusterNode as Node, XPathConstants.NODESET) as NodeList

            for (i in 0 until clusterNodeList.length) {
                val cluster = CpuCoreCluster(clusterNodeList.item(i).textContent.toInt())
                clusters.add(cluster)
            }
        }
        else {
            // TODO: get numCores in another way
                //  If this number is not correct for the device, there will be exceptions
                //  Let it be 4 cores like on Samsung A3 2016 by default
            val cluster = CpuCoreCluster(4)
            clusters.add(cluster)
        }

        for (i in 0 until clusters.size) {
            val speedsExpr = if(clusters.size == 1) {
                xPath.compile("//array[contains(@name, 'cpu.speeds')]")
            } else {
                xPath.compile("//array[contains(@name, 'cpu.speeds.cluster$i')]")
            }
            val speedsNode = speedsExpr.evaluate(doc, XPathConstants.NODE) as Node
            val speedsNodeList = xPath.evaluate("value", speedsNode, XPathConstants.NODESET) as NodeList

            for (j in 0 until speedsNodeList.length) {
                val speed = (speedsNodeList.item(j).textContent.toLong())
                if (speed < MIN_SPEED_VALUE) {
                    continue
                }

                clusters[i].speeds.add(speed)
            }

            val powersExpr = if(clusters.size == 1) {
                xPath.compile("//array[contains(@name, 'cpu.active')]")
            } else {
                xPath.compile("//array[contains(@name, 'cpu.active.cluster$i')]")
            }
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