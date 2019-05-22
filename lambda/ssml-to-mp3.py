import json
import os
import boto3

s3client = boto3.client('s3')
polly_client = boto3.client('polly')
dynamodb = boto3.resource('dynamodb')

pollyMetadataTable = os.environ['pollyMetadataTable']


def item_exists(table, file_name):
    try:
        response = table.get_item(
            Key={
                'FileName': file_name
            }
        )
        item = response['Item']
        print("Item exists in DynamoDB: {}".format(json.dumps(response)))
    except Exception as e:
        print(e)
        response = None
    return response


def add_item(table, file_name, taskId):
    try:
        response = table.put_item(
            Item={
                'FileName': file_name,
                'TaskId': taskId
            }
        )
        print("Added item to DynamoDB: {}".format(json.dumps(response)))
    except Exception as e:
        print(e)
        response = None
    return response


def lambda_handler(event, context):
    print('received event: ' + str(event))
    bucketName = event['Records'][0]['s3']['bucket']['name']
    s3Key = event['Records'][0]['s3']['object']['key']
    print('received S3 Key: ' + str(s3Key))

    object_tags = s3client.get_object_tagging(
        Bucket=bucketName,
        Key=s3Key
    )

    # Code below will break if there are multiple tags added to S3 object
    #voiceId = object_tags['TagSet'][0]['Value'])
    try:
        voiceId = object_tags['TagSet'][0]['Value']
    except:
        voiceId = 'Brian'

    mp3Prefix = s3Key.replace('ssml','mp3').replace('.txt','')

    s3obj = s3client.get_object(Bucket = bucketName, Key = s3Key)

    s3Text = s3obj['Body'].read().decode('utf-8')

    table = dynamodb.Table(pollyMetadataTable)

    ddb_check = item_exists(table, s3Key)

    if ddb_check is None:
        print("Item not present in DynamoDB, start polly task")
        response = polly_client.start_speech_synthesis_task(VoiceId=voiceId,
                                                            OutputS3BucketName=bucketName,
                                                            OutputS3KeyPrefix=mp3Prefix,
                                                            OutputFormat='mp3',
                                                            TextType = 'ssml',
                                                            Text = s3Text)

        taskId = response['SynthesisTask']['TaskId']
        add_item(table, s3Key, taskId)
        print("Task id is {} ".format(taskId))
        task_status = polly_client.get_speech_synthesis_task(TaskId = taskId)
        print(task_status)
    else:
        task_status = "Item already present in DynamoDB, delete to restart polly task"
        print(task_status)

    return {
        'statusCode': 200,
        'body': json.dumps(task_status)
    }

