package com.orendainx.hortonworks.trucking.commons.models

/**
  * The supertype for all trucking data models.
  * Extending this type ensures that a data model meets the appropriate requirements and has
  * the necessary apply/unapply/serialize/deserialize/etc. methods that components
  * may need to call to transform and act on the data.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
trait TruckingData extends Serializable {

  /**
    * @return A text representation of the data, in CSV format ('|' delimited).
    */
  def toCSV: String
}
