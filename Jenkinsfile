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
                git "https://github.com/edigonzales/gretl-ng.git/"
            }
        }
        stage('Build') {
            steps {
                sh './gradlew --no-daemon clean gretl:classes'
            }
        }
   
        stage('Publish locally') {
            steps {
                sh './gradlew gretl:build gretl:publishPluginMavenPublicationToMavenLocal -x test'
            }
        }
        stage('Build docker image') {
            steps {
                dir("runtimeImage") {
                    sh "pwd"
                    sh "./build-gretl.sh ${env.GIT_COMMIT} 1.0.${env.BUILD_NUMBER}"
                }
            }
        }  
        stage('Integration Tests') {
            steps {
                sh "pwd"
                //sh './gradlew gretl:jarTest'
                sh './gradlew gretl:imageTest'
                publishHTML target: [
                    reportName : 'Image Tests',
                    reportDir:   'gretl/build/reports/tests/imageTest', 
                    reportFiles: 'index.html',
                    keepAll: true,
                    alwaysLinkToLastBuild: true,
                    allowMissing: false
                ]                                
            }
        }            
    }

}