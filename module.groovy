pipeline {
    agent any
    parameters {
        // Define a boolean parameter to control the destroy action
        booleanParam(name: 'DESTROY', defaultValue: false, description: 'Set to true to destroy the infrastructure')
        // Define a string parameter for the environment
        string(name: 'ENVIRONMENT', defaultValue: 'staging', description: 'The environment to apply or destroy (e.g., staging, production)')
    }
    stages {
        stage('Checkout') {
            steps {
                // Checkout the repository containing Terraform code
                git url: 'https://github.com/your-repo/terraform-project.git', branch: 'main'
            }
        }
        stage('Terraform Init') {
            steps {
                // Initialize Terraform in the specified environment directory
                dir("environments/${params.ENVIRONMENT}") {
                    sh 'terraform init'
                }
            }
        }
        stage('Terraform Plan') {
            when {
                // Execute this stage only if DESTROY is false
                expression { return !params.DESTROY }
            }
            steps {
                // Create a Terraform plan
                dir("environments/${params.ENVIRONMENT}") {
                    sh 'terraform plan -out=plan.tfplan'
                }
            }
        }
        stage('Terraform Apply') {
            when {
                // Execute this stage only if DESTROY is false
                expression { return !params.DESTROY }
            }
            steps {
                // Apply the Terraform plan
                dir("environments/${params.ENVIRONMENT}") {
                    sh 'terraform apply plan.tfplan'
                }
            }
        }
        stage('Terraform Destroy Plan') {
            when {
                // Execute this stage only if DESTROY is true
                expression { return params.DESTROY }
            }
            steps {
                // Create a Terraform destroy plan
                dir("environments/${params.ENVIRONMENT}") {
                    sh 'terraform plan -destroy -out=destroy.tfplan'
                }
            }
        }
        stage('Terraform Destroy Apply') {
            when {
                // Execute this stage only if DESTROY is true
                expression { return params.DESTROY }
            }
            steps {
                // Apply the Terraform destroy plan
                dir("environments/${params.ENVIRONMENT}") {
                    sh 'terraform apply destroy.tfplan'
                }
            }
        }
    }
    post {
        always {
            // Clean up after the build
            cleanWs()
        }
    }
}
