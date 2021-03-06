package org.dbpedia.lookup.server

import com.sun.jersey.api.container.httpserver.HttpServerFactory
import com.sun.jersey.api.core.ClassNamesResourceConfig
import java.net.URI
import java.io.{FileInputStream, InputStream, InputStreamReader, File}
import java.util.Properties
import org.dbpedia.lookup.lucene.Searcher

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 17.01.11
 * Time: 13:48
 * DBpedia Lookup Server
 * 
 * Usage : mvn scala:run -Dlauncher=Server -DaddArgs=indexing.properties
 */

object Server {
	
    var MAX_HITS_DEFAULT = 5

    private val resources = new ClassNamesResourceConfig(classOf[LookupResource])

    protected[server] val searcher = new Searcher()

    @volatile private var running = true

    def main(args : Array[String]) {
      
    	// reading params from param file
        val in = new FileInputStream(args(0))
        val properties = new Properties()
        try properties.load(new InputStreamReader(in, "UTF-8"))
        finally in.close()
        
        val serverURI = new URI(properties.getProperty("org.dbpedia.lookup.server.uri","http://localhost:1111/"))
        MAX_HITS_DEFAULT = properties.getProperty("org.dbpedia.lookup.server.max_hit_default","5").toInt
        val runExampleOnServer = properties.getProperty("org.dbpedia.lookup.server.runExOnBrowser","false").toBoolean

        val server = HttpServerFactory.create(serverURI, resources)
        server.start()

        System.err.println("Server started in " + System.getProperty("user.dir") + " listening on " + serverURI)

        if(runExampleOnServer){
        //Open browser
        	try {
        		val example = new URI(serverURI.toString+"api/search.asmx/KeywordSearch?QueryString=Berlin")
        		java.awt.Desktop.getDesktop().browse(example)
        	}
        	catch {
        	case e : Exception => System.err.println("Could not open browser. ", e)
        	}
        }

        while(running) {
            Thread.sleep(100)
        }

        //Stop the HTTP server
        server.stop(0)
    }

}
