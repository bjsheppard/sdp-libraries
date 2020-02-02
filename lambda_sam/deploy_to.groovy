import static groovy.json.JsonOutput.*

void call(app_env){
  stage("Deploy to ${app_env.short_name}"){

    // aws region where lambda application lives
    String region = env.AWS_REGION ?: { error "must define AWS region where lambda application is deployed." }()

    // set bucket to use while generating cloudformation changeset
    String bucket = app_env.S3_BUCKET ?: { error "${app_env.short_name} has no S3_BUCKET defined"}

    // set cloudformation stack to deploy to
    String stack = app_env.CF_STACK ?: { error "${app_env.short_name} has no CF_STACK defined"}

    inside_sdp_image "aws", {
      withAWS(region: region){

        // call AWS library assumeRole method
        assumeRole()

        // pull workspace into container
        unstash "workspace"
          
        // create a cloudformation changeset using SAM template
        sh "aws cloudformation package --s3-bucket ${bucket} --template-file template.yaml --output-template-file template-generated.yaml"

        // deploy cloudformation changeset to stack
        // TODO - may need ability to add capabilities to '--capabilities'
        //      - can add capabilities needed in config file  
        sh "aws cloudformation deploy --capabilities CAPABILITY_IAM --template-file template-generated.yaml --stack-name ${stack}"
      }
    }
  }
}