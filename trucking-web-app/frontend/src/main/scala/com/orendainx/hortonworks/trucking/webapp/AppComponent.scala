package com.orendainx.hortonworks.trucking.webapp

import angulate2.std.Component

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
@Component(
  selector = "my-app",
  templateUrl = "/assets/templates/app.component.html"
)
class AppComponent {
  val title = "Real-Time Truck Monitor - Powered by HDF"
}
