package com.orendainx.hortonworks.trucking.nifi.processors

import java.io.{InputStream, OutputStream}
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicReference
import java.util.Scanner

import com.orendainx.hortonworks.trucking.commons.models.{EnrichedTruckData, TruckData}
import com.orendainx.hortonworks.trucking.enrichment.WeatherAPI
import org.apache.nifi.annotation.behavior._
import org.apache.nifi.annotation.documentation.{CapabilityDescription, Tags}
import org.apache.nifi.annotation.lifecycle.{OnRemoved, OnShutdown}
import org.apache.nifi.components.PropertyDescriptor
import org.apache.nifi.logging.ComponentLog
import org.apache.nifi.processor.io.InputStreamCallback
import org.apache.nifi.processor.io.OutputStreamCallback
import org.apache.nifi.processor._

import scala.collection.JavaConverters._
import scala.language.implicitConversions

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
@Tags(Array("trucking", "data", "event", "enrich", "iot"))
@CapabilityDescription("Enriches data for a trucking application. Master project <a href=\"https://github.com/orendain/trucking-iot\">found here</a>")
@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED)
@TriggerSerially
@WritesAttributes(Array(
  new WritesAttribute(attribute = "dataType", description = "The class name of the resulting enriched data type.")
))
class EnrichTruckData extends AbstractProcessor {

  private var log: ComponentLog = _

  override def init(context: ProcessorInitializationContext): Unit = {
    log = context.getLogger
  }

  override def onTrigger(context: ProcessContext, session: ProcessSession): Unit = {

    var flowFile = session.get
    log.debug(s"Flowfile received: $flowFile")

    // Convert the entire stream of bytes from the flow file into a string
    val content = new AtomicReference[String]
    session.read(flowFile, new InputStreamCallback {
      override def process(inputStream: InputStream) = {
        val scanner = new Scanner(inputStream).useDelimiter("\\A")
        val result = if (scanner.hasNext()) scanner.next() else ""
        log.debug(s"Parsed content: $result")
        content.set(result)
      }
    })

    // Form a TruckData object from content, then creating an EnrichedTruckData object by making the appropriate
    // calls to WeatherAPI
    val truckData = TruckData.fromCSV(content.get())
    val enrichedTruckData = EnrichedTruckData(truckData, WeatherAPI.isFoggy(truckData.eventType),
      WeatherAPI.isRainy(truckData.eventType), WeatherAPI.isWindy(truckData.eventType))

    log.debug(s"EnrichedData generated: $enrichedTruckData")

    // Add the new data type as a flow file attribute
    flowFile = session.putAttribute(flowFile, "dataType", enrichedTruckData.getClass.getSimpleName)

    // Replace the flow file, writing in the new content
    flowFile = session.write(flowFile, new OutputStreamCallback {
      override def process(outputStream: OutputStream) =
        outputStream.write(enrichedTruckData.toCSV.getBytes(StandardCharsets.UTF_8))
    })

    // TODO: document what this does
    session.getProvenanceReporter.route(flowFile, RelSuccess)
    session.transfer(flowFile, RelSuccess)
    session.commit()
  }

  // For use with results from the WeatherAPI - convert result from the call to an integer value
  private implicit def bool2Int(bool: Boolean): Int = if (bool) 1 else 0

  // Define properties and relationships
  override def getSupportedPropertyDescriptors: java.util.List[PropertyDescriptor] = List.empty[PropertyDescriptor].asJava

  override def getRelationships: java.util.Set[Relationship] = Set(
    new Relationship.Builder().name("success").description("All generated data is routed to this relationship.").build
  ).asJava
}
