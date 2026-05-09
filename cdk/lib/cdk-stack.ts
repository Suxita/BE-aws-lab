import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as lambda from 'aws-cdk-lib/aws-lambda';
import * as apigateway from 'aws-cdk-lib/aws-apigateway';
import * as path from 'path';

export class ProductServiceStack extends cdk.Stack {
    constructor(scope: Construct, id: string, props?: cdk.StackProps) {
        super(scope, id, props);

        // Lambda: GET /products
        const getProductsList = new lambda.Function(this, 'getProductsList', {
            runtime: lambda.Runtime.JAVA_17,
            handler: 'com.shop.product.handler.GetProductsListHandler::handleRequest',
            code: lambda.Code.fromAsset(
                path.join(__dirname, '../../product_service/target/product-service-1.0-SNAPSHOT.jar')
            ),
            memorySize: 512,
            timeout: cdk.Duration.seconds(30),
        });

        // Lambda: GET /products/{productId}
        const getProductsById = new lambda.Function(this, 'getProductsById', {
            runtime: lambda.Runtime.JAVA_17,
            handler: 'com.shop.product.handler.GetProductsByIdHandler::handleRequest',
            code: lambda.Code.fromAsset(
                path.join(__dirname, '../../product_service/target/product-service-1.0-SNAPSHOT.jar')
            ),
            memorySize: 512,
            timeout: cdk.Duration.seconds(30),
        });

        // API Gateway
        const api = new apigateway.RestApi(this, 'ProductServiceApi', {
            restApiName: 'Product Service',
            defaultCorsPreflightOptions: {
                allowOrigins: apigateway.Cors.ALL_ORIGINS,
                allowMethods: apigateway.Cors.ALL_METHODS,
            },
        });

        // /products -> GET
        const productsResource = api.root.addResource('products');
        productsResource.addMethod(
            'GET',
            new apigateway.LambdaIntegration(getProductsList)
        );

        // /products/{productId} -> GET
        const productByIdResource = productsResource.addResource('{productId}');
        productByIdResource.addMethod(
            'GET',
            new apigateway.LambdaIntegration(getProductsById)
        );

        // Output the API URL
        new cdk.CfnOutput(this, 'ApiUrl', {
            value: api.url,
            description: 'Product Service API URL',
        });
    }
}