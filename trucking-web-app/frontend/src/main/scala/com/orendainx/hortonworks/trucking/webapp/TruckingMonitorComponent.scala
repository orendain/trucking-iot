package com.orendainx.hortonworks.trucking.webapp

import angulate2.std.Component

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
@Component(
  selector = "trucking-monitor",
  templateUrl = "/assets/templates/trucking-monitor.component.html"
)
class TruckingMonitorComponent {
  val title = "Real-Time Truck Monitor - Powered by HDF"
}
