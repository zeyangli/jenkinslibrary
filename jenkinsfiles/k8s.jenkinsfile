pipeline {
    agent {
        kubernetes {
            label 'img'
            yaml """
kind: Pod
metadata:
  name: img
  annotations:
    container.apparmor.security.beta.kubernetes.io/img: unconfined  
spec:
  containers:
  - name: golang
    image: golang:1.11
    command:
    - cat
    tty: true
  - name: img
    workingDir: /home/jenkins
    image: caladreas/img:0.5.1
    imagePullPolicy: Always
    securityContext:
        rawProc: true
        privileged: true
    command:
    - cat
    tty: true
    volumeMounts:
      - name: jenkins-docker-cfg
        mountPath: /root
  volumes:
  - name: temp
    emptyDir: {}
  - name: jenkins-docker-cfg
    projected:
      sources:
      - secret:
          name: regcred
          items:
            - key: .dockerconfigjson
              path: .docker/config.json
"""
        }
    }
    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/joostvdg/cat.git'
            }
        }
        stage('Build') {
            steps {
                container('golang') {
                    sh './build-go-bin.sh'
                }
            }
        }
        stage('Make Image') {
            steps {
                container('img') {
                    sh 'mkdir cache'
                    sh 'img build -s ./cache -f Dockerfile.run -t caladreas/cat .'
                }
            }
        }
    }
}
