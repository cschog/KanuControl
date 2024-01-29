import * as React from 'react';
import { AuthState, OktaAuth } from '@okta/okta-auth-js';
export declare type OnAuthRequiredFunction = (oktaAuth: OktaAuth) => Promise<void> | void;
export declare type OnAuthResumeFunction = () => void;
export declare type RestoreOriginalUriFunction = (oktaAuth: OktaAuth, originalUri: string) => Promise<void> | void;
export interface IOktaContext {
    oktaAuth: OktaAuth;
    authState: AuthState | null;
    _onAuthRequired?: OnAuthRequiredFunction;
}
declare const OktaContext: React.Context<IOktaContext | null>;
export declare const useOktaAuth: () => IOktaContext;
export default OktaContext;
