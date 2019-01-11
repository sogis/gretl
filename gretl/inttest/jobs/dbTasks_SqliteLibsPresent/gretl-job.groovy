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
        dir('gretl/inttest/jobs/dbTasks_SqliteLibsPresent') {
            // show current location and content
            sh 'pwd && ls -l'

            // do the job
            sh "gretl querySqliteMaster -Pgretltest_dburi=jdbc:sqlite::memory:"
        }
    }
}
