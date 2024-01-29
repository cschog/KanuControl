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
import oktaJQueryStatic from '../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import oktaUnderscore from '../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import Bundles from '../../util/Bundles.js';
import { getAuthenticatorDisplayName } from '../view-builder/utils/AuthenticatorUtil.js';
import { AUTHENTICATOR_KEY, FORMS } from './RemediationConstants.js';
import { I18N_BASE_ATTRIBUTE_ENROLL_PROFILE_MAPPINGS } from '../view-builder/views/enroll-profile/i18nBaseAttributeMappings.js';

/* eslint-disable max-len */
const WEBAUTHN_API_GENERIC_ERROR_KEY = 'authfactor.webauthn.error';
const SECURITY_QUESTION_PREFIXES = ['enroll-authenticator.security_question.credentials.questionKey.', 'challenge-authenticator.security_question.credentials.questionKey.'];
const I18N_OVERRIDE_MAPPINGS = {
  'identify.identifier': 'primaryauth.username.placeholder',
  'select-authenticator-unlock-account.identifier': 'primaryauth.username.placeholder',
  'identify.credentials.passcode': 'primaryauth.password.placeholder',
  'identify.rememberMe': 'oie.remember',
  'enroll-profile.userProfile.rememberMe': 'oie.remember',
  'identify-recovery.identifier': 'password.forgot.email.or.username.placeholder',
  'select-authenticator-enroll.authenticator.duo': 'factor.duo',
  'select-authenticator-enroll.authenticator.google_otp': 'oie.google_authenticator.label',
  'select-authenticator-enroll.authenticator.okta_email': 'oie.email.label',
  'select-authenticator-enroll.authenticator.okta_password': 'oie.password.label',
  'select-authenticator-enroll.authenticator.okta_verify': 'oie.okta_verify.label',
  'select-authenticator-enroll.authenticator.phone_number': 'oie.phone.label',
  'select-authenticator-enroll.authenticator.rsa_token': 'factor.totpHard.rsaSecurId',
  'select-authenticator-enroll.authenticator.security_question': 'oie.security.question.label',
  'select-authenticator-enroll.authenticator.symantec_vip': 'factor.totpHard.symantecVip',
  'select-authenticator-enroll.authenticator.webauthn': 'oie.webauthn.label',
  'select-authenticator-enroll.authenticator.yubikey_token': 'oie.yubikey.label',
  'select-authenticator-authenticate.authenticator.duo': 'factor.duo',
  'select-authenticator-authenticate.authenticator.google_otp': 'oie.google_authenticator.label',
  'select-authenticator-authenticate.authenticator.okta_email': 'oie.email.label',
  'select-authenticator-authenticate.authenticator.okta_password': 'oie.password.label',
  'select-authenticator-authenticate.authenticator.okta_verify.push': 'oie.okta_verify.push.title',
  'select-authenticator-authenticate.authenticator.okta_verify.signed_nonce': 'oie.okta_verify.signed_nonce.label',
  'select-authenticator-authenticate.authenticator.okta_verify.totp': 'oie.okta_verify.totp.title',
  'select-authenticator-authenticate.authenticator.phone_number': 'oie.phone.label',
  'select-authenticator-authenticate.authenticator.rsa_token': 'factor.totpHard.rsaSecurId',
  'select-authenticator-authenticate.authenticator.security_question': 'oie.security.question.label',
  'select-authenticator-authenticate.authenticator.symantec_vip': 'factor.totpHard.symantecVip',
  'select-authenticator-authenticate.authenticator.webauthn': 'oie.webauthn.label',
  'select-authenticator-authenticate.authenticator.yubikey_token': 'oie.yubikey.label',
  'select-authenticator-authenticate.authenticator.custom_app': 'oie.custom.app.authenticator.title',
  'select-authenticator-unlock-account.authenticator.okta_email': 'oie.email.label',
  'select-authenticator-unlock-account.authenticator.phone_number': 'oie.phone.label',
  'select-authenticator-unlock-account.authenticator.okta_verify.push': 'oie.okta_verify.push.title',
  'authenticator-verification-data.okta_verify.authenticator.methodType.signed_nonce': 'oie.okta_verify.signed_nonce.label',
  'authenticator-verification-data.okta_verify.authenticator.methodType.push': 'oie.okta_verify.push.title',
  'authenticator-verification-data.okta_verify.authenticator.methodType.totp': 'oie.okta_verify.totp.title',
  'authenticator-enrollment-data.phone_number.authenticator.phoneNumber': 'mfa.phoneNumber.placeholder',
  'authenticator-enrollment-data.phone_number.authenticator.methodType.sms': 'oie.phone.enroll.sms.label',
  'authenticator-enrollment-data.phone_number.authenticator.methodType.voice': 'oie.phone.enroll.voice.label',
  'enroll-authenticator.okta_password.credentials.passcode': 'oie.password.passwordLabel',
  'enroll-authenticator.okta_email.credentials.passcode': 'mfa.challenge.enterCode.placeholder',
  'enroll-authenticator.phone_number.credentials.passcode': 'mfa.challenge.enterCode.placeholder',
  'enroll-authenticator.security_question.sub_schema_local_credentials.0': 'oie.security.question.questionKey.label',
  'enroll-authenticator.security_question.sub_schema_local_credentials.1': 'oie.security.question.createQuestion.label',
  'enroll-authenticator.security_question.credentials.answer': 'mfa.challenge.answer.placeholder',
  'enroll-authenticator.security_question.credentials.question': 'oie.security.question.createQuestion.label',
  'enroll-authenticator.security_question.credentials.questionKey': 'oie.security.question.questionKey.label',
  'enroll-authenticator.google_otp.credentials.passcode': 'oie.google_authenticator.otp.enterCodeText',
  'enroll-authenticator.onprem_mfa.credentials.clientData': 'enroll.onprem.username.placeholder',
  'enroll-authenticator.onprem_mfa.credentials.passcode': 'enroll.onprem.passcode.placeholder',
  'enroll-authenticator.rsa_token.credentials.clientData': 'enroll.onprem.username.placeholder',
  'enroll-authenticator.rsa_token.credentials.passcode': 'enroll.onprem.passcode.placeholder',
  'enroll-authenticator.symantec_vip.credentials.credentialId': 'enroll.symantecVip.credentialId.placeholder',
  'enroll-authenticator.symantec_vip.credentials.passcode': 'enroll.symantecVip.passcode1.placeholder',
  'enroll-authenticator.symantec_vip.credentials.nextPasscode': 'enroll.symantecVip.passcode2.placeholder',
  'enroll-authenticator.yubikey_token.credentials.passcode': 'oie.yubikey.passcode.label',
  'enrollment-channel-data.email': 'oie.enroll.okta_verify.channel.email.label',
  'select-enrollment-channel.authenticator.channel.qrcode': 'oie.enroll.okta_verify.select.channel.qrcode.label',
  'select-enrollment-channel.authenticator.channel.email': 'oie.enroll.okta_verify.select.channel.email.label',
  'select-enrollment-channel.authenticator.channel.sms': 'oie.enroll.okta_verify.select.channel.sms.label',
  'challenge-authenticator.okta_email.credentials.passcode': 'mfa.challenge.enterCode.placeholder',
  'challenge-authenticator.okta_password.credentials.passcode': 'mfa.challenge.password.placeholder',
  'challenge-authenticator.phone_number.credentials.passcode': 'mfa.challenge.enterCode.placeholder',
  'challenge-authenticator.security_question.credentials.answer': 'mfa.challenge.answer.placeholder',
  'challenge-authenticator.okta_verify.credentials.totp': 'oie.okta_verify.totp.enterCodeText',
  'challenge-authenticator.google_otp.credentials.passcode': 'oie.google_authenticator.otp.enterCodeText',
  'challenge-authenticator.onprem_mfa.credentials.passcode': 'mfa.challenge.enterCode.placeholder',
  'challenge-authenticator.rsa_token.credentials.passcode': 'mfa.challenge.enterCode.placeholder',
  'challenge-authenticator.custom_otp.credentials.passcode': 'oie.custom_otp.verify.passcode.label',
  'challenge-authenticator.symantec_vip.credentials.passcode': 'oie.symantecVip.verify.passcode.label',
  'challenge-authenticator.yubikey_token.credentials.passcode': 'oie.yubikey.passcode.label',
  'challenge-authenticator.credentials.passcode': 'oie.password.label',
  'reset-authenticator.okta_password.credentials.passcode': 'oie.password.newPasswordLabel',
  'reenroll-authenticator.okta_password.credentials.passcode': 'oie.password.newPasswordLabel',
  'reenroll-authenticator-warning.okta_password.credentials.passcode': 'oie.password.newPasswordLabel',
  'incorrectPassword': 'oie.password.incorrect.message',
  'profile-update.userProfile.secondEmail': 'oie.user.profile.secondary.email',
  'user-code.userCode': 'device.code.activate.label',
  // Remap authn API errors to OIE
  'api.authn.poll.error.push_rejected': 'oktaverify.rejected',
  // Remap duo API errors to OIE
  'oie.authenticator.duo.method.duo.verification_timeout': 'oie.authenticator.duo.error',
  'oie.authenticator.duo.method.duo.verification_failed': 'oie.authenticator.duo.error',
  'idx.email.verification.required': 'registration.complete.confirm.text',
  'tooManyRequests': 'oie.tooManyRequests',
  'api.users.auth.error.POST_PASSWORD_UPDATE_AUTH_FAILURE': 'oie.post.password.update.auth.failure.error',
  'security.access_denied': 'errors.E0000006',
  'E0000009': 'errors.E0000009',
  'api.factors.error.sms.invalid_phone': 'oie.phone.invalid',
  'app.ldap.password.reset.failed': 'errors.E0000017',
  'oie.selfservice.unlock_user.challenge.failed.permissions': 'errors.E0000006',
  'core.auth.factor.signedNonce.error.invalidEnrollment': 'core.auth.factor.signedNonce.error',
  'core.auth.factor.signedNonce.error.invalidFactor': 'core.auth.factor.signedNonce.error',
  'core.auth.factor.signedNonce.error.deletedDevice': 'core.auth.factor.signedNonce.error',
  'core.auth.factor.signedNonce.error.invalidDeviceStatus': 'core.auth.factor.signedNonce.error.invalidDevice',
  // re-map autoPush: "Send push automatically"
  'challenge-poll.custom_app.autoChallenge': 'autoPush',
  // authenticator-verification-custom-app-push-autochallenge
  'challenge-poll.okta_verify.autoChallenge': 'autoPush',
  // authenticator-verification-okta-verify-push-autoChallenge-on
  'authenticator-verification-data.authenticator.autoChallenge': 'autoPush',
  // authenticator-verification-data-okta-verify-push-autoChallenge-off.json
  'authenticator-verification-data.okta_verify.authenticator.autoChallenge': 'autoPush',
  'authenticator-verification-data.custom_app.authenticator.autoChallenge': 'autoPush',
  // Existing overrides
  ...I18N_BASE_ATTRIBUTE_ENROLL_PROFILE_MAPPINGS //enroll-profile strings

};
const I18N_PARAMS_MAPPING = {
  [FORMS.ENROLL_AUTHENTICATOR]: {
    [AUTHENTICATOR_KEY.ON_PREM]: {
      getParam: getAuthenticatorDisplayName
    },
    [AUTHENTICATOR_KEY.RSA]: {
      getParam: getAuthenticatorDisplayName
    }
  }
};
/**
 * For i18n keys that require string interpolation using values from "params".
 * {baseKey} : {params}
 */

