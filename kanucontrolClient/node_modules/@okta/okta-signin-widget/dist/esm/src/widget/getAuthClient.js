import { OktaAuth } from '@okta/okta-auth-js';
import Util from '../util/Util.js';
import config from '../config/config.json.js';

function getAuthClient (options = {}) {
  // if authClient is set, authParams are disregarded
  let {
    authClient: authClient,
    authParams: authParams
  } = options;

  if (!authClient) {
    // Create an authClient using widget options and optional authParams
    const {
      issuer: issuer,
      clientId: clientId,
      redirectUri: redirectUri,
      state: state,
      scopes: scopes,
      flow: flow,
      codeChallenge: codeChallenge,
      codeChallengeMethod: codeChallengeMethod,
      recoveryToken: recoveryToken
    } = options;
    authParams = {
      issuer: issuer,
      clientId: clientId,
      redirectUri: redirectUri,
      state: state,
      scopes: scopes,
      flow: flow,
      codeChallenge: codeChallenge,
      codeChallengeMethod: codeChallengeMethod,
      transformErrorXHR: Util.transformErrorXHR,
      recoveryToken: recoveryToken,
      ...authParams
    };

    if (!authParams.issuer) {
      authParams.issuer = options.baseUrl + '/oauth2/default';
    }

    authClient = new OktaAuth(authParams);
  } // Add widget version to extended user agent header


  if (!authClient._oktaUserAgent) ; else {
    authClient._oktaUserAgent.addEnvironment(`okta-signin-widget-${config.version}`);
  }

  return authClient;
}

export { getAuthClient as default };
//# sourceMappingURL=getAuthClient.js.map
