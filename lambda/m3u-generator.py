import json
import re
import boto3
import os

s3client = boto3.client('s3')
dynamodb = boto3.resource('dynamodb')
pollyMetadataTable = os.environ['pollyMetadataTable']

pattern = r"([a-zA-Z_/-]*)([\d]*)([a-zA-Z_-]*)(^$|[\d]*)([a-zA-Z_-]*)(^$|[\d]*).*"

def convertToInt(item_num):
    x = int(item_num) if len(item_num) > 0 else 0
    return x

def sortKey(file_name):
    match_resp = re.findall(pattern, file_name)[0]
    section_num = convertToInt(match_resp[1])
    topic_num = convertToInt(match_resp[3])
    para_num = convertToInt(match_resp[5])
    return section_num * 100000 + topic_num * 1000 + para_num


def findTaskId(file_name, items):
    srch_items = [x['TaskId'] for x in items if x['FileName'] is file_name]
    return srch_items[0]


def mp3_file(s3_key, items):
    file_name = s3_key.split('/')[-1]
    return file_name.replace('txt', findTaskId(s3_key, items) + '.mp3')


def lambda_handler(event, context):
    table = dynamodb.Table(pollyMetadataTable)
    response = table.scan()
    print(json.dumps(response))
    print("Event" + str(event))

    items = response['Items']
    filenames = [x['FileName'] for x in items]
    taskIds = [x['TaskId'] for x in items]

    sf = sorted(filenames, key = sortKey)
    mp3_list = [mp3_file(l, items) for l in sf]
    m3u_str = '\n'.join(mp3_list)

    s3Bucket = event['s3Bucket']
    m3uFile = event['m3uFile']

    s3client.put_object(Body=m3u_str.encode('utf-8'), Bucket=s3Bucket, Key=m3uFile)

    return {
        'statusCode': 200,
        'body': json.dumps('Playlist file stored successfully!')
    }
