pipeline {
    agent any
    stages {
        stage('start-dbs') {
            steps {
                sh 'docker-compose up'
            }
        }
        stage('build') {
            steps {
                sh 'sh ./mvnw -s jenkins-settings.xml -e install -Dmaven.test.failure.ignore=false -T1C'
            }
        }
        stage('stop-dbs') {
            steps {
                sh 'docker-compose down'
            }
        }
    }
    post {
        always {
            junit '**/target/surefire-reports/*.xml'
            junit '**/target/failsafe-reports/*.xml'
        }
    }
}