pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'your-dockerhub-username/your-image-name'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        REGISTRY_CREDENTIALS = 'docker-hub-credentials' // 在 Jenkins 中配置的 Docker Hub 凭据 ID
    }

    tools {
        maven 'Maven3' // 在 Jenkins 中配置 Maven 名称
        jdk 'JDK11'    // 在 Jenkins 中配置 JDK 名称
    }

    stages {
        stage('Clone Code') {
            steps {
                git url: 'https://github.com/yourusername/your-springboot-repo.git', branch: 'main'
            }
        }

        stage('Build with Maven') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                script {
                    docker.withRegistry('', REGISTRY_CREDENTIALS) {
                        docker.image("${DOCKER_IMAGE}:${DOCKER_TAG}").push()
                    }
                }
            }
        }

        stage('Deploy to Remote Server') {
            steps {
                sshPublisher(
                    publishers: [
                        sshPublisherDesc(
                            configName: 'your-ssh-config-name', // 在 Jenkins 配置的 SSH 主机别名
                            transfers: [
                                sshTransfer(
                                    execCommand: """
                                        docker pull ${DOCKER_IMAGE}:${DOCKER_TAG} &&
                                        docker stop myapp || true &&
                                        docker rm myapp || true &&
                                        docker run -d --name myapp -p 8080:8080 ${DOCKER_IMAGE}:${DOCKER_TAG}
                                    """
                                )
                            ]
                        )
                    ]
                )
            }
        }
    }
}
