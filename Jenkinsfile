node {
    stage('Checkout Repository') {
        checkout scm
    }

    stage ('init') {
        sh "chmod +x gradlew"
    }

    stage ('Build app') {
        sh "./gradlew build"
    }
}