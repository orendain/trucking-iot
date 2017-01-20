package com.hortonworks.orendainx.trucking.simulator.services

import java.net.URI
import java.nio.file.{FileSystemNotFoundException, FileSystems}
import java.util

import better.files.{File, Scannable}
import com.hortonworks.orendainx.trucking.simulator.models.{Location, Route}
import com.typesafe.scalalogging.Logger

import scala.collection.mutable.ListBuffer

/**
  * A parser for a directory storing Route files (.route extension).
  * When parsing the base directory, RouteReader traverses recursively into directories in search of every route file.
  *
  * After a series of headaches (hehe), this class can now process routes stored in traditional filesystems or
  * jar-packaged filesystems (when this library is leveraged either as a JAR or as a compiled part of a larger system).
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object RouteParser {

  // TODO: if a routefile is incorrectly formatted, entire system goes down.  change this.
  val log = Logger(getClass)

  def apply(routeDirectory: String): RouteParser = {
    //val path = s"${getClass.getResource("/routes").getPath}/$routeDirectory"

    //val path = s"${getClass.getResource(".").getPath}"
    //log.debug(s"path: $path")
    //new RouteParser(path)

    // TODO: Cleanup!
    val path = s"${getClass.getResource("/routes").getPath}/$routeDirectory"
    log.debug(s"1 $path")
    if (path.startsWith("file")) { // is a jar file
      val p = path.takeWhile(_ != '!')
      log.debug(s"2 $p")
      val jarPath = URI.create(s"jar:$p")
      log.debug(s"3 $jarPath")
      //val env = Map[String, String]("create" -> "true")
      val env = new util.HashMap[String, String]()
      //env.put("create", "true")


      val fs = try {
        FileSystems.getFileSystem(jarPath)
      } catch {
        case ex: FileSystemNotFoundException =>
          FileSystems.newFileSystem(jarPath, env)
      }

        //val fs = FileSystems.newFileSystem(jarPath, env)

        val fsp = fs.getPath("/routes/midwest")
        log.debug(s"4 ${fsp.toString}")

        //val file = fsp.toFile.toScala
        val file = File(fsp)
        new RouteParser(file)
    } else {
      log.debug(s"path: $path")
      new RouteParser(File(path))
    }

  }

  def parseFile(file: File): Route = {
    val scanner = file.newScanner
    val routeId = scanner.next[Int]
    val routeName = scanner.tillEndOfLine()
    val locations = ListBuffer[Location]()
    while (scanner.hasNext)
      locations += scanner.next[Location]

    Route(routeId, routeName, locations.toList)
  }

  // Define Scanner parser for Location
  private implicit val locationParser: Scannable[Location] = Scannable { scanner =>
    Location(scanner.next[Double], scanner.next[Double])
  }
}

class RouteParser(directory: File) {

  lazy val routes: List[Route] = {

    if (directory.isDirectory)
      directory.listRecursively
        .filter(_.extension.contains(".route"))
        .map(RouteParser.parseFile).toList
    else
      List.empty[Route]
  }
}
