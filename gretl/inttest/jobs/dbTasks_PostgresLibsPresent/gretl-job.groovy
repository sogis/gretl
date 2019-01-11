properties([
        // keep only the last 20 builds
        buildDiscarder(logRotator(numToKeepStr: '20')),
        // run every weekday at noon
        pipelineTriggers([
                cron('H 12 * * 1-5')
        ])
])

// kill job if it is running too long
timeout(time: 10, unit: 'MINUTES') {
    // name of GRETL runtime Docker container
    node ("gretl") {
        // Git repository containing the files for the job
        git 'https://github.com/sogis/gretl.git'

        // directory of this job, relative to the Git repository root
        dir('gretl/inttest/jobs/dbTasks_PostgresLibsPresent') {
            // show current location and content
            sh 'pwd && ls -l'

            // do the job
            sh "gretl queryPostgresVersion -Pgretltest_dburi_pg=jdbc:postgresql://${POSTGRES_GIS_SERVICE_HOST}:${POSTGRES_GIS_SERVICE_PORT}/gretl"
        }
    }
}
