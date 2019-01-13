gretl-build-template.yaml: Configuration for build of runtime docker image on openshift
init-test-db.sh and reset-test-db.sh: Scripts for postgres test-db used for db- and integrationtests --> does not belong here
postgres-gis.json: Config to install other postgres db image for system test (in openshift). Pipeline should be using the same image in all places