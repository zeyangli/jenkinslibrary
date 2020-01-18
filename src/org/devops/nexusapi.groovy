package org.devops


//封装HTTP
def HttpReq(reqType,reqUrl,reqBody){
    def sonarServer = "http://192.168.1.200:30083/service/rest"
   
    result = httpRequest authentication: 'nexus-admin-user',
            httpMode: reqType, 
            contentType: "APPLICATION_JSON",
            consoleLogResponseBody: true,
            ignoreSslErrors: true, 
            requestBody: reqBody,
            url: "${sonarServer}/${reqUrl}",
            quiet: true
    
    return result
}


//获取仓库中所有组件

def GetRepoComponents(repoName){
    apiUrl = "/v1/components?repository=${repoName}"
    response = HttpReq("GET",apiUrl,'')
    
    response = readJSON text: """${response.content}"""
    println(response["items"].size())
    
    return response["items"]
}


//获取单件组件

def GetComponentsId(repoName,groupId,artifactId,version){
    println("获取单件组件ID")
    result = GetRepoComponents(repoName) 
    
    for (component in result){
        
        if (component["group"] == groupId && component["name"] == artifactId && component["version"] == version  ){
            
            componentId = component["id"]
            
            return componentId
        }
    }
    
    println(componentId)
}

//获取组件信息
def GetSingleComponents(repoName,groupId,artifactId,version){
    println("获取单件组件信息")
    componentId = GetComponentsId(repoName,groupId,artifactId,version)
    apiUrl = "/v1/components/${componentId}"
    response = HttpReq("GET",apiUrl,'')
    
    response = readJSON text: """${response.content}"""
    println(response["assets"]["downloadUrl"])
}

