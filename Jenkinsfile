pipeline {
  agent any

  environment {
    SWARM_MANAGER_HOST = credentials('swarm-manager-ip-address')

    REGISTRY_ACCOUNT = credentials('registry-login')
    REGISTRY_PASSWORD = credentials('registry-password')

    STACK_DIR= 'ci-provision'
    STACK_NAME = 'currency'
  }
  stages {
    stage('package') {
      steps {
        sh 'mvn clean package deploy'
        script {
          env.IMAGE_TAG = "1.0.${env.BUILD_NUMBER}"
          env.VERSION = "${env.IMAGE_TAG}"
        }
      }
    }
    stage('build images and publish') {
      when {
        expression {
          currentBuild.result == null || currentBuild.result == 'SUCCESS'
        }
      }
      steps {
        sh 'docker login -u ${REGISTRY_ACCOUNT} -p ${REGISTRY_PASSWORD}'

        sh '''
          export IMAGE_NAME=currency-api
          
          docker build \
           -t ${REGISTRY_ACCOUNT}/${IMAGE_NAME}:$IMAGE_TAG \
           -t ${REGISTRY_ACCOUNT}/${IMAGE_NAME}:latest api 
           
          docker push ${REGISTRY_ACCOUNT}/${IMAGE_NAME}:$IMAGE_TAG
          docker push ${REGISTRY_ACCOUNT}/${IMAGE_NAME}:latest
        '''

        script {
          env.DOCKER_HOST = "ssh://jenkins@${SWARM_MANAGER_HOST}"
        }
      }
      post {
        always {
          sh 'docker logout'
        }
      }
    }
    stage('deploy dev') {
      environment {
        APPLICATION_ENVIRONMENT = 'dev'
      }
      steps {
        input('Deploy to dev')
        sh '''
          docker login -u ${REGISTRY_ACCOUNT} -p ${REGISTRY_PASSWORD} ${REGISTRY_URL}
          
          test -f ${STACK_DIR}/${APPLICATION_ENVIRONMENT}-variables.env && export $(cat ${STACK_DIR}/${APPLICATION_ENVIRONMENT}-variables.env)
          envsubst < ${STACK_DIR}/template/docker-stack.tmpl > ${STACK_DIR}/docker-stack.yml
          docker stack deploy -c ${STACK_DIR}/docker-stack.yml --with-registry-auth ${APPLICATION_ENVIRONMENT}-${STACK_NAME}
        '''
      }
      post {
        always {
          sh 'docker logout'
        }
      }
    }
    stage('deploy e2e') {
      environment {
        APPLICATION_ENVIRONMENT = 'e2e'
      }
      steps {
        input('Deploy to e2e')
        sh '''
          docker login -u ${REGISTRY_ACCOUNT} -p ${REGISTRY_PASSWORD} ${REGISTRY_URL}
          
          test -f ${STACK_DIR}/${APPLICATION_ENVIRONMENT}-variables.env && export $(cat ${STACK_DIR}/${APPLICATION_ENVIRONMENT}-variables.env)
          envsubst < ${STACK_DIR}/template/docker-stack.tmpl > ${STACK_DIR}/docker-stack.yml
          docker stack deploy -c ${STACK_DIR}/docker-stack.yml --with-registry-auth ${APPLICATION_ENVIRONMENT}-${STACK_NAME}
        '''
      }
      post {
        always {
          sh 'docker logout'
        }
      }
    }
    stage('deploy staging') {
      environment {
        APPLICATION_ENVIRONMENT = 'staging'
      }
      steps {
        input('Deploy to staging')
        sh '''
          docker login -u ${REGISTRY_ACCOUNT} -p ${REGISTRY_PASSWORD}
          
          test -f ${STACK_DIR}/${APPLICATION_ENVIRONMENT}-variables.env && export $(cat ${STACK_DIR}/${APPLICATION_ENVIRONMENT}-variables.env)
          envsubst < ${STACK_DIR}/template/docker-stack.tmpl > ${STACK_DIR}/docker-stack.yml
          docker stack deploy -c ${STACK_DIR}/docker-stack.yml --with-registry-auth ${APPLICATION_ENVIRONMENT}-${STACK_NAME}
        '''
      }
      post {
        always {
          sh 'docker logout'
        }
      }
    }
    stage('deploy prod') {
      environment {
        APPLICATION_ENVIRONMENT =  'prod'
      }
      steps {
        input('Deploy to prod')
        sh '''
          docker login -u ${REGISTRY_ACCOUNT} -p ${REGISTRY_PASSWORD}
          
          test -f ${STACK_DIR}/${APPLICATION_ENVIRONMENT}-variables.env && export $(cat ${STACK_DIR}/${APPLICATION_ENVIRONMENT}-variables.env)
          envsubst < ${STACK_DIR}/template/docker-stack.tmpl > ${STACK_DIR}/docker-stack.yml
          docker stack deploy -c ${STACK_DIR}/docker-stack.yml --with-registry-auth ${APPLICATION_ENVIRONMENT}-${STACK_NAME}
        '''
      }
      post {
        always {
          sh 'docker logout'
        }
      }
    }
  }
}
