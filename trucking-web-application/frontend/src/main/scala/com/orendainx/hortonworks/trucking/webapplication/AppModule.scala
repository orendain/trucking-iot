package com.orendainx.hortonworks.trucking.webapplication

import angulate2.platformBrowser.BrowserModule
import angulate2.router.RouterModule
import angulate2.std._

import scala.scalajs.js

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
@NgModule(
  imports = @@[BrowserModule] :+
  RouterModule.forRoot(js.Array(
    Route(path = "", component = %%[TruckingMonitorComponent]),
    Route(path = "dashboard", component = %%[DashboardComponent])//,
    //Route(path = "", redirectTo = "/monitor", pathMatch = "full")
  )),
  providers = @@[WebSocketService],
  declarations = @@[AppComponent, DashboardComponent, TruckingMonitorComponent, MapComponent, EventListComponent],
  bootstrap = @@[AppComponent]
)
class AppModule
