OpenShift GRETL system project
------------------------------
Create the GRETL system on the OpenShift container platform.


## Setup the GRETL system with Jenkins

### Create project
```
oc new-project gretl-system
```

### Optional: Create a Docker image pull secret
Create a secret for pulling images from Docker Hub, and link this secret to the default service account:
```
oc create secret docker-registry my-pull-secret --docker-username=xx --docker-password=yy
oc secrets link default my-pull-secret --for=pull
```

### GRETL-Jenkins
Apply project template with the GRETL-Jenkins configuration.
```
oc process -f openshift/templates/jenkins-s2i-persistent-template.yaml \
  -p JENKINS_CONFIGURATION_REPO_URL="https://github.com/sogis/openshift-jenkins.git" \
  -p JENKINS_DOCKER_IMAGE_TAG="4.7" \
  -p GRETL_JOB_REPO_URL="https://github.com/sogis/gretljobs.git" \
  -p VOLUME_CAPACITY="2Gi" \
  -p JENKINS_HOSTNAME="gretl.example.org" \
  | oc apply -f -
```
Parameter:
* JENKINS_CONFIGURATION_REPO_URL: Repo containing the Jenkins configuration.
* JENKINS_DOCKER_IMAGE_TAG: Jenkins base image tag to be used.
* GRETL_JOB_REPO_URL: Repo containing the GRETL jobs.
* VOLUME_CAPACITY: Persistent volume size for Jenkins configuration data, e.g. 512Mi, 2Gi.
* JENKINS_HOSTNAME: The public hostname for the Jenkins service.

### Jenkins agent

The configuration of the Jenkins agent,
including the definition of which Docker image to pull from Docker Hub.

#### Create the gretl image stream providing the GRETL image

Add the *gretl* image stream to pull the newest GRETL image:
```
oc process -f openshift/templates/gretl-is-template.yaml \
  -p GRETL_IMAGE_TAG="latest" \
  | oc apply -f -
```
Parameters:
* GRETL_IMAGE_TAG: GRETL image tag to be pulled from Docker Hub.
* IMPORT_POLICY_SCHEDULED: Regularly check for changed image; defaults to "false"

#### Create the jenkins-agent image stream providing the Jenkins agent image

Add the *jenkins-agent* image stream to pull a Jenkins agent image:
```
oc process -f openshift/templates/jenkins-agent-is-template.yaml \
  -p JENKINS_AGENT_IMAGE_TAG=4.7 \
  | oc apply -f -
```
Parameters:
* JENKINS_AGENT_IMAGE_TAG: Docker image tag of Jenkins agent to be pulled from Quay.io.
* IMPORT_POLICY_SCHEDULED: Regularly check for changed image;
  defaults to "true"
  because it seems that Quay.io immediately deletes any image that has no more tag

#### Create a ConfigMap that configures the Jenkins agent

Adapt the OpenShift project name (and maybe the image tag) in the ConfigMap
`openshift/templates/gretl-pod-template-configmap.yaml`,
then apply the ConfigMap:
```
oc apply -f openshift/templates/gretl-pod-template-configmap.yaml
```
Note: When editing the ConfigMap any further, pay close attention
to the indentation of the lines inside the `<yaml>` XML tags.

### Create or update resources to be used by GRETL

Create a ConfigMap containing the resources that may be used by GRETL:

Use the file `templates/gretl-resources-example.yaml`
as a template for creating a file named `gretl-resources-ENVIRONMENT.yaml`
and modify it to your needs.
Then run
```
oc apply -f gretl-resources-ENVIRONMENT.yaml
rm gretl-resources-ENVIRONMENT.yaml
```
(Remove the resources YAML file for security reasons.)

Steps for adding a new resource:
```
oc get --export -o yaml configmap gretl-resources > gretl-resources-ENVIRONMENT.yaml
vi gretl-resources-ENVIRONMENT.yaml
```
* Keep everything of the `data` section
* In the `data` section append the new entries, e.g. `ORG_GRADLE_PROJECT_dbUriXy: xyxyxyxy`
* Remove all entries of the `metadata` section, except the `labels` and the `name`
```
oc apply -f gretl-resources-ENVIRONMENT.yaml
rm gretl-resources-ENVIRONMENT.yaml
```
(Remove the resources YAML file for security reasons.)