const I18N_OVERRIDE_WITH_PARAMS_MAP = {
  'registration.error.invalidLoginEmail': {
    Email: 'Email'
  },
  'registration.error.doesNotMatchPattern': {
    Email: 'Email'
  },
  'registration.error.notUniqueWithinOrg': {
    Email: 'Email'
  }
};
/**
 * For messages that need to be interpolated with param values.
 *
 * Enumerate each possible param interpolation and hardcode that into properties file.
 * This is to ensure proper translation.
 *
 * Example - a known param:
 *
 * input =
 *  "i18n": {
      "key": "registration.error.doesNotMatchPattern",
      "params": [
        "Email"
      ]
    }
 * output = registration.error.doesNotMatchPattern.Email
 *
 * Example - an unknown param:
 *
 * input =
 *  "i18n": {
      "key": "registration.error.doesNotMatchPattern",
      "params": [
        "Custom Property"
      ]
    }
 * output = registration.error.doesNotMatchPattern.custom
 *
 * @param {String} key
 * @param {String} param
 * @returns {String}
 */

const getI8nKeyUsingParams = (key, param) => {
  let i18nKey = key;

  if (I18N_OVERRIDE_WITH_PARAMS_MAP[i18nKey][param]) {
    i18nKey += `.${param}`;
  } else {
    i18nKey += '.custom';
  }

  return i18nKey;
};

