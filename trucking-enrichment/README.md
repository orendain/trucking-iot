# Trucking Enrichment

A library with a set of APIs for enriching trucking data.  The different APIs that currently exist:

-   WeatherAPI
    -   def isFoggy: Boolean
    -   def isRainy: Boolean
    -   def isWindy: Boolean


-   DriverClassificationAPI
    -   def isCerfied: Boolean
    -   def wagePlan: String


-   DriverTimesheetAPI
    -   def hoursLogged: Int
    -   def milesLogged: Int


If using NiFi: A custom NiFi processor has been developed that wraps this enrichment API and generates data entirely using NiFi's drag-and-drop interface.  **No code or terminal required**.  Check out the `trucking-nifi-bundle` subproject.
