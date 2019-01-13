import com.puzzleitc.jenkins.DockerHub

/**
 * Used to keep only a certain amount of versions of a Docker Hub repository.
 * Needed parameter:
 * - dockerHubUser: Docker Hub user for authentication (text parameter)
 * - dockerHubPwd: Docker Hub password for authentication (password parameter)
 * - organisation: user / organisation of the repository (text parameter)
 * - repository: repository to check the tags (text parameter)
 * - numberOfTagsToKeep: amount of tags to keep (text parameter)
 * - testMode: true does not delete any tags (boolean parameter)
 */

node {
    def dockerHub = new DockerHub(this)
    def token
    def buildNumberTags

    stage('check parameter') {
        check.mandatoryParameter('dockerHubUser')
        check.mandatoryParameter('dockerHubPwd')
        check.mandatoryParameter('organisation')
        check.mandatoryParameter('repository')
        check.mandatoryParameter('numberOfTagsToKeep')
        check.mandatoryParameter('testMode')
    }
    stage('login') {
        token = dockerHub.createLoginToken(params.dockerHubUser, dockerHubPwd)
        if (token == null) {
            currentBuild.result = 'FAILURE'
            error('Login not successful!')
        }
    }
    stage('read tags') {
        def tags = dockerHub.readTags(params.organisation, params.repository, token)
        println "all tags: " + tags

        buildNumberTags = tags.findAll{ it != null && it.isInteger()  }.collect { it as int }.sort()
        println "build number tags: " + buildNumberTags
    }
    stage('delete tags') {
        int numberToKeep = params.numberOfTagsToKeep as Integer
        if (buildNumberTags.size() > numberToKeep) {
            def numberToDelete = buildNumberTags.size() - numberToKeep
            for (int i = 0; i < numberToDelete; i++) {
                String delTag = buildNumberTags.get(i)

                println "tag to remove: " + delTag
                if (params.testMode) {
                    println "testMode: tag <" + delTag + "> would be removed"
                } else {
                    println "remove tag: " + delTag
                    dockerHub.deleteByTag(dockerHubUser, params.repository, delTag, token)
                }
            }
        } else {
            println "nothing to do, only " + buildNumberTags.size() + " tags available."
        }
    }
}
