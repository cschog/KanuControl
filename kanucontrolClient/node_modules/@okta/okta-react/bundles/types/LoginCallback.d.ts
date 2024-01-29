import * as React from 'react';
import { OnAuthResumeFunction } from './OktaContext';
interface LoginCallbackProps {
    errorComponent?: React.ComponentType<{
        error: Error;
    }>;
    onAuthResume?: OnAuthResumeFunction;
    loadingElement?: React.ReactElement;
}
declare const LoginCallback: React.FC<LoginCallbackProps>;
export default LoginCallback;
