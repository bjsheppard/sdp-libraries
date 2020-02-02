.. _Lambda_SAM:
------
Lambda SAM
------

The Lambda SAM library will deploy a Lambda application that has been defined in a SAM template.
It uses the Jenkins Environment Variables. The configuration uses the app environment first then the library configuration

Jenkins Environment Variables Used
==================================
- AWS_REGION

Steps Provided
==============

- call()

Example Configuration Snippet
=============================

.. code:: groovy
   application_environments{
     Dev{
       S3_BUCKET = "dev_s3_bucket"
       CF_STACK = "dev_cf_stack"
     }
     Prod{
       S3_BUCKET = "prod_s3_bucket"
       CF_STACK = "prod_cf_stack"
     }
   }
   libraries{
     aws {
       projectName = "aws"
       identifier = "app1"
     }
     lambda_sam
   }

Configurations
==============

.. csv-table::  Configuration Options
   :header: "Field", "Description", "Default Value", "Required"

   "S3_BUCKET", "the S3 Bucket used for storing CF deploy files", "", "true"
   "CF_STACK", "the CloudFormation Stack to deplpy to", "", "true"

External Dependencies
=====================

- the `aws` sdp library is needed
- requires AWS Parameter Store values
- A container registry must be set up and configured and contain an 'aws' image

Troubleshooting
===============

FAQ
===
