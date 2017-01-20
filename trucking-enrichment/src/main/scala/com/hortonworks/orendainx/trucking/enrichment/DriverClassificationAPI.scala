package com.hortonworks.orendainx.trucking.enrichment

import better.files.File
import com.github.tototoshi.csv.CSVReader

import scala.io.Source

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object DriverClassificationAPI {

  lazy val apply = new DriverClassificationAPI(Source.fromInputStream(getClass.getResourceAsStream("classifications-default.conf")))

  def apply(filename: String) = new DriverTimesheetAPI(File(filename).newBufferedSource)
}

class DriverClassificationAPI(datasource: Source) {

  private val reader = CSVReader.open(datasource)
  private val values = reader.all()
  reader.close()

  /** Queries driver classifications for driver certification status.
    *
    * @param driverId The id of the driver to query for
    * @return true if the driver is certified, false otherwise
    */
  def isCertified(driverId: Int): Boolean =
    values.collectFirst { case lst: List[String] if lst.head.toInt == driverId => lst(1) }.exists(_.equalsIgnoreCase("Y"))

  /** Queries driver classifications for a wage plan.
    *
    * @param driverId The id of the driver to query for
    * @return the driver's wage plan as a string (i.e. "miles" or "hours")
    */
  def wagePlan(driverId: Int): String =
    values.collectFirst { case lst: List[String] if lst.head.toInt == driverId => lst(2) }.get
}
