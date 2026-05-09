import * as cdk from 'aws-cdk-lib';
import { ProductServiceStack } from '../lib/cdk-stack';

const app = new cdk.App();
new ProductServiceStack(app, 'ProductServiceStack', {
    env: {
        account: process.env.CDK_DEFAULT_ACCOUNT,
        region: 'eu-north-1',
    },
});