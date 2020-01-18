package org.devops

//封装HTTP请求
def HttpReq(reqType,reqUrl,reqBody){
    def gitServer = "http://192.168.1.200:30088/api/v4"
    withCredentials([string(credentialsId: 'gitlab-token', variable: 'gitlabToken')]) {
      result = httpRequest customHeaders: [[maskValue: true, name: 'PRIVATE-TOKEN', value: "${gitlabToken}"]], 
                httpMode: reqType, 
                contentType: "APPLICATION_JSON",
                consoleLogResponseBody: true,
                ignoreSslErrors: true, 
                requestBody: reqBody,
                url: "${gitServer}/${reqUrl}"
                //quiet: true
    }
    return result
}


//更新文件内容
def UpdateRepoFile(projectId,filePath,fileContent){
    apiUrl = "projects/${projectId}/repository/files/${filePath}"
    reqBody = """{"branch": "master","encoding":"base64", "content": "${fileContent}", "commit_message": "update a new file"}"""
    response = HttpReq('PUT',apiUrl,reqBody)
    println(response)

}

//获取文件内容
def GetRepoFile(projectId,filePath){
    apiUrl = "projects/${projectId}/repository/files/${filePath}/raw?ref=master"
    response = HttpReq('GET',apiUrl,'')
    return response.content
}

//创建仓库文件
def CreateRepoFile(projectId,filePath,fileContent){
    apiUrl = "projects/${projectId}/repository/files/${filePath}"
    reqBody = """{"branch": "master","encoding":"base64", "content": "${fileContent}", "commit_message": "create a new file"}"""
    response = HttpReq('POST',apiUrl,reqBody)
    println(response)
}


//更改提交状态
def ChangeCommitStatus(projectId,commitSha,status){
    commitApi = "projects/${projectId}/statuses/${commitSha}?state=${status}"
    response = HttpReq('POST',commitApi,'')
    println(response)
    return response
}

//获取项目ID
def GetProjectID(repoName='',projectName){
    projectApi = "projects?search=${projectName}"
    response = HttpReq('GET',projectApi,'')
    def result = readJSON text: """${response.content}"""
    
    for (repo in result){
       // println(repo['path_with_namespace'])
        if (repo['path'] == "${projectName}"){
            
            repoId = repo['id']
            println(repoId)
        }
    }
    return repoId
}

//删除分支
def DeleteBranch(projectId,branchName){
    apiUrl = "/projects/${projectId}/repository/branches/${branchName}"
    response = HttpReq("DELETE",apiUrl,'').content
    println(response)
}

//创建分支
def CreateBranch(projectId,refBranch,newBranch){
    try {
        branchApi = "projects/${projectId}/repository/branches?branch=${newBranch}&ref=${refBranch}"
        response = HttpReq("POST",branchApi,'').content
        branchInfo = readJSON text: """${response}"""
    } catch(e){
        println(e)
    }  //println(branchInfo)
}

//创建合并请求
def CreateMr(projectId,sourceBranch,targetBranch,title,assigneeUser=""){
    try {
        def mrUrl = "projects/${projectId}/merge_requests"
        def reqBody = """{"source_branch":"${sourceBranch}", "target_branch": "${targetBranch}","title":"${title}","assignee_id":"${assigneeUser}"}"""
        response = HttpReq("POST",mrUrl,reqBody).content
        return response
    } catch(e){
        println(e)
    }
}

//搜索分支
def SearchProjectBranches(projectId,searchKey){
    def branchUrl =  "projects/${projectId}/repository/branches?search=${searchKey}"
    response = HttpReq("GET",branchUrl,'').content
    def branchInfo = readJSON text: """${response}"""
    
    def branches = [:]
    branches[projectId] = []
    if(branchInfo.size() ==0){
        return branches
    } else {
        for (branch in branchInfo){
            //println(branch)
            branches[projectId] += ["branchName":branch["name"],
                                    "commitMes":branch["commit"]["message"],
                                    "commitId":branch["commit"]["id"],
                                    "merged": branch["merged"],
                                    "createTime": branch["commit"]["created_at"]]
        }
        return branches
    }
}

//允许合并
def AcceptMr(projectId,mergeId){
    def apiUrl = "projects/${projectId}/merge_requests/${mergeId}/merge"
    HttpReq('PUT',apiUrl,'')
}
