#! /usr/bin/env bash

REGION=$1
POLLY_ASSETS_BUCKET=$2

aws s3api put-bucket-notification-configuration --region ${REGION} --bucket ${POLLY_ASSETS_BUCKET} --notification-configuration file://scripts/bucket_lambda_notification_replaced.json