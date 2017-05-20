# Trucking Web Application

Run `/scripts/builds/web-application.sh` on the cluster to build and run the application.

By default, the application runs on **port 15001**.  If you are using the HDF sandbox for this demo application, you'll find that you need to point your browser to `http://sandbox-hdf.hortonworks.com:25001` to be port forwarded to the correct port inside the sandbox.

This subproject is built with:
-   **Scala** on top of the **Play Framework** in the backend
-   **ScalaJS** and **Angular 2** in the frontend
-   **Leaflet** in the frontend for graphing purposes