const getI18NParams = (remediation, authenticatorKey) => {
  const params = [];
  const formName = remediation.name;

  if (I18N_PARAMS_MAPPING[formName] && I18N_PARAMS_MAPPING[formName][authenticatorKey]) {
    const config = I18N_PARAMS_MAPPING[formName][authenticatorKey];
    const param = config.getParam(remediation);
    params.push(param);
  }

  return params;
};

const getI18nKey = i18nPath => {
  let i18nKey; // Extract security question value from i18nPath

  SECURITY_QUESTION_PREFIXES.forEach(prefix => {
    if (i18nPath.indexOf(prefix) === 0) {
      const securityQuestionValue = i18nPath.replace(prefix, '');
      i18nKey = `security.${securityQuestionValue}`;
    }
  });

  if (I18N_OVERRIDE_MAPPINGS[i18nPath]) {
    i18nKey = I18N_OVERRIDE_MAPPINGS[i18nPath];
  }

  if (i18nKey && !Bundles.login[i18nKey]) {
    i18nKey = null;
  }

  return i18nKey;
};

const doesI18NKeyExist = i18nKey => {
  return !!Bundles.login[i18nKey];
};
/**
 * Find i18n value using {@code i18nPath} if it exists.
 * Otherwise return {@code defaultValue}.
 *
 * @param {string} i18nPath
 * @param {string} defaultValue
 * @param {string[]} params
 */


