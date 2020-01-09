package org.devops


//Maven打包构建
def MavenBuild(buildShell){
    def server = Artifactory.newServer url: "http://192.168.1.200:30082/artifactory"
    def rtMaven = Artifactory.newMavenBuild()
    def buildInfo
    server.connection.timeout = 300
    server.credentialsId = 'artifactory-admin-user' 
    //maven打包
    rtMaven.tool = 'M2' 
    buildInfo = Artifactory.newBuildInfo()

    String newBuildShell = "${buildShell}".toString()
    println(newBuildShell)
    rtMaven.run pom: 'pom.xml', goals: newBuildShell, buildInfo: buildInfo
    //上传build信息
    server.publishBuildInfo buildInfo
}



//上传制品
def PushArtifact(){
    
    
    //重命名制品
    def jarName = sh returnStdout: true, script: "cd target;ls *.jar"
    jarName = jarName - "\n"
    def pom = readMavenPom file: 'pom.xml'
    env.pomVersion = "${pom.version}"
    env.serviceName = "${JOB_NAME}".split("_")[0]
    env.buildTag = "${BUILD_ID}"
    def newJarName = "${serviceName}-${pomVersion}-${buildTag}.jar"
    println("${jarName}  ------->>> ${newJarName}")
    sh " mv target/${jarName}  target/${newJarName}"
    
    //上传制品
    env.businessName = "${env.JOB_NAME}".split("-")[0]
    env.repoName = "${businessName}-${JOB_NAME.split("_")[-1].toLowerCase()}"
    println("本次制品将要上传到${repoName}仓库中!")   
    env.uploadDir = "${repoName}/${businessName}/${serviceName}/${pomVersion}"
    
    println('上传制品')
    rtUpload (
        serverId: "artifactory",
        spec:
            """{
            "files": [
                {
                "pattern": "target/${newJarName}",
                "target": "${uploadDir}/"
                }
            ]
            }"""
    )
}


def main(buildType,buildShell){
    if(buildType == "mvn"){
        MavenBuild(buildShell)
    }
}