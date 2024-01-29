import { loc } from '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import oktaUnderscore from '../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import fn from '../../../util/FactorUtil.js';
import { AUTHENTICATOR_KEY } from '../../ion/RemediationConstants.js';

/*!
 * Copyright (c) 2020, Okta, Inc. and/or its affiliates. All rights reserved.
 * The Okta software accompanied by this notice is provided pursuant to the Apache License, Version 2.0 (the "License.")
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 */
const {
  getPasswordComplexityDescriptionForHtmlList: getPasswordComplexityDescriptionForHtmlList
} = fn;

const getButtonDataSeAttr = function (authenticator) {
  if (authenticator.authenticatorKey) {
    var _authenticator$value, _authenticator$value2;

    const method = (_authenticator$value = authenticator.value) !== null && _authenticator$value !== void 0 && _authenticator$value.methodType ? '-' + ((_authenticator$value2 = authenticator.value) === null || _authenticator$value2 === void 0 ? void 0 : _authenticator$value2.methodType) : '';
    return authenticator.authenticatorKey + method;
  }

  return '';
};
/* eslint complexity: [0, 0] */


const getAuthenticatorData = function (authenticator, isVerifyAuthenticator) {
  var _authenticator$relate, _authenticator$relate2;

  const authenticatorKey = authenticator.authenticatorKey;
  const key = oktaUnderscore.isString(authenticatorKey) ? authenticatorKey.toLowerCase() : '';
  let authenticatorData = {};

  switch (key) {
    case AUTHENTICATOR_KEY.EMAIL:
      Object.assign(authenticatorData, {
        description: isVerifyAuthenticator ? '' : loc('oie.email.authenticator.description', 'login'),
        iconClassName: 'mfa-okta-email',
        buttonDataSeAttr: getButtonDataSeAttr(authenticator)
      });
      break;

    case AUTHENTICATOR_KEY.PASSWORD:
      Object.assign(authenticatorData, {
        description: isVerifyAuthenticator ? '' : loc('oie.password.authenticator.description', 'login'),
        iconClassName: 'mfa-okta-password',
        buttonDataSeAttr: getButtonDataSeAttr(authenticator)
      });
      break;

    case AUTHENTICATOR_KEY.PHONE:
      Object.assign(authenticatorData, {
        description: isVerifyAuthenticator ? (_authenticator$relate = authenticator.relatesTo) === null || _authenticator$relate === void 0 ? void 0 : (_authenticator$relate2 = _authenticator$relate.profile) === null || _authenticator$relate2 === void 0 ? void 0 : _authenticator$relate2.phoneNumber : loc('oie.phone.authenticator.description', 'login'),
        iconClassName: 'mfa-okta-phone',
        noTranslateClassName: isVerifyAuthenticator ? 'no-translate' : '',
        buttonDataSeAttr: getButtonDataSeAttr(authenticator)
      });
      break;

    case AUTHENTICATOR_KEY.SECURITY_QUESTION:
      Object.assign(authenticatorData, {
        description: isVerifyAuthenticator ? '' : loc('oie.security.question.authenticator.description', 'login'),
        iconClassName: 'mfa-okta-security-question',
        buttonDataSeAttr: getButtonDataSeAttr(authenticator)
      });
      break;

    case AUTHENTICATOR_KEY.WEBAUTHN:
      Object.assign(authenticatorData, {
        description: isVerifyAuthenticator ? '' : loc('oie.webauthn.description', 'login'),
        iconClassName: 'mfa-webauthn',
        buttonDataSeAttr: getButtonDataSeAttr(authenticator)
      });
      break;

    case AUTHENTICATOR_KEY.OV:
      Object.assign(authenticatorData, {
        description: isVerifyAuthenticator ? loc('oie.okta_verify.label', 'login') : loc('oie.okta_verify.authenticator.description', 'login'),
        iconClassName: 'mfa-okta-verify',
        buttonDataSeAttr: getButtonDataSeAttr(authenticator)
      });
      break;

    case AUTHENTICATOR_KEY.GOOGLE_OTP:
      Object.assign(authenticatorData, {
        description: isVerifyAuthenticator ? '' : loc('oie.google_authenticator.authenticator.description', 'login'),
        iconClassName: 'mfa-google-auth',
        buttonDataSeAttr: getButtonDataSeAttr(authenticator)
      });
      break;

    case AUTHENTICATOR_KEY.ON_PREM:
      {
        var _authenticator$relate3;

        const vendorName = ((_authenticator$relate3 = authenticator.relatesTo) === null || _authenticator$relate3 === void 0 ? void 0 : _authenticator$relate3.displayName) || loc('oie.on_prem.authenticator.default.vendorName', 'login');
        Object.assign(authenticatorData, {
          description: isVerifyAuthenticator ? '' : loc('oie.on_prem.authenticator.description', 'login', [vendorName]),
          iconClassName: 'mfa-onprem',
          buttonDataSeAttr: getButtonDataSeAttr(authenticator)
        });
        break;
      }

    case AUTHENTICATOR_KEY.RSA:
      Object.assign(authenticatorData, {
        description: isVerifyAuthenticator ? '' : loc('oie.rsa.authenticator.description', 'login'),
        iconClassName: 'mfa-rsa',
        buttonDataSeAttr: getButtonDataSeAttr(authenticator)
      });
      break;

    case AUTHENTICATOR_KEY.DUO:
      Object.assign(authenticatorData, {
        description: isVerifyAuthenticator ? '' : loc('oie.duo.authenticator.description', 'login'),
        iconClassName: 'mfa-duo',
        buttonDataSeAttr: getButtonDataSeAttr(authenticator)
      });
      break;

    case AUTHENTICATOR_KEY.IDP:
      {
        var _authenticator$relate4;

        const idpName = (_authenticator$relate4 = authenticator.relatesTo) === null || _authenticator$relate4 === void 0 ? void 0 : _authenticator$relate4.displayName;
        Object.assign(authenticatorData, {
          description: isVerifyAuthenticator ? '' : loc('oie.idp.authenticator.description', 'login', [idpName]),
          iconClassName: 'mfa-custom-factor',
          buttonDataSeAttr: getButtonDataSeAttr(authenticator)
        });
        break;
      }

    case AUTHENTICATOR_KEY.CUSTOM_OTP:
      {
        Object.assign(authenticatorData, {
          description: isVerifyAuthenticator ? '' : loc('oie.custom_otp.description', 'login'),
          iconClassName: 'mfa-hotp',
          buttonDataSeAttr: getButtonDataSeAttr(authenticator)
        });
        break;
      }

    case AUTHENTICATOR_KEY.SYMANTEC_VIP:
      {
        var _authenticator$relate5;

        const appName = (_authenticator$relate5 = authenticator.relatesTo) === null || _authenticator$relate5 === void 0 ? void 0 : _authenticator$relate5.displayName;
        Object.assign(authenticatorData, {
          description: isVerifyAuthenticator ? '' : loc('oie.symantecVip.authenticator.description', 'login', [appName]),
          iconClassName: 'mfa-symantec',
          buttonDataSeAttr: getButtonDataSeAttr(authenticator)
        });
        break;
      }

    case AUTHENTICATOR_KEY.YUBIKEY:
      {
        Object.assign(authenticatorData, {
          description: isVerifyAuthenticator ? '' : loc('oie.yubikey.authenticator.description', 'login'),
          iconClassName: 'mfa-yubikey',
          buttonDataSeAttr: getButtonDataSeAttr(authenticator)
        });
        break;
      }

    case AUTHENTICATOR_KEY.CUSTOM_APP:
      {
        var _authenticator$relate6, _authenticator$relate7;

        Object.assign(authenticatorData, {
          description: isVerifyAuthenticator ? authenticator === null || authenticator === void 0 ? void 0 : (_authenticator$relate6 = authenticator.relatesTo) === null || _authenticator$relate6 === void 0 ? void 0 : _authenticator$relate6.displayName : loc('oie.custom.app.authenticator.description', 'login', [authenticator.label]),
          noTranslateClassName: isVerifyAuthenticator ? 'no-translate' : '',
          buttonDataSeAttr: getButtonDataSeAttr(authenticator),
          iconClassName: 'mfa-custom-app-logo',
          logoUri: (authenticator === null || authenticator === void 0 ? void 0 : (_authenticator$relate7 = authenticator.relatesTo) === null || _authenticator$relate7 === void 0 ? void 0 : _authenticator$relate7.logoUri) || ''
        });
        break;
      }
  }

  return authenticatorData;
};