### Create or update secrets to be used by GRETL

Create a Secret containing the secrets that may be used by GRETL:

Use the file `templates/gretl-secrets-example.yaml`
as a template for creating a file named `gretl-secrets-ENVIRONMENT.yaml`
and modify it to your needs.
Then run
```
oc apply -f gretl-secrets-ENVIRONMENT.yaml
rm gretl-secrets-ENVIRONMENT.yaml
```
(Remove the secrets YAML file for security reasons.)

Steps for adding a new secret:
```
oc get --export -o yaml secret gretl-secrets > gretl-secrets-ENVIRONMENT.yaml
vi gretl-secrets-ENVIRONMENT.yaml
```
* Keep everything of the `data` section
* Add a new `stringData` section
* In the `stringData` section add the new entries, e.g. `ORG_GRADLE_PROJECT_dbPwdXy: xyxyxyxy`
* Remove all entries of the `metadata` section, except the `name`
```
oc apply -f gretl-secrets-ENVIRONMENT.yaml
rm gretl-secrets-ENVIRONMENT.yaml
```
(Remove the secrets YAML file for security reasons.)

Please check if all new and old secrets have got their value assigned.
If not, run the `oc apply` command again.

### Configure a database connection
Database connections are configured globally in the OpenShift project.
Such that the GRETL-Jobs can use the url and credentials.

The url/ip-address is added as environment variable.
To protect the username and password a OpenShift secret will be used.

#### Add database secret
There is an example secret definition file to be applied:
```
oc apply -f openshift/templates/database-secret.yaml
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

### Update GRETL image
There are several ways to change the GRETL image version.

Either apply the `gretl-is-template.yaml` template again with the desired image tag.

Or set the image tag with a command like this:
```
oc tag --source=docker sogis/gretl:2.1.119 gretl:latest
```

Or edit the Image Stream manually inside the OpenShift web console:
1. go to the project
1. select Builds -> Images
1. click on the Image Stream with name *gretl*
1. select *Edit YAML* on the Actions button
1. change the image tag name to the desired version and save it. 

### Update Jenkins
Follow the steps below for updating Jenkins:

1. In a test OpenShift project, manually start a build
of *s2i-jenkins-build*: `oc start-build s2i-jenkins-build`
1. When the build has finished and the newly built image has been deployed, log into Jenkins and go to the *Manage Plugins* section
1. Go through the plugin list in https://github.com/sogis/openshift-jenkins/blob/master/plugins.txt, and locate these plugins in the *Manage Plugins* section of Jenkins; note down the versions that Jenkins would install, but do not start the update. Carefully read the warning messages in the plugin list; if there is any warning like *Warning: This plugin is built for Jenkins 2.150.3 or newer. Jenkins will refuse to load this plugin if installed.* then check in the online Jenkins plugin repository at https://plugins.jenkins.io/ which older plugin version works with your new Jenkins version.
1. Update the *plugins.txt* file mentioned above with the plugin version numbers you noted down, and commit the changes.
1. Start a manual build again and check if everything works fine. Jenkins may show you messages about unreadable configuration data; following the steps recommended by Jenkins to purge these parts of the configuration is usually safe.
1. Now you can start a build in your production OpenShift project and have Jenkins purge the unreadable configuration data here as well.

### Update Jenkins to a specific version
The deployment instructions above actually use the *Jenkins* ImageStream of the *openshift* namespace for getting the base image (see https://github.com/sogis/gretl/blob/119149e64c939eaadf9bf81764768848e9e63170/openshift/templates/jenkins-s2i-persistent-template.yaml#L76). So the Jenkins version that is going to be built depends on this ImageStream, which depends on the OpenShift version installed. The command `oc get is jenkins -n openshift -o yaml` shows you the details about the ImageStream. (By the way, the *openshift* namespace is the one that provides the Image Catalog at https://your-web-console-hostname/console/catalog.)

The same applies to the update instructions above. They will update Jenkins to the version provided by the *Jenkins* ImageStream of the *openshift* namespace. Now, if you want to update Jenkins to a specific image version (3.7, as an example), use the following patch, which will update the build configuration to the desired version. The patch bypasses the *Jenkins* ImageStream of the *openshift* namespace and uses a Docker image from a remote image registry instead.
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
