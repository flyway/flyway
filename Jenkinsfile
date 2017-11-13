pipeline {
    agent any
    stages {
        stage('build') {
            steps {
                sh 'sh ./mvnw -s jenkins-settings.xml -e install -Dmaven.test.failure.ignore=false -T1C'
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