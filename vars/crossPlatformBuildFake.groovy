def getBuildContext(Map config, String architecture) {
    if (architecture=='windows-amd64') {
        return config.windowsContext
    }
    return config.linuxContext
}

def buildAndPush() {
    def image = docker.build("${REPO_NAME}:${TAG}", "--pull -f ${BUILD_CONTEXT}/${DOCKERFILE} ${BUILD_CONTEXT}")
        withDockerRegistry([credentialsId: "docker-hub", url: ""]) {
            image.push()
        }
}

def call(Map config) {
    pipeline {
        agent any
        environment {
            REPO_NAME = "${config.repoName}"
        }
        stages {
            stage('build') {
                environment {
                    DOCKERFILE = 'Dockerfile'
                }
                parallel {
                    stage('linux-amd64') {
                        environment {
                            BUILD_CONTEXT = getBuildContext(config, env.STAGE_NAME)
                            TAG = "$STAGE_NAME"
                            // Feed the Docker Linux Server
                        }
                        steps {
                            script {
                                buildAndPush()
                            }
                        }
                    }
                    stage('windows-amd64') {
                        environment {
                            BUILD_CONTEXT = getBuildContext(config, env.STAGE_NAME)
                            TAG = "$STAGE_NAME"
                            // Feed the Docker Windows Server
                        }
                        steps {
                            script {
                                buildAndPush()
                            }
                        }
                    }
                }
            }
            stage('notify') {
                steps {
                    echo "https://hub.docker.com/r/$REPO_NAME"
                }
            }
        }
    }
}