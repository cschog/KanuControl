import { ConfigError } from '../../util/Errors.js';
import { FORMS } from '../ion/RemediationConstants.js';
import { CONFIGURED_FLOW } from './constants.js';

/*!
 * Copyright (c) 2021, Okta, Inc. and/or its affiliates. All rights reserved.
 * The Okta software accompanied by this notice is provided pursuant to the Apache License, Version 2.0 (the "License.")
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 */
// if not available, returns original idxResponse (and logs warning)

async function proceedIfAvailable(idxState, remediation, flow) {
  const rem = idxState.neededToProceed.find(item => item.name === remediation);

  if (!rem) {
    return idxState;
  }

  try {
    const nextIdxState = await idxState.proceed(remediation);
    return nextIdxState;
  } catch (err) {
    // catches and handles `Unknown remediation` errors thrown okta-idx-js
    if (typeof err === 'string' && err.startsWith('Unknown remediation choice')) {
      return idxState;
    } else {
      // do not catch non-`Unknown remediation` errors here
      throw err;
    }
  }
} // attempts to "step into" a specific flow by calling `.proceed` with a specific remeidation (or calls an action)
// the "desired" remeidation is not guaranteed to be available, depends upon Org configurations


async function stepIntoSpecificIdxFlow(idxState, flow = 'default') {
  switch (flow) {
    case CONFIGURED_FLOW.DEFAULT:
    case CONFIGURED_FLOW.PROCEED:
    case CONFIGURED_FLOW.LOGIN:
      // default IDX response from interact is "Login" page/flow. Do nothing
      return idxState;
    // step logic is handled by auth-js

    case CONFIGURED_FLOW.REGISTRATION:
    case CONFIGURED_FLOW.RESET_PASSWORD:
      return idxState;

    case CONFIGURED_FLOW.UNLOCK_ACCOUNT:
      // requires: introspect -> identify-recovery -> select-authenticator-unlock-account
      return await proceedIfAvailable(idxState, FORMS.UNLOCK_ACCOUNT);

    default:
      throw new ConfigError('Invalid "flow" configuration');
  }
} // ensures the `flow` stored in the transaction meta matches the flow configuration
// if they do not match, abandon the current (meta) flow and start a new (configured) flow


async function handleConfiguredFlow(originalResp, settings) {
  const authClient = settings.getAuthClient();
  const configuredFlow = authClient.idx.getFlow(); // attempts to step into the desired flow

  const idxState = await stepIntoSpecificIdxFlow(originalResp, configuredFlow);
  return idxState;
}

export { handleConfiguredFlow };
//# sourceMappingURL=handleConfiguredFlow.js.map
