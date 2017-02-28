package com.orendainx.hortonworks.trucking.nifi.processors

import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicReference

import org.apache.nifi.annotation.behavior._
import org.apache.nifi.annotation.documentation.{CapabilityDescription, Tags}
import org.apache.nifi.annotation.lifecycle.{OnRemoved, OnShutdown}
import org.apache.nifi.components.PropertyDescriptor
import org.apache.nifi.logging.ComponentLog
import org.apache.nifi.processor._
import java.util.Scanner

import org.apache.nifi.processor.io.InputStreamCallback
import org.apache.nifi.processor.io.OutputStreamCallback
import java.io.{InputStream, OutputStream}

import com.orendainx.hortonworks.trucking.common.models.{EnrichedTruckData, TruckData}
import com.orendainx.hortonworks.trucking.enrichment.WeatherAPI

import scala.language.implicitConversions
import scala.collection.JavaConverters._

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
@Tags(Array("trucking", "data", "event", "enrich", "iot"))
@CapabilityDescription("Enriches data for a trucking application. Master project <a href=\"https://github.com/orendain/trucking-iot\">found here</a>")
@WritesAttributes(Array(
  new WritesAttribute(attribute = "dataType", description = "The class name of the resulting enriched data type.")
))
@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED)
@TriggerSerially
class EnrichTruckData extends AbstractProcessor {

  // Relationships
  val RelSuccess = new Relationship.Builder()
    .name("success")
    .description("All generated data is routed to this relationship.")
    .build
  lazy val relationships = Set(RelSuccess)
  lazy val properties = List.empty[PropertyDescriptor]

  private var log: ComponentLog = _

  override def init(context: ProcessorInitializationContext): Unit = {
    log = context.getLogger
  }

  override def onTrigger(context: ProcessContext, session: ProcessSession): Unit = {

    var flowFile = session.get

    log.debug(s"Flowfile1: ${flowFile}")

    val content = new AtomicReference[String]

    session.read(flowFile, new InputStreamCallback {
      override def process(inputStream: InputStream) = {
        val scanner = new Scanner(inputStream).useDelimiter("\\A")
        val result = if (scanner.hasNext()) scanner.next() else ""
        log.debug(s"Flowfile2: ${result}")
        content.set(result)
      }
    })

    implicit def bool2Int(bool: Boolean): Int = if (bool) 1 else 0
    val truckData = TruckData.fromCSV(content.get()
    val enrichedTruckData = EnrichedTruckData(truckData, WeatherAPI.isFoggy(truckData.eventType),
      WeatherAPI.isRainy(truckData.eventType), WeatherAPI.isWindy(truckData.eventType))

    log.debug(s"Content: ${content.get()}")
    log.debug(s"EnrichedData: ${enrichedTruckData}")

    // Update dataType
    flowFile = session.putAttribute(flowFile, "dataType", enrichedTruckData.getClass.getSimpleName)

    //var flowFile = session.create()
    flowFile = session.write(flowFile, new OutputStreamCallback {
      override def process(outputStream: OutputStream) = {
        outputStream.write(enrichedTruckData.toCSV.getBytes(StandardCharsets.UTF_8))
      }
    })

    session.getProvenanceReporter.route(flowFile, RelSuccess)
    session.transfer(flowFile, RelSuccess)
    session.commit()
  }

  override def getSupportedPropertyDescriptors: java.util.List[PropertyDescriptor] = properties.asJava
  override def getRelationships: java.util.Set[Relationship] = relationships.asJava
}
