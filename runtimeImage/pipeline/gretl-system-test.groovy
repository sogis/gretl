/**
 * Runs tests against a Jenkins running on OpenShift. For that the GRETL Jenkins inside OpenShift is needed.
 * The Jenkinis will be prepared and the port is forwarded to give access to it.
 * The GRETL runtime Docker image will be pushed to the repository with the build number as tag.
 * Needed parameter:
 * - repository: repository incl. user / organisation to push the Docker image to (text parameter)
 * - gitRepository: Url of the Git repository with the source code. (text parameter)
 * - openShiftCluster: OpenShift Cluster to be used. (text parameter)
 * - openShiftProject: OpenShift project to be used. (text parameter)
 * - ocToolName: Jenkins custom tool name of oc client. (text parameter)
 * - openShiftDeployTokenName: secret token of the service account. (text parameter)
 * - jenkinsUser: name of Jenkins user to login into the Jenkins API (text parameter)
 * - jenkinsToken: login token of the Jenkins user to login into the Jenkins API (password parameter)
 *                 The API token is available in your personal configuration page.
 *                 Click your name on the top right corner on every page, then click "Configure" to see your API token.
 *                 (The URL $root/me/configure is a good shortcut.)
 * Optional parameter:
 * - buildTime: Test build time in minutes. Control over port forward duration. (text parameter)
 */
pipeline {
    agent any
    tools {
        'jdk' "OpenJDK 1.8"
    }
    stages {
        stage('check parameter') {
            steps {
                script {
                    check.mandatoryParameter('repository')
                    check.mandatoryParameter('openShiftCluster')
                    check.mandatoryParameter('openShiftProject')
                    check.mandatoryParameter('ocToolName')
                    check.mandatoryParameter('openShiftDeployTokenName')
                    check.mandatoryParameter('jenkinsUser')
                    check.mandatoryParameter('jenkinsToken')
                }
            }
        }

        stage('prepare system test') {
            steps {
                sh 'rm -rf build-tmp'
                sh 'mkdir -p build-tmp/inttest'

                sh 'cp -R gretl/inttest/* build-tmp/inttest/'

                sh 'ls -la build-tmp/inttest'

                // prepare postgres-db for systemtest
                script {
                    timeout(10) {
                        def ocDir = tool params.ocToolName
                        withEnv(["PATH+OC=${ocDir}"]) {
                            sh "oc version"
                            openshift.withCluster(params.openShiftCluster, params.openShiftDeployTokenName) {
                                openshift.withProject(params.openShiftProject) {
                                    echo "Running in project: ${openshift.project()}"

                                    // find database pod with name: postgresql
                                    def podSelector = openshift.selector('pods', [name: 'postgres-gis'])
                                    def dbPod = podSelector.name()
                                    String shortName = dbPod.indexOf("/") > 0 ? dbPod.substring(dbPod.indexOf("/") + 1) : dbPod;
                                    println "db pod: " + shortName

                                    // prepare database
                                    openshift.raw("cp", "runtimeImage/pipeline/scripts/reset-test-db.sh", "${shortName}:/tmp/")
                                    echo openshift.exec(shortName, 'bash', '/tmp/reset-test-db.sh').out

                                    openshift.raw("cp", "runtimeImage/pipeline/scripts/init-test-db.sh", "${shortName}:/tmp/")
                                    echo openshift.exec(shortName, 'bash', '/tmp/init-test-db.sh').out
                                }
                            }
                        }
                    }
                }
            }
        }
        stage('run system test') {
            steps {
                script{
                    timeout(10) {
                        def ocDir = tool params.ocToolName
                        withEnv(["PATH+OC=${ocDir}"]) {
                            sh "oc version"
                            openshift.withCluster(params.openShiftCluster, params.openShiftDeployTokenName) {
                                openshift.withProject(params.openShiftProject) {
                                    echo "Running in project: ${openshift.project()}"

                                  //  def gretlIS = openshift.selector('is/gretl').object()
                                  //  println "GRETL Runtime version: -> " + gretlIS.spec.tags['from'].name


                                    // find jenkins pod with name: jenkins
                                    def podSelector = openshift.selector('pods', [name: 'jenkins'])
                                    def dbPod = podSelector.name()
                                    String shortName = dbPod.substring(dbPod.indexOf("/") + 1);
                                    println "Jenkins pod: " + shortName

                                    parallel(
                                        port_forward: {
                                            def buildTime = 4
                                            if (params.buildTime != null) {
                                                buildTime = params.buildTime as int
                                            }
                                            try {
                                                timeout(buildTime) {
                                                    echo "Forward Jenkins API port"
                                                    openshift.raw( 'port-forward', shortName, '8081:8080' )
                                                }
                                            } catch (err) {} // catch timeout
                                            println "port forward done (time: ${buildTime})"
                                        },
                                        test: {
                                            echo "run system tests ..."
                                            dir('build-tmp/inttest') {
                                                sh './gradlew -version'
                                                sh "./gradlew clean build testSystem -Dgretltest_jenkins_uri=http://localhost:8081 -Dgretltest_jenkins_user=${params.jenkinsUser} -Dgretltest_jenkins_pwd=${params.jenkinsToken} --refresh-dependencies"
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            post {
                always {
                    junit 'build-tmp/inttest/build/test-results/**/*.xml'  // Requires JUnit plugin
                }
            }
        }
        stage('Docker Hub push') {
            steps {
                script{
                    timeout(10) {
                        def ocDir = tool params.ocToolName
                        withEnv(["PATH+OC=${ocDir}"]) {
                            sh "oc version"
                            openshift.withCluster(params.openShiftCluster, params.openShiftDeployTokenName) {
                                openshift.withProject(params.openShiftProject) {
                                    echo "Running in project: ${openshift.project()}"

                                    // update docker image tag with build number
                                    def imageRef = "docker.io/${params.repository}:${BUILD_NUMBER}"
                                    def bc = openshift.selector('bc/gretlout').object()
                                    bc.spec.output.to['name'] = imageRef
                                    openshift.apply(bc)


                                    def builds = openshift.startBuild("gretlout")
                                    def result
                                    builds.untilEach(1) {
                                        echo "Created builds so far: ${it.names()}"
                                        result = it.object().status.phase
                                        return result == "Complete" || result == "Cancelled" || result == "Failed"
                                    }

                                    if (result == "Complete") {
                                        echo "pushed image: ${imageRef}"
                                    }
                                    else {
                                        error('Docker Hub push: ' + result)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
