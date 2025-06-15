#!/bin/bash

# Create S3 bucket
awslocal s3 mb s3://hobby-images-bucket

# Set bucket policy to allow public access
awslocal s3api put-bucket-policy --bucket hobby-images-bucket --policy '{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::hobby-images-bucket/*"
    }
  ]
}'

echo "S3 bucket created and configured"