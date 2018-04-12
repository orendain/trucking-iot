package com.orendainx.trucking.nifi.processors

import java.io.{InputStream, OutputStream}
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicReference
import java.util.Scanner

import com.orendainx.trucking.commons.models.{EnrichedTruckData, TruckData}
import com.orendainx.trucking.enrichment.WeatherAPI
import org.apache.nifi.annotation.behavior._
import org.apache.nifi.annotation.documentation.{CapabilityDescription, Tags}
import org.apache.nifi.components.PropertyDescriptor
import org.apache.nifi.logging.ComponentLog
import org.apache.nifi.processor.io.InputStreamCallback
import org.apache.nifi.processor.io.OutputStreamCallback
import org.apache.nifi.processor._

import scala.collection.JavaConverters._

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
@Tags(Array("trucking", "data", "event", "enrich", "iot"))
@CapabilityDescription("Enriches simulated truck sensor data. Find the master project and its code, documentation and corresponding tutorials at: https://github.com/orendain/trucking-iot")
@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED)
@TriggerSerially
@WritesAttributes(Array(
  new WritesAttribute(attribute = "dataType", description = "The class name of the resulting enriched data type.")
))
class EnrichTruckData extends AbstractProcessor {

  private var log: ComponentLog = _
  private val RelSuccess = new Relationship.Builder().name("success").description("All generated data is routed to this relationship.").build

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
    val enrichedTruckData = EnrichedTruckData(truckData, WeatherAPI.default.getFog(truckData.eventType),
      WeatherAPI.default.getRain(truckData.eventType), WeatherAPI.default.getWind(truckData.eventType))

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

  // Define properties and relationships
  override def getSupportedPropertyDescriptors: java.util.List[PropertyDescriptor] = List.empty[PropertyDescriptor].asJava

  override def getRelationships: java.util.Set[Relationship] = Set(RelSuccess).asJava
}
