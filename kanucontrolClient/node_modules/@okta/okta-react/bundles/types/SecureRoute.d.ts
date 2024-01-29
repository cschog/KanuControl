import * as React from 'react';
import { OnAuthRequiredFunction } from './OktaContext';
import * as ReactRouterDom from 'react-router-dom';
declare const SecureRoute: React.FC<{
    onAuthRequired?: OnAuthRequiredFunction;
    errorComponent?: React.ComponentType<{
        error: Error;
    }>;
} & ReactRouterDom.RouteProps & React.HTMLAttributes<HTMLDivElement>>;
export default SecureRoute;
