package org.devops 


//saltstack
def SaltDeploy(hosts,func){
    sh " salt \"${hosts}\" ${func} "
}


//ansible

def AnsibleDeploy(hosts,func){
    sh " ansible ${func} ${hosts}"
    
    
}