package com.orendainx.hortonworks.trucking.webapp

import angulate2.platformBrowser.BrowserModule
import angulate2.std._

/**
  * @author Edgar Orendain <edgar@orendainx.com>
  */
@NgModule(
  imports = @@[BrowserModule],
  providers = @@[WebSocketService],
  declarations = @@[AppComponent, MapComponent, EventListComponent],
  bootstrap = @@[AppComponent]
)
class AppModule