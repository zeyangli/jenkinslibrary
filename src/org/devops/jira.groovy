package org.devops

//封装HTTP请求
def HttpReq(reqType,reqUrl,reqBody){
    def apiServer = "http://192.168.1.200:8050/rest/api/2"
   
   result = httpRequest authentication: 'jira-admin-user',
            httpMode: reqType, 
            contentType: "APPLICATION_JSON",
            consoleLogResponseBody: true,
            ignoreSslErrors: true, 
            requestBody: reqBody,
            url: "${apiServer}/${reqUrl}"
            //quiet: true
    return result
}





//执行JQL
def RunJql(jqlContent){
    apiUrl = "search?jql=${jqlContent}"
    response = HttpReq("GET",apiUrl,'')
    return response
}