const getI18NValue = (i18nPath, defaultValue, params = []) => {
  const i18nKey = getI18nKey(i18nPath); // TODO : OKTA-397225
  // here defaultValue is uiSchema label or placeholders, some lables may be customized by 
  // admin to anything string. We should not localize and replace these customized labels even if i18nkey exists

  if (i18nKey) {
    return loc(i18nKey, 'login', params);
  } else {
    return defaultValue;
  }
};

const updateLabelForUiSchema = (remediation, uiSchema) => {
  var _remediation$relatesT, _remediation$relatesT2;

  if (uiSchema.mutable === false && uiSchema.name.indexOf('questionKey') < 0) {
    return;
  }
  const authenticatorKey = (_remediation$relatesT = remediation.relatesTo) === null || _remediation$relatesT === void 0 ? void 0 : (_remediation$relatesT2 = _remediation$relatesT.value) === null || _remediation$relatesT2 === void 0 ? void 0 : _remediation$relatesT2.key;
  const authenticatorKeyPath = authenticatorKey ? `.${remediation.relatesTo.value.key}` : '';
  const i18nPrefix = `${remediation.name}${authenticatorKeyPath}.`;
  let i18nPath = `${i18nPrefix}${uiSchema.name}`;

  if (uiSchema.type === 'text' && uiSchema.name.indexOf('questionKey') >= 0 && uiSchema.value !== 'custom') {
    i18nPath = `${i18nPath}.${uiSchema.value}`;
  }

  if (uiSchema.type === 'checkbox' && uiSchema.placeholder) {
    uiSchema.placeholder = getI18NValue(i18nPath, uiSchema.placeholder);
  }

  if (uiSchema.label) {
    const params = getI18NParams(remediation, authenticatorKey);
    uiSchema.label = getI18NValue(i18nPath, uiSchema.label, params);
  }

  if (oktaJQueryStatic.isPlainObject(uiSchema.options)) {
    uiSchema.options = oktaUnderscore.mapObject(uiSchema.options, (value, key) => {
      const i18nPathOption = `${i18nPath}.${key}`;
      return getI18NValue(i18nPathOption, value);
    });
  }

  if (Array.isArray(uiSchema.options)) {
    uiSchema.options.forEach(o => {
      if (!o.label) {
        return;
      }

      let i18nPathOption;

      if (o.authenticatorKey) {
        var _o$value;

        i18nPathOption = `${i18nPath}.${o.authenticatorKey}`;
        const methodType = (_o$value = o.value) === null || _o$value === void 0 ? void 0 : _o$value.methodType;

        if (o.authenticatorKey === AUTHENTICATOR_KEY.OV && methodType) {
          i18nPathOption = `${i18nPathOption}.${methodType}`;
        }
      } else if (typeof o.value === 'string' || typeof o.value === 'number') {
        // value could be string, number, object or undefined.
        i18nPathOption = `${i18nPath}.${o.value}`;
      } else {
        i18nPathOption = i18nPath;
      }
      o.label = getI18NValue(i18nPathOption, o.label);
    });
  }

  if (Array.isArray(uiSchema.optionsUiSchemas)) {
    uiSchema.optionsUiSchemas.forEach(optionsUiSchema => {
      optionsUiSchema.forEach(uiSchema => updateLabelForUiSchema(remediation, uiSchema));
    });
  }
};

const isWebAuthnAPIError = i18nKey => i18nKey.startsWith(WEBAUTHN_API_GENERIC_ERROR_KEY);
/**
 * @typedef {Object} Message
 * @property {string} message
 * @property {Object=} i18n
 * @property {string} i18n.key
 * @property {string[]} i18n.params
 */

