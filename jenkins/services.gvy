node {
  
   stage('Preparation') { // for display purposes
      // Get some code from a GitHub repository
      sh 'rm -rf *'
      
      checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'services']], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/ess-acppo/services.git']]])
      
      checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'nsl-infra']], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/ess-acppo/nsl-infra.git']]])
    
     
   }
   stage('Unit test') {
        
        dir('services'){
            try{
                sh 'export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64;$WORKSPACE/services/grailsw clean-all;$WORKSPACE/services/grailsw "test-app unit:"'
        
            }catch(e) {
                currentBuild.result = 'failure'
            }
           }
      
   }
   stage('Building war') {
      
        dir('services'){
         sh 'export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64;$WORKSPACE/services/grailsw war;$WORKSPACE/services/grailsw "set-war-path nsl"'
        }
      
   }
   stage("Deploy to $INVENTORY_NAME") {
      dir('nsl-infra'){
          sh 'ansible-playbook  -i inventory/$INVENTORY_NAME -u ubuntu playbooks/deploy.yml -e \'{"apps":[{"app": "services"}], "war_names": [{"war_name": "nsl#services##1.0123"}   ],   "war_source_dir": "/var/lib/jenkins/workspace/nsl-services-pipeline/services/target"}\''
      }
   }
}
