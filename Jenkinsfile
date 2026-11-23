// SE 481 Tetris CI/CD Pipeline (https://github.com/nikitasharma768/tetris.git)
// Docker build/push stages stay skipped until Dockerfile and CONTAINER_IMAGE are provided.
pipeline {
    agent any

    tools {
        jdk 'jdk-21'
        maven 'maven-system'
    }

    options {
        skipDefaultCheckout()
        disableConcurrentBuilds()
    }

    parameters {
        string(name: 'CONTAINER_IMAGE', defaultValue: '', description: 'Docker Hub namespace/image')
    }

    environment {
        DOCKER_HUB_CREDENTIALS = 'dockerhub-ci'
    }

    stages {
        stage('Checkout') {
            steps {
                deleteDir()
                script {
                    env.CHECKED_OUT_BRANCH = checkout(scm).GIT_BRANCH ?: ''
                }
            }
        }

        stage('Compile') {
            steps {
                sh 'mvn -B -ntp compile'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn -B -ntp test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn -B -ntp -DskipTests package'
            }
        }

        stage('Docker build') {
            when {
                allOf {
                    expression { return params.CONTAINER_IMAGE?.trim() }
                    expression { return fileExists('Dockerfile') }
                }
            }
            steps {
                sh '''
                    docker build --tag "$CONTAINER_IMAGE:$BUILD_NUMBER" .
                    docker tag "$CONTAINER_IMAGE:$BUILD_NUMBER" "$CONTAINER_IMAGE:latest"
                '''
            }
        }

        stage('Docker push') {
            when {
                allOf {
                    expression { return params.CONTAINER_IMAGE?.trim() }
                    expression { return fileExists('Dockerfile') }
                    expression { return env.CHECKED_OUT_BRANCH in ['origin/main', 'main', 'refs/heads/main'] }
                }
            }
            steps {
                withCredentials([usernamePassword(
                    credentialsId: env.DOCKER_HUB_CREDENTIALS,
                    usernameVariable: 'DOCKER_HUB_USER',
                    passwordVariable: 'DOCKER_HUB_TOKEN'
                )]) {
                    sh '''
                        set +x
                        export DOCKER_CONFIG
                        DOCKER_CONFIG=$(mktemp -d)
                        trap 'rm -rf -- "$DOCKER_CONFIG"' EXIT
                        printf '%s\n' "$DOCKER_HUB_TOKEN" |
                            docker login --username "$DOCKER_HUB_USER" --password-stdin
                        docker push "$CONTAINER_IMAGE:$BUILD_NUMBER"
                        docker push "$CONTAINER_IMAGE:latest"
                    '''
                }
            }
        }
    }

    post {
        failure {
            emailext(
                to: '$DEFAULT_RECIPIENTS',
                subject: "Failed: $env.JOB_NAME #$env.BUILD_NUMBER",
                body: "See $env.BUILD_URL for the failed build."
            )
        }
        fixed {
            emailext(
                to: '$DEFAULT_RECIPIENTS',
                subject: "Fixed: $env.JOB_NAME #$env.BUILD_NUMBER",
                body: "See $env.BUILD_URL for the recovered build."
            )
        }
    }
}