function getAuthenticatorDataForEnroll(authenticator) {
  return getAuthenticatorData(authenticator);
}
function getAuthenticatorDataForVerification(authenticator) {
  return getAuthenticatorData(authenticator, true);
}
function getIconClassNameForBeacon(authenticatorKey) {
  return getAuthenticatorData({
    authenticatorKey: authenticatorKey
  }).iconClassName;
}
function removeRequirementsFromError(errorJSON) {
  var _errorJSON$errorCause;

  if (((_errorJSON$errorCause = errorJSON.errorCauses) === null || _errorJSON$errorCause === void 0 ? void 0 : _errorJSON$errorCause.length) > 0 && Array.isArray(errorJSON.errorCauses[0].errorSummary) && errorJSON.errorCauses[0].errorSummary.length > 0) {
    var _errorJSON$errorCause2;

    // Change from Array to string for all errors.
    errorJSON.errorCauses[0].errorSummary = errorJSON.errorCauses[0].errorSummary[0]; // Overrides for particular error messages.

    const errorKey = ((_errorJSON$errorCause2 = errorJSON.errorCauses[0].errorKey) === null || _errorJSON$errorCause2 === void 0 ? void 0 : _errorJSON$errorCause2.length) > 0 && errorJSON.errorCauses[0].errorKey[0]; // Remove the requirements string only if this is requirements were not met error.

    if (errorKey === 'password.passwordRequirementsNotMet') {
      errorJSON.errorCauses[0].errorSummary = loc('registration.error.password.passwordRequirementsNotMet', 'login');
    }
  }

  return errorJSON;
}
/**
 * Get authenticator display name from {@code remediation}.
 *
 * @param {Object} remediation
 */

function getAuthenticatorDisplayName(remediation) {
  var _remediation$relatesT, _remediation$relatesT2;

  return (_remediation$relatesT = remediation.relatesTo) === null || _remediation$relatesT === void 0 ? void 0 : (_remediation$relatesT2 = _remediation$relatesT.value) === null || _remediation$relatesT2 === void 0 ? void 0 : _remediation$relatesT2.displayName;
} // Re-export function from FactorUtil

export { getAuthenticatorDataForEnroll, getAuthenticatorDataForVerification, getAuthenticatorDisplayName, getIconClassNameForBeacon, getPasswordComplexityDescriptionForHtmlList, removeRequirementsFromError };
//# sourceMappingURL=AuthenticatorUtil.js.map
