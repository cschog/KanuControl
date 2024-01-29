import * as React from 'react';
import { IOktaContext } from './OktaContext';
declare const withOktaAuth: <P extends IOktaContext>(ComponentToWrap: React.ComponentType<P>) => React.FC<Omit<P, keyof IOktaContext>>;
export default withOktaAuth;
