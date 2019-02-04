#!/usr/bin/env groovy

pipeline {
    agent any
    
    environment {
        oracleUsername = credentials('oracleUsername')
        oraclePassword = credentials('oraclePassword')
    }    

    stages {
        stage('Prepare') {
            steps {
                echo "Checkout sources"
                git "https://github.com/edigonzales/gretl-ng.git/"
            }
        }
        
        stage('Build') {
            steps {
                sh './gradlew --no-daemon clean gretl:classes'
            }
        }

        stage('Unit Tests') {
            steps {
                sh './gradlew --no-daemon gretl:test gretl:dbTest'
                publishHTML target: [
                    reportName : 'Gradle Tests',
                    reportDir:   'gretl/build/reports/tests/test', 
                    reportFiles: 'index.html',
                    keepAll: true,
                    alwaysLinkToLastBuild: true,
                    allowMissing: false
                ]                
            }
        }    
        
        stage('Publish (locally)') {
            steps {
                sh './gradlew gretl:build gretl:publishPluginMavenPublicationToMavenLocal -x test'
            }
        }
        
        stage('Build docker image') {
            steps {
                dir("runtimeImage") {
                    sh "pwd"
                    echo ${env.BUILD_NUMBER}
                    sh './build-gretl.sh'
                }
            }
        }  
        
        stage('Integration Tests') {
            steps {
                sh "pwd"
            }
        }        
              
    }
    post {
        always {
            deleteDir() 
        }
    }
}