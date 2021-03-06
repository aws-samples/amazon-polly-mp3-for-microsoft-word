#! /bin/bash

# Command to run from project root
# bash scripts/deploy_lambda_cfn.sh <region> <lambda-bucket-name> <stack-name> ; e.g.,
# bash scripts/deploy_lambda_cloudformation.sh us-east-2 cftests-us-east-2 polly-stack

REGION=$1
POLLY_ASSETS_BUCKET=$2
STACK_NAME=$3

echo "Checking for pre-requisites"

checkAndProceed() {
  cmd=$1
  name=$2
  cmd_out=$(${cmd})
  retVal=$?
  if [[ ${retVal} -ne 0 ]]; then
    echo "${name} not present"
    echo "Please install ${name} and make sure it is available in the path and retry"
    exit ${retVal}
  else
    echo "Found ${name}"
  fi
}

checkBucketExists() {
  bucket=$1
  cmd_out=$(aws s3 ls ${bucket})
  retVal=$?
    if [[ ${retVal} -ne 0 ]]; then
    echo "${bucket} bucket not present"
    echo "Please create S3 bucket: ${bucket} in $REGION Region and retry"
    exit ${retVal}
  else
    echo "Found bucket: ${bucket}"
  fi
}

checkAndProceed "which sed" "sed"
checkAndProceed "aws --version" "AWS CLI"
checkAndProceed "java -version" "Java"
checkAndProceed "mvn --version" "Apache Maven"
checkBucketExists "${POLLY_ASSETS_BUCKET}"

cd lambda
for file in *.py;
do
    filePart="${file%.*}"
    echo ${filePart}
    zip ${filePart}.zip ${filePart}.py
    aws s3 cp ${filePart}.zip s3://${POLLY_ASSETS_BUCKET}/assets/ --region ${REGION}
done

cd polly-word-reader
mvn clean package
aws s3 cp target/polly-word-reader.jar s3://${POLLY_ASSETS_BUCKET}/assets/

echo "Successfully deployed lambda functions"

cd ../..

aws s3 cp cloudformation/polly-word-to-mp3.template.yaml s3://${POLLY_ASSETS_BUCKET}/cloudformation/

aws cloudformation --region ${REGION} create-stack --stack-name $3 \
  --template-url https://${POLLY_ASSETS_BUCKET}.s3.amazonaws.com/cloudformation/polly-word-to-mp3.template.yaml \
  --parameters  ParameterKey=ArtifactBucket,ParameterValue=${POLLY_ASSETS_BUCKET} \
  --capabilities "CAPABILITY_IAM"

aws cloudformation wait stack-create-complete --stack-name $STACK_NAME --region $REGION

echo "Successfully launched stack"

echo "CloudFormation outputs: "
PollyWordReaderFunctionARN=$(aws cloudformation --region $REGION describe-stacks --stack $STACK_NAME --output text | grep OUTPUTS | grep PollyWordReaderFunctionARN | cut -f4)
SSMLToMP3FunctionARN=$(aws cloudformation describe-stacks --region $REGION --stack $STACK_NAME --output text | grep OUTPUTS | grep SSMLToMP3FunctionARN | cut -f4)

echo "PollyWordReader Lambda function ARN: ${PollyWordReaderFunctionARN}"
echo "SSMLToMP3 Lambda function ARN: ${SSMLToMP3FunctionARN}"

echo "Updating event notification template"
cat scripts/bucket_lambda_notification.json | sed "s/SSML_MP3_FUNCTION_ARN_TOKEN/${SSMLToMP3FunctionARN}/g" | sed "s/POLLY_FUNCTION_ARN_TOKEN/${PollyWordReaderFunctionARN}/g" > scripts/bucket_lambda_notification_replaced.json

echo "Adding event notification"
bash scripts/add_bucket_notification.sh ${REGION} ${POLLY_ASSETS_BUCKET}