/**
 * - If `message.i18n.key` exists and has a value in 'login.properties'
 *   through the given key or via I18N_OVERRIDE_MAPPINGS, return the value.
 *
 * - returns `message.message` otherwise
 *
 * @param {Message} message
 */


const getMessage = message => {
  var _message$i18n;

  if ((_message$i18n = message.i18n) !== null && _message$i18n !== void 0 && _message$i18n.key) {
    var _message$i18n2;

    let i18nKey = message.i18n.key;
    let i18nParams = message.i18n.params || []; // TODO - remove this block once API fix is done - OKTA-398080
    // Sometimes API sends params: [""] an array with empty string.
    // example - error-authenticator-enroll-password-common mock

    if (i18nParams.length === 1 && i18nParams[0] === '') {
      i18nParams = [];
    }

    if (I18N_OVERRIDE_MAPPINGS[(_message$i18n2 = message.i18n) === null || _message$i18n2 === void 0 ? void 0 : _message$i18n2.key]) {
      var _message$i18n3;

      i18nKey = I18N_OVERRIDE_MAPPINGS[(_message$i18n3 = message.i18n) === null || _message$i18n3 === void 0 ? void 0 : _message$i18n3.key];
    } else if (I18N_OVERRIDE_WITH_PARAMS_MAP[i18nKey]) {
      var _message$i18n$params;

      const param = (_message$i18n$params = message.i18n.params) === null || _message$i18n$params === void 0 ? void 0 : _message$i18n$params[0];
      i18nKey = getI8nKeyUsingParams(i18nKey, param);
      i18nParams = i18nKey.endsWith('custom') ? [param] : [];
    }

    if (Bundles.login[i18nKey]) { // expect user config i18n properly.
      // e.g. the i18n value shall have placeholders like `{0}`, when params is not empty.

      return loc(i18nKey, 'login', i18nParams);
    }

    if (isWebAuthnAPIError(i18nKey)) {
      // The WebAuthn api error doesn't make much sense to a typical end user, but useful for developer.
      // So keep the api message in response, but show a generic error message on UI.
      return loc(WEBAUTHN_API_GENERIC_ERROR_KEY, 'login');
    }
  }
  return message.message;
};
/**
 * @param {Object} error
 */


const getMessageFromBrowserError = error => {
  if (error.name) {
    const key = `oie.browser.error.${error.name}`;

    if (Bundles.login[key]) { // expect user config i18n properly.

      return loc(key, 'login');
    }
  }

  return error.message;
};
/**
 * - iff `message.i18n.key` exists return the key.
 *
 * @param {Message} message
 */


const getMessageKey = message => {
  var _message$i18n4;

  return (message === null || message === void 0 ? void 0 : (_message$i18n4 = message.i18n) === null || _message$i18n4 === void 0 ? void 0 : _message$i18n4.key) || '';
};

const uiSchemaLabelTransformer = transformedResp => {
  var _transformedResp$mess;

  // Try to override label using i18n value
  if (Array.isArray(transformedResp.remediations)) {
    transformedResp.remediations.filter(remediation => Array.isArray(remediation.uiSchema) && remediation.uiSchema.length).forEach(remediation => {
      remediation.uiSchema.forEach(uiSchema => updateLabelForUiSchema(remediation, uiSchema));
    });
  } // Try to override `messages` using i18n value.
  // 1. This is only handling top level `messages` object when response status is 200.
  // 2. See `IonResponseHelper.js` where handle `messages` object when none 200 response.
  // 3. Handling `messages` in remediation forms on 200 response is not considered yet.
  //    Is that possible?


  if (Array.isArray((_transformedResp$mess = transformedResp.messages) === null || _transformedResp$mess === void 0 ? void 0 : _transformedResp$mess.value)) {
    transformedResp.messages.value.forEach(message => {
      message.message = getMessage(message);
    });
  }

  return transformedResp;
};
/**
 * Has this i18n key been overridden for customization?
 * @param {String} i18nKey
 * @param {Object} settings
 * @returns Boolean
 */


const isCustomizedI18nKey = (i18nKey, settings) => {
  const language = settings.get('languageCode');
  const i18n = settings.get('i18n');
  const customizedProperty = i18n && i18n[language] && i18n[language][i18nKey];
  return !!customizedProperty;
};

export { uiSchemaLabelTransformer as default, doesI18NKeyExist, getI18NParams, getMessage, getMessageFromBrowserError, getMessageKey, isCustomizedI18nKey };
//# sourceMappingURL=i18nTransformer.js.map
