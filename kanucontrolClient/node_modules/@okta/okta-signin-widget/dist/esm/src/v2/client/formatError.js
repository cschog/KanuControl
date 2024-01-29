import { loc } from '../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';

/*!
 * Copyright (c) 2021-Present, Okta, Inc. and/or its affiliates. All rights reserved.
 * The Okta software accompanied by this notice is provided pursuant to the Apache License, Version 2.0 (the "License.")
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 */
function isInvalidRecoveryTokenError(error) {
  // special case: error from interact when passing an (invalid) recovery token
  return (error === null || error === void 0 ? void 0 : error.error) === 'invalid_request' && error.error_description === 'The recovery token is invalid';
}
function formatInvalidRecoveryTokenError(error) {
  // This error comes from `oauth2/interact` so is not an IDX error.
  // simulate an IDX-JS error response
  const idxError = formatIDXError(error);
  const {
    details: details
  } = idxError;
  const messages = {
    type: 'array',
    value: [{
      message: error.error_description,
      i18n: {
        key: 'oie.invalid.recovery.token'
      },
      class: 'ERROR'
    }]
  };
  details.rawIdxState.messages = messages;
  details.context.messages = messages;
  return idxError;
}
function isOIENotEnabledError(error) {
  // special case: error from interact. `useInteractionCodeFlow` is true but the Org does not have OIE enabled
  // The response is not in IDX format. See playground/mocks/data/oauth2/error-feature-not-enabled.json
  return (error === null || error === void 0 ? void 0 : error.error) === 'access_denied' && error.error_description;
}
function formatOIENotEnabledError(error) {
  // This error comes from `oauth2/interact` so the error is in OAuth format
  // simulate an IDX-JS error response
  const idxError = formatIDXError(error);
  const {
    details: details
  } = idxError;
  const messages = {
    type: 'array',
    value: [{
      message: error.error_description,
      i18n: {
        key: 'oie.feature.disabled'
      },
      class: 'ERROR'
    }]
  };
  details.rawIdxState.messages = messages;
  details.context.messages = messages;
  return error;
}
function isOIEConfigurationError(error) {
  return (error === null || error === void 0 ? void 0 : error.error) && error.error_description;
}
function formatOIEConfigurationError(error) {
  // This error comes from `oauth2/interact` so the error is in OAuth format
  // simulate an IDX-JS error response
  const idxError = formatIDXError(error);
  const {
    details: details
  } = idxError;
  const messages = {
    type: 'array',
    value: [{
      message: loc('oie.configuration.error', 'login'),
      class: 'ERROR',
      i18n: undefined
    }]
  };
  details.rawIdxState.messages = messages;
  details.context.messages = messages;
  return error;
}
function formatIDXError(error) {
  // Make the error object resemble an IDX response
  const idxError = error;
  idxError.details = idxError.details || {};
  const {
    details: details
  } = idxError;
  details.rawIdxState = details.rawIdxState || {};
  details.context = details.context || {};
  details.neededToProceed = details.neededToProceed || []; // Populate generic error message if there isn't any.

  if (!details.rawIdxState.messages) {
    const idxMessage = {
      type: 'array',
      value: [{
        message: loc('oform.error.unexpected', 'login'),
        class: 'ERROR',
        i18n: undefined
      }]
    };
    details.rawIdxState.messages = idxMessage;
    details.context.messages = idxMessage;
  }

  return idxError;
}
function formatError(error) {
  // If the error is a string, wrap it in an Error object
  if (typeof error === 'string') {
    error = new Error(error);
  } // special case error handling
  // invalid reccovery token


  if (isInvalidRecoveryTokenError(error)) {
    return formatInvalidRecoveryTokenError(error);
  } // OIE is not enabled


  if (isOIENotEnabledError(error)) {
    return formatOIENotEnabledError(error);
  } // Other errors from /interact in OAuth format


  if (isOIEConfigurationError(error)) {
    return formatOIEConfigurationError(error);
  }

  error = formatIDXError(error);
  return error;
}

export { formatError, formatIDXError, formatInvalidRecoveryTokenError, formatOIEConfigurationError, formatOIENotEnabledError, isInvalidRecoveryTokenError, isOIEConfigurationError, isOIENotEnabledError };
//# sourceMappingURL=formatError.js.map
