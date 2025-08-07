#!/bin/bash

echo "Checking for AWS resources that might be incurring costs..."

# Check for EKS clusters
echo "=== EKS Clusters ==="
aws eks list-clusters --region ap-south-1

# Check for Elastic Beanstalk environments
echo "=== Elastic Beanstalk Environments ==="
aws elasticbeanstalk describe-environments --region ap-south-1

# Check for EC2 instances
echo "=== EC2 Instances ==="
aws ec2 describe-instances --region ap-south-1 --query 'Reservations[*].Instances[*].[InstanceId,State.Name,InstanceType]' --output table

# Check for Load Balancers
echo "=== Load Balancers ==="
aws elbv2 describe-load-balancers --region ap-south-1 --query 'LoadBalancers[*].[LoadBalancerName,State.Code]' --output table

# Check for CloudFormation stacks
echo "=== CloudFormation Stacks ==="
aws cloudformation list-stacks --region ap-south-1 --stack-status-filter CREATE_COMPLETE UPDATE_COMPLETE --query 'StackSummaries[*].[StackName,StackStatus]' --output table

echo ""
echo "To delete resources:"
echo "1. EKS Cluster: eksctl delete cluster --name CLUSTER_NAME --region ap-south-1"
echo "2. EB Environment: eb terminate ENVIRONMENT_NAME"
echo "3. CloudFormation: aws cloudformation delete-stack --stack-name STACK_NAME --region ap-south-1"