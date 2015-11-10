# Simple on-premise data source connectivity validation tool

Use this utility to validate that Bluemix applications can access on-premise data sources that you've exposed using the [Secure Gateway service](https://console.ng.bluemix.net/catalog/secure-gateway) and user-provided services.

![sample output](https://raw.githubusercontent.com/wiki/ibm-cds-labs/on-prem-connectivity-test-java-sample/images/BM_sample_app_output.png)  

For a quick introduction on how to configure the gateway service, set up user-provided services and prepare your on-premise environment, read [the tutorial](https://developer.ibm.com/clouddataservices/access-an-on-premises-db2-data-server-from-the-bluemix-cloud/).

Out of the box, this application supports DB2 for Linux, Unix and Windows databases. You can extend this application by creating a custom connector for other data sources.

## Deploy to IBM Bluemix

You can deploy this application to Bluemix using the button provided below or manually. Note that Bluemix applications can only access services that are located in the same space. You 
*must* therefore deploy this application to a space where user-provided services have already been created or where user-provided services will be created.

### Simple deployment using the Deploy to Bluemix button

The fastest way to deploy this application to Bluemix is to click the button. 

[![Deploy to Bluemix](https://deployment-tracker.mybluemix.net/stats/342af0e859ee71ef16193deff87d5771/button.svg)](https://bluemix.net/deploy?repository=https://github.com/ibm-cds-labs/on-prem-connectivity-test-java-sample.git)

**Don't have a Bluemix account?** If you haven't already, you'll be prompted to [sign up](http://www.ibm.com/cloud-computing/bluemix/) for a Bluemix account when you click the button.  Sign up, verify your email address, then return here and click the the **Deploy to Bluemix** button again. Your new credentials let you deploy to the platform and also to code online with Bluemix and Git. If you have questions about working in Bluemix, find answers in the [Bluemix Docs](https://www.ng.bluemix.net/docs/).

Once the application is deployed, follow the instructions in section *Binding user-provided services* below.

### Manual deployment

To manually deploy this application to Bluemix:

    $ git clone https://github.com/ibm-cds-labs/on-prem-connectivity-test-java-sample.git

    $ cd on-prem-connectivity-test-java-sample

    $ ant 

    $ cd target

    $ cf push

**Note:** You may notice that Bluemix assigns a URL to your application containing a random word. This is defined in the `manifest.yml` file where the `random-route` key set to the value of `true`. This ensures that multiple people deploying this application to Bluemix do not run into naming collisions. To specify your own route, remove the `random-route` line from the `manifest.yml` file and add a `host` key with the unique value you would like to use for the host name.

### Binding user-provided services

On-premise data sources are typically exposed to Bluemix applications using user-provided services, as described in the tutorial. By default, no services are bound to this application and the tool therefore won't run any tests. To validate that an application can successfully use the user-provided services that you have defined, bind them to this application using the Bluemix web console or the Cloud Foundry CLI client.

#### Binding user-provided services using the Bluemix web console
  * [Log in to Bluemix](https://console.ng.bluemix.net/).
  * Open the *DASHBOARD* and navigate to the space where you've deployed the application.
  * Double-click on the **on-prem-data-source-access-test** application tile.
  * Click **BIND A SERVICE OR API**.
  * Select the desired service instances.

If you bind a service other than *user-provided*, it is ignored by the application.

Refresh the application web page to view the test results.  

#### Binding user-provided services using the Cloud Foundry CLI

To bind a user-provided service to the application run the following command, replacing *SERVICE_INSTANCE* with the name of the appropriate service, such as *sample_db2inst1_on_QA1*.

    $ cf bind-service on-prem-data-source-access-test SERVICE_INSTANCE

Repeat this step for every user-provided service that you would like to test. After you've bound the desired services, restage the application by running

    $ cf restage on-prem-data-source-access-test

If you bind a service other than *user-provided* it is ignored by the application.

Refresh the application web page to view the test results.  

### Privacy Notice

This web application includes code to track deployments to [IBM Bluemix](https://www.bluemix.net/) and other Cloud Foundry platforms. The following information is sent to a [Deployment Tracker](https://github.com/cloudant-labs/deployment-tracker) service on each deployment:

* Application Name (`application_name`)
* Space ID (`space_id`)
* Application Version (`application_version`)
* Application URIs (`application_uris`)

This data is collected from the `VCAP_APPLICATION` environment variable in IBM Bluemix and other Cloud Foundry platforms. This data is used by IBM to track metrics around deployments of sample applications to IBM Bluemix to measure the usefulness of our examples, so that we can continuously improve the content we offer to you. Only deployments of sample applications that include code to ping the Deployment Tracker service will be tracked.

#### Disabling Deployment Tracking

To disable deployment tracking rebuild the war file using the `build-plain-war` target and deploy the app.

### License

Licensed under the [Apache License, Version 2.0](https://github.com/ibm-cds-labs/on-prem-connectivity-test-java-sample/blob/master/LICENSE).
