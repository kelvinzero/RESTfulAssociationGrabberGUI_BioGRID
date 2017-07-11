package datatools.RESTaccess

import java.io.{BufferedReader, IOException, InputStreamReader}
import java.net.URL
import java.util

/**
  * Created by Josh Cotes on 6/17/2017.
  */
class RestQuery() {

  final private val IDKEY = "fc7bbef4153d286c9263325e9898d3bc"
  val _excludeGenes = false
  val _includeInteractors = true
  val _includeInteractions = false

  @throws[IOException]
  def getAssociationsForGeneid(geneid: String): util.ArrayList[Array[String]] = {
    val newList = new util.ArrayList[Array[String]]
    val url = new URL("http://webservice.thebiogrid.org/interactions/?searchNames=true&searchSynonyms=true&includeInteractors=" + _includeInteractors + "&includeInteractorInteractions= " + _includeInteractions + " &excludeGenes=" + _excludeGenes + "&geneList=" + geneid + "&includeHeader=" + _includeInteractions + "&format=tab2Extended&accesskey=" + IDKEY)
    val urlc = url.openConnection

    //use post mode
    urlc.setDoOutput(true)
    urlc.setAllowUserInteraction(false)
    //get result
    val br = new BufferedReader(new InputStreamReader(urlc.getInputStream))
    var l = ""
    while ( {l = br.readLine() ; l != null}) {
      newList.add(l.split("\t"))
      //println(l)
    }
    br.close()
    newList
  }

  @throws[IOException]
  def printTypes(): Unit = {
    val url = new URL("http://webservice.thebiogrid.org/identifiers/?accesskey=fc7bbef4153d286c9263325e9898d3bc")
    //make connection
    val urlc = url.openConnection
    urlc.setDoOutput(true)
    urlc.setAllowUserInteraction(false)
    val br = new BufferedReader(new InputStreamReader(urlc.getInputStream))
    var l = br.readLine()
    while ( {l = br.readLine() ; l != null})
      //System.out.println(l)
    br.close()
  }

  def filterAssociationsList(geneid: String, associations: util.ArrayList[Array[String]]): util.ArrayList[Array[String]] = {

    val newList = new util.ArrayList[Array[String]]
    var nstr = new Array[String](3)
    var i = 0

    for(i <- 0 until associations.size()){

      var as1 = associations.get(i)(5)
      var as2 = associations.get(i)(6)
      var as3 = associations.get(i)(7)
      var as4 = associations.get(i)(8)
      var found = false

      if(as1.equalsIgnoreCase(geneid) && !as2.equalsIgnoreCase(geneid)) {
        for(i <- 0 until newList.size())
          if(newList.get(i)(2).equalsIgnoreCase(as2))
            found = true
        if(!found)
          newList.add(Array(geneid, "pp", as2))
      }

      else if(as2.equalsIgnoreCase(geneid) && !as1.equalsIgnoreCase(geneid)) {
        for(i <- 0 until newList.size())
          if(newList.get(i)(2).equalsIgnoreCase(as1))
            found = true
        if(!found)
          newList.add(Array(geneid, "pp", as1))
      }

      else if(as3.equalsIgnoreCase(geneid) && !as4.equalsIgnoreCase(geneid)) {
        for(i <- 0 until newList.size())
          if(newList.get(i)(2).equalsIgnoreCase(as4))
            found = true
        if(!found)
          newList.add(Array(geneid, "pp", as4))
      }

      else if(as4.equalsIgnoreCase(geneid) && !as3.equalsIgnoreCase(geneid)) {
        for(i <- 0 until newList.size())
          if(newList.get(i)(2).equalsIgnoreCase(as3))
            found = true
        if(!found)
          newList.add(Array(geneid, "pp", as3))
      }
    }
    newList
  }
}