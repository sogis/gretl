OpenShift GRETL system project
------------------------------
Create the GRETL system on the OpenShift container platform.


## Setup runtime with Jenkins

### Create project
```
oc new-project gretl-system
```

### GRETL-Jenkins
Apply project template with the GRETL-Jenkins configuration.
```
oc process -f serviceConfig/templates/jenkins-s2i-persistent-template.yaml \
  -p JENKINS_CONFIGURATION_REPO_URL="https://github.com/sogis/openshift-jenkins.git" \
  -p JENKINS_IMAGE_STREAM_TAG="jenkins:2" \
  -p GRETL_JOB_REPO_URL="git://github.com/sogis/gretljobs.git" \
  -p GRETL_JOB_FILE_PATH="**" \
  -p GRETL_JOB_FILE_NAME="gretl-job.groovy" \
  -p VOLUME_CAPACITY="2Gi" \
  -p JENKINS_HOSTNAME="gretl.example.org" \
  | oc apply -f -
```
Parameter:
* JENKINS_CONFIGURATION_REPO_URL: Repo containing the Jenkins configuration.
* JENKINS_IMAGE_STREAM_TAG: Docker base image for the Jenkins. 
* GRETL_JOB_REPO_URL: Repo containing the GRETL jobs.
* GRETL_JOB_FILE_PATH: Base path to the GRETL job definitions (Ant style)
* GRETL_JOB_FILE_NAME: Name of the GRETL job configuration file.
* JENKINS_HOSTNAME: The public hostname for the Jenkins service.
* VOLUME_CAPACITY: Persistent volume size for Jenkins configuration data, e.g. 512Mi, 2Gi.

### GRETL runtime
The GRETL runtime configuration with definition of which Docker image to pull from Docker Hub.

Add gretl imagestream to pull newest GRETL runtime image:
```
oc process -f serviceConfig/templates/gretl-is-template.yaml \
  -p GRETL_RUNTIME_IMAGE="sogis/gretl-runtime:32" \
  | oc apply -f -
```
Parameter:
* GRETL_RUNTIME_IMAGE: Docker image reference of the GRETL runtime.


### Configure a database connection
Database connections are configured globally in the OpenShift project.
Such that the GRETL-Jobs can use the url and credentials.

The url/ip-address is added as environment variable.
To protect the username and password a OpenShift secret will be used.

#### Add database secret
There is an example secret definition file to be applied:
```
oc apply -fserviceConfig/templates/database-secret.yaml
```
The values of *username* and *password* of the section *stringData* will be encripted
and added to the *data* section.

The label *credential.sync.jenkins.openshift.io* must be added and set to "true".
With this configuration, the OpenShift secret will be synced to Jenkins as credential.
The name of the credential is the secret name prefixed by the OpenShift project name.

#### Configure database url and credential name
Add an environment entry with the OpenShift web console. 
1. go to the project
1. select Applications -> Deployments
1. click on the Deployment with name *jenkins*
1. select *Edit* on the Actions button
1. scroll to the *Environment Variables* section
1. add database url, click: *Add Environment Variable*
    * Name: *DB1_URL*
    * Value: *database_host:port* (ip or hostname)
1. add credential name, click: *Add Environment Variable*
    * Name: *DB1_CREDENTIAL*
    * Value: *gretl-system-db1-secret* (secret name prefixed by the OpenShift project name)
1. Save the changes

A new deplyoment should start after saving the changes. If not, click on deploy.

The new started Jenkins will have that environment variable available.

#### Pipeline example
How to use the configured database connection.

Read environment configuration from master node. It has the above defined env. entries.
```
def dbUrl = ''
def dbCredentialName = ''
node ("master") {
  dbUrl = "${DB1_URL}"
  dbCredentialName = "${DB1_CREDENTIAL}"
}
``` 

The *withCredentials* block reads the Jenkins credential with the name from the above env. entry stored as variable *dbCredentialName*.

Within that block the database user and password are available as *DB_USER* and *DB_PWD*. Those are the values from the OpenShift secret.
```
node ("gretl") {
    ...
    withCredentials([usernamePassword(credentialsId: "${dbCredentialName}", usernameVariable: 'DB_USER', passwordVariable: 'DB_PWD')]) {

        // do the job
        sh "gretl task -Pgretltest_dburi_pg=jdbc:postgresql://${dbUrl}/gretl -Pgretltest_dbuser=${DB_USER} -Pgretltest_dbpwd=${DB_PWD}"
    }
}
```
The database connection string can then be combined with the protocol, url and database name:
```jdbc:postgresql://${dbUrl}/gretl```


## Updates
Update an existing system.

### Update GRETL runtime image
There are several ways to change the GRETL runtime image version.

Apply the template from the previous section again with the desired image tag.


Apply a patch update with the desired image tag:
```
oc patch is gretl -p $'spec:\n  tags:\n  - from:\n      kind: DockerImage\n      name: sogis/gretl-runtime:32\n    name: latest'
```

Edit the version manually inside the web console of OpenShift
1. go to the project
1. select Builds -> Images
1. click on the Image Stream with name *gretl*
1. select *Edit YAML* on the Actions button
1. change the image tag name to the desired version and save it. 

### Update Jenkins version
To update the Jenkins version to 3.7, as example, use the following patch.
This will update the build configuration to the desired version.
```
oc patch bc s2i-jenkins-build -p $'spec:\n  strategy:\n    sourceStrategy:\n      from:\n        kind: DockerImage\n        name: registry.access.redhat.com/openshift3/jenkins-2-rhel7:v3.7'
```
Start a build. This will do a source-to-image build and use the new Jenkins base Docker image.

When the build is done, a deployment is triggered automatically.

#### Fallback for errors
It can be that the actual configuration differs to much. Then the change has to be done by hand.

Edit the version manually inside the web console of OpenShift
1. go to the project
1. select Builds -> Builds
1. click on the Build with name *s2i-jenkins-build*
1. select *Edit YAML* on the Actions button
1. change the version inside the strategy to the desired version and save it.
1. Start a build.

The strategy section has to look like this:
```
  strategy:
    sourceStrategy:
      from:
        kind: DockerImage
        name: 'registry.access.redhat.com/openshift3/jenkins-2-rhel7:v3.7'
```
