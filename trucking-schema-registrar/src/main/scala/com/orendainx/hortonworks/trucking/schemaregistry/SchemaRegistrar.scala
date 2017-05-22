package com.orendainx.hortonworks.trucking.schemaregistry

import java.util.Scanner

import com.hortonworks.registries.schemaregistry.avro.AvroSchemaProvider
import com.hortonworks.registries.schemaregistry.client.SchemaRegistryClient
import com.hortonworks.registries.schemaregistry.{SchemaCompatibility, SchemaMetadata, SchemaVersion, SerDesInfo}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger

/**
  * Example of how to leverage the Schema Registry with Scala.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object SchemaRegistrar {

  import collection.JavaConversions._

  // Create logger
  private val log = Logger(this.getClass)

  // Load default configuration file (application.conf in the resources folder)
  private val baseConfig = ConfigFactory.load()

  // Create a config object with the configuration properties to instantiate a SchemaRegistryClient
  private val clientConfig = Map[String, AnyRef](SchemaRegistryClient.Configuration.SCHEMA_REGISTRY_URL.name() -> baseConfig.getString("schema-registry.url"))
  private val schemaRegistryClient = new SchemaRegistryClient(clientConfig)

  /**
    * Entry point for an application.  Immediately runs register().
    */
  def main(args: Array[String]): Unit = {
    register()
  }

  /**
    * Register all built-in schemas with the Schema Registry service.  These include the following schemas:
    * - TruckData
    * - TrafficData
    * - EnrichedTruckAndTrafficData
    */
  def register(): Unit = {
    setupSchema("schema.truck-data")
    setupSchema("schema.enriched-truck-data")
    setupSchema("schema.enriched-truck-data-kafka")
    setupSchema("schema.traffic-data")
    setupSchema("schema.traffic-data-kafka")
    setupSchema("schema.enriched-truck-and-traffic-data")
    setupSchema("schema.enriched-truck-and-traffic-data-kafka")
    setupSchema("schema.windowed-driver-stats")
    setupSchema("schema.windowed-driver-stats-kafka")
  }


  /**
    * The steps involved:
    * - Build and register schema metadata
    * - Retrieve and submit schema content, creating a version of the schema and linking it the metadata
    * - Upload compiled SerDes classes and register the SerDes with the service
    * - Map the SerDes to the previously created schema
    *
    * @param schemaConfigPath Config path that holds schema registry configuration values.
    */
  def setupSchema(schemaConfigPath: String): Unit = {

    val config = baseConfig.getConfig(schemaConfigPath)

    /*
     * Retrieve configuration properties for the general schema information,
     * using it to create and then registering the schema metadata with the registry client.
     */
    val schemaName = config.getString("name")
    val schemaGroupName = config.getString("group-name")
    val schemaDescription = config.getString("description")
    val schemaTypeCompatibility = SchemaCompatibility.BACKWARD
    val schemaType = AvroSchemaProvider.TYPE

    val schemaMetadata = new SchemaMetadata.Builder(schemaName).`type`(schemaType).schemaGroup(schemaGroupName)
      .description(schemaDescription).compatibility(schemaTypeCompatibility).build()

    val metadataRegistrationResult = schemaRegistryClient.registerSchemaMetadata(schemaMetadata)

    log.info(s"Schema registration result: $metadataRegistrationResult")




    /*
     * Read the file with the Avro schema text, creating a SchemaVersion out of it
     * before adding that version to the service via the registry client.
     */
    val scanner = new Scanner(getClass.getResourceAsStream(config.getString("avro.filepath"))).useDelimiter("\\A")
    val avroSchemaContent = if (scanner.hasNext) scanner.next() else ""
    val schemaVersionId = schemaRegistryClient.addSchemaVersion(schemaName, new SchemaVersion(avroSchemaContent, "Initial schema"))
    //val schemaVersionId = schemaRegistryClient.addSchemaVersion(schemaMetadata, schemaVersion)

    log.info(s"Schema content: $avroSchemaContent")
    log.info(s"Schema version id: $schemaVersionId")




    /*
     * Retrieve configuration properties for the Avro version of the schema,
     * then upload the jar file with the compiled class files of the de/serializer.
     * Follow up with creating both de/serializers, and finally add them with the registry client.
     */
    val avroSchemaName = config.getString("avro.name")
    val avroSchemaDescription = config.getString("avro.description")
//    val avroSerializerClassName = config.getString("avro.serializer-class-name")
//    val avroDeserializerClassName = config.getString("avro.deserializer-class-name")
val avroSerializerClassName = "com.hortonworks.schemaregistry.samples.serdes.SimpleSerializer"
val avroDeserializerClassName = "com.hortonworks.schemaregistry.samples.serdes.SimpleDeserializer"

    //val avroJar = getClass.getResourceAsStream(config.getString("avro.jarpath"))
    val avroJar = getClass.getResourceAsStream("/schema/serdes-examples.jar")
    val avroSerDesFileId = schemaRegistryClient.uploadFile(avroJar)
    avroJar.close()

    log.info(s"Upload fileId: $avroSerDesFileId")

    val avroSerializerInfo = new SerDesInfo.Builder().name(avroSchemaName).description(avroSchemaDescription)
      .fileId(avroSerDesFileId).className(avroSerializerClassName).buildSerializerInfo()

    val avroDeserializerInfo = new SerDesInfo.Builder().name(avroSchemaName).description(avroSchemaDescription)
      .fileId(avroSerDesFileId).className(avroDeserializerClassName).buildDeserializerInfo()

    val avroSerializerId = schemaRegistryClient.addSerializer(avroSerializerInfo)
    val avroDeserializerId = schemaRegistryClient.addDeserializer(avroDeserializerInfo)

    log.info(s"Avro serializer id: $avroSerializerId")
    log.info(s"Avro deserializer id: $avroDeserializerId")




    /*
     * Map the serializer/deserializer to our schema.
     */
    schemaRegistryClient.mapSchemaWithSerDes(schemaName, avroSerializerId)
    schemaRegistryClient.mapSchemaWithSerDes(schemaName, avroDeserializerId)
  }
}
