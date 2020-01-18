package org.devops 

//封装HTTP请求
def HttpReq(reqType,reqUrl,reqBody,reqFile='',contentType="APPLICATION_JSON"){
    result = httpRequest httpMode: reqType, 
                        contentType: contentType,
                        consoleLogResponseBody: true,
                        ignoreSslErrors: true, 
                        uploadFile: reqFile,
                        requestBody: reqBody,
                        url: "${reqUrl}"
                        //quiet: true
    
    return result
}

//FIR 获取上传凭证
def GetCert(appType,bundleId){
    reqUrl = "http://api.fir.im/apps"
    withCredentials([string(credentialsId: 'fir-admin-token', variable: 'firadmintoken')]) {
        reqBody = """{"type":"${appType}","bundle_id":"${bundleId}","apiToken":"${firadmintoken}"}"""
    }
    response = readJSON text: """${HttpReq("POST",reqUrl,reqBody).content}"""

    return response['cert']['binary']
}

//FIR 上传APK
def UploadFir(appType,bundleId,appName,appVersion,appPath){
    result = GetCert(appType,bundleId)
    uploadUrl = result["upload_url"]
    uploadKey = result['key']
    uploadToken = result['token']
    
    /*reqBody = """  {"key": result['key'],
                    "token" : result['token'],
                    "x:name": appName ,
                    "x:build" : "${BUILD_ID}",
                    "x:version" : appVersion} """
    response = HttpReq("POST",uploadUrl,reqBody,appPath,'APPLICATION_OCTETSTREAM')*/
    
    sh """

        curl -F "key=${uploadKey}"       \
             -F "token=${uploadToken}"   \
             -F "file=@${appPath}"       \
             -F "x:name=${appName}"       \
             -F "x:version=${appVersion}"  \
             -F "x:build=${BUILD_ID}"    "${uploadUrl}"
       """
}



//puer
def UploadPgyer(appPath){
    reqUrl = "https://upload.pgyer.com/apiv1/app/upload"
    /*reqBody = """{" uKey" : '7b70873bb4d6e11143f94af9611d2ae5',
                    "_api_key" : 'a9acab611e1556015382c5cae360a5ab'}  """*/
         
    withCredentials([usernamePassword(credentialsId: 'pgyer-admin-token', passwordVariable: 'apikey', usernameVariable: 'ukey')]) {
   
       result =  sh returnStdout: true, script: """curl -F "file=@${appPath}" -F "uKey=${ukey}" -F "_api_key=${apikey}" ${reqUrl}"""
    }
    result = result - "\n"
    println(result)
    return result
}
