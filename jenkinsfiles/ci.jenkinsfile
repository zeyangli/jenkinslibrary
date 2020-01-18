#!groovy

@Library('jenkinslibrary@master') _

//func from shareibrary
def build = new org.devops.build()
def deploy = new org.devops.deploy()
def tools = new org.devops.tools()
def gitlab = new org.devops.gitlab()
def toemail = new org.devops.toemail()
def sonar = new org.devops.sonarqube()
def sonarapi = new org.devops.sonarapi()
def nexus = new org.devops.nexus()
def artifactory = new org.devops.artifactory() 


def runOpts
//env
String buildType = "${env.buildType}"
String buildShell = "${env.buildShell}"
String deployHosts = "${env.deployHosts}"
String srcUrl = "${env.srcUrl}"
String branchName = "${env.branchName}"
String artifactUrl = "${env.artifactUrl}"


if ("${runOpts}" == "GitlabPush"){
    branchName = branch - "refs/heads/"
    
    currentBuild.description = "Trigger by ${userName} ${branch}"
    gitlab.ChangeCommitStatus(projectId,commitSha,"running")
    env.runOpts = "GitlabPush"

    
} else {
   userEmail = "2560350642@qq.com"
}


//pipeline
pipeline{
    agent { node { label "build"}}
    
    
    stages{

        stage("CheckOut"){
            steps{
                script{
                   
                    
                    println("${branchName}")
                
                    tools.PrintMes("获取代码","green")
                    checkout([$class: 'GitSCM', branches: [[name: "${branchName}"]], 
                                      doGenerateSubmoduleConfigurations: false, 
                                      extensions: [], 
                                      submoduleCfg: [], 
                                      userRemoteConfigs: [[credentialsId: 'gitlab-admin-user', url: "${srcUrl}"]]])

                }
            }
        }
        stage("Build"){
            steps{
                script{
                
                    tools.PrintMes("执行打包","green")
                    //build.Build(buildType,buildShell)
                    artifactory.main(buildType,buildShell)
                    artifactory.PushArtifact()
                    
                    //上传制品
                    //nexus.main("nexus")
                    
                    //发布制品
                    //sh " wget ${artifactUrl} && ls "
                    
                    
                    


                    //deploy.SaltDeploy("${deployHosts}","test.ping")
                    //deploy.AnsibleDeploy("${deployHosts}","-m ping ")
                }
            }
       }
       
       
        stage("QA"){
            steps {
                script{
                    tools.PrintMes("搜索项目","green")
                    result = sonarapi.SerarchProject("${JOB_NAME}")
                    println(result)
                    
                    if (result == "false"){
                        println("${JOB_NAME}---项目不存在,准备创建项目---> ${JOB_NAME}！")
                        sonarapi.CreateProject("${JOB_NAME}")
                    } else {
                        println("${JOB_NAME}---项目已存在！")
                    }
                    
                    tools.PrintMes("配置项目质量规则","green")
                    qpName="${JOB_NAME}".split("-")[0]   //Sonar%20way
                    sonarapi.ConfigQualityProfiles("${JOB_NAME}","java",qpName)
                
                    tools.PrintMes("配置质量阈","green")
                    sonarapi.ConfigQualityGates("${JOB_NAME}",qpName)
                
                    tools.PrintMes("代码扫描","green")
                    sonar.SonarScan("test","${JOB_NAME}","${JOB_NAME}","src")
                    

                    sleep 30
                    tools.PrintMes("获取扫描结果","green")
                    result = sonarapi.GetProjectStatus("${JOB_NAME}")
                    
                    
                    println(result)
                    if (result.toString() == "ERROR"){
                        toemail.Email("代码质量阈错误！请及时修复！",userEmail)
                        error " 代码质量阈错误！请及时修复！"
                        
                        
                    } else {
                        println(result)
                    }
                
                

                }
           }
       }
    }
    post {
        always{
            script{
                println("always")
            }
        }
        
        success{
            script{
                println("success")
                if ("${runOpts}" == "GitlabPush"){
                    gitlab.ChangeCommitStatus(projectId,commitSha,"success")
                }
                toemail.Email("流水线成功",userEmail)
            
            }
        
        }
        failure{
            script{
                println("failure")
                if ("${runOpts}" == "GitlabPush"){
                    gitlab.ChangeCommitStatus(projectId,commitSha,"failed")
                }
                toemail.Email("流水线失败了！",userEmail)
            }
        }
        
        aborted{
            script{
                println("aborted")
                if ("${runOpts}" == "GitlabPush"){
                    gitlab.ChangeCommitStatus(projectId,commitSha,"canceled")
                }
               toemail.Email("流水线被取消了！",userEmail)
            }
        
        }
    
    }
    
    
}
