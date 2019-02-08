# Pushes the tested image to dockerhub
hub_user=sogispipeline
hub_pass=$1

docker login -u $hub_user -p $hub_pass
docker push sogis/gretl-runtime