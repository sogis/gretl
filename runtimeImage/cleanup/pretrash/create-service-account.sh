#!/bin/sh

# configuration script for creation of the jenkins service accout
# this service account is needed to deploy the application

cat <<EOF | oc create -f -
{
  "apiVersion": "v1",
  "kind": "ServiceAccount",
  "metadata": {
    "name": "jenkins"
  }
}
EOF

oc get sa jenkins
oc policy add-role-to-user edit system:serviceaccount:$(oc project --short):jenkins
oc policy add-role-to-user view system:serviceaccount:$(oc project --short):default -n $(oc project --short)
oc policy who-can edit $(oc project --short)
