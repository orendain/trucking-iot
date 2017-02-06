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
import java.io.InputStream

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
@Tags(Array("trucking", "data", "event", "enrich", "iot"))
@CapabilityDescription("Enriches data for a trucking application. Master project <a href=\"https://github.com/orendain/trucking-iot\">found here</a>")
@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED)
@TriggerSerially
class EnrichTruckingData extends AbstractProcessor {

  // Relationships
  val RelSuccess = new Relationship.Builder()
    .name("success")
    .description("All generated data is routed to this relationship.")
    .build
  lazy val relationships = Set(RelSuccess)
  lazy val properties = List.empty[PropertyDescriptor]

  import scala.collection.JavaConverters._

  private var log: ComponentLog = _

  override def init(context: ProcessorInitializationContext): Unit = {
    log = context.getLogger
  }

  override def onTrigger(context: ProcessContext, session: ProcessSession): Unit = {

    val flowFile = session.get
    val content = new AtomicReference[String]

    session.read(flowFile, new InputStreamCallback {
      override def process(inputStream: InputStream) = {
        val scanner = new Scanner(inputStream).useDelimiter("\\A")
        val result = if (scanner.hasNext()) scanner.next() else ""
        content.set(result)
      }
    })

    log.debug(s"Content: ${content.get()}")

    session.getProvenanceReporter.route(flowFile, RelSuccess)
    session.transfer(flowFile, RelSuccess)
    session.commit()
  }

  override def getSupportedPropertyDescriptors: java.util.List[PropertyDescriptor] = properties.asJava
  override def getRelationships: java.util.Set[Relationship] = relationships.asJava
}
