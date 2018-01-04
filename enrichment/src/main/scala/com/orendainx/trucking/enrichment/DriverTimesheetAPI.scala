package com.orendainx.trucking.enrichment

import java.io.{InputStreamReader, Reader}
import java.util.{Calendar, Date}

import better.files.File
import com.github.tototoshi.csv.CSVReader

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object DriverTimesheetAPI {

  lazy val apply = new DriverTimesheetAPI(new InputStreamReader(getClass.getResourceAsStream("timesheet-default.conf")))

  def apply(filename: String) = new DriverTimesheetAPI(File(filename).newBufferedReader)
}

class DriverTimesheetAPI(datasource: Reader) {

  private val reader = CSVReader.open(datasource)
  private val values = reader.all()
  reader.close()

  /** Queries the driver timesheet for hours logged.
    *
    * @param driverId The id of the driver to query for
    * @return the number of hours the given driver has logged
    */
  def hoursLogged(driverId: Int, eventTime: Long): Int = {
    val cal = Calendar.getInstance()
    cal.setTime(new Date(eventTime))
    val weekNumber = cal.get(Calendar.WEEK_OF_YEAR)

    values.filter(_.head.toInt == driverId).collectFirst{ case lst: List[_] if lst(1).toInt == weekNumber => lst(3).toInt }.get
  }

  /** Queries the driver timesheet for miles logged.
    *
    * @param driverId The id of the driver to query for
    * @return the number of miles the given driver has logged
    */
  def milesLogged(driverId: Int, eventTime: Long): Int = {
    val cal = Calendar.getInstance()
    cal.setTime(new Date(eventTime))
    val weekNumber = cal.get(Calendar.WEEK_OF_YEAR)
    values.filter(_.head.toInt == driverId).collectFirst{ case lst: List[_] if lst(1).toInt == weekNumber => lst(4).toInt }.get
  }
}
