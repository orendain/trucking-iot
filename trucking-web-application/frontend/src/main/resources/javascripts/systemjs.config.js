/**
 * Basic System.js configuration for angulate2.
 * Adjust as necessary for your application needs.
 */
(function (global) {
  System.config({
    // Set up path alias
    paths: {
      'webjars:': 'assets/lib/'
    },
    // Map tells the System loader where to look for things
    map: {
      // App root is also web root
      app: '/',
      
      // Angular bundles
      '@angular/core': 'webjars:angular__core/bundles/core.umd.min.js',
      '@angular/common': 'webjars:angular__common/bundles/common.umd.min.js',
      '@angular/compiler': 'webjars:angular__compiler/bundles/compiler.umd.min.js',
      '@angular/platform-browser': 'webjars:angular__platform-browser/bundles/platform-browser.umd.min.js',
      '@angular/platform-browser-dynamic': 'webjars:angular__platform-browser-dynamic/bundles/platform-browser-dynamic.umd.min.js',

      // Unused Angular bundles
      '@angular/http': 'webjars:angular__http/bundles/http.umd.min.js',
      '@angular/router': 'webjars:angular__router/bundles/router.umd.min.js',
      '@angular/forms': 'webjars:angular__forms/bundles/forms.umd.min.js',

      // other libraries
      'rxjs':                      'webjars:rxjs'

    },
    // Packages tells the System loader how to load when no filename and/or no extension
    packages: {
      app: {
        // Main script to be loaded, relative to value of app ('/')
        main: 'assets/trucking-web-application-frontend-sjsx.js',
        map: {
          // Scala.js module to be loaded
          scalaModule: 'assets/trucking-web-application-frontend-fastopt.js'
          //scalaModule: 'assets/trucking-web-application-frontend-opt.js'
        },
        format: 'cjs',
        defaultExtension: 'js'
      },
      rxjs: {
        defaultExtension: 'js'
      }
    }
  });
})(this);
