package com.hortonworks.orendainx.trucking.nifi.processors

import org.apache.nifi.components.PropertyDescriptor
import org.apache.nifi.processor.util.StandardValidators

trait GetTruckingDataProperties {
  val ExampleProperty =
    new PropertyDescriptor.Builder()
      .name("Example Property")
      .description("Just an example property for now, move along friend!")
      .required(true)
      .defaultValue("Something")
      .expressionLanguageSupported(true)
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      .build

  lazy val properties = List(ExampleProperty)
}
