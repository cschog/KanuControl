import '../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import oktaUnderscore from '../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import { getMessage, getMessageKey } from './i18nTransformer.js';

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

const convertErrorMessageToErrorSummary = (formName, remediationValues = []) => {
  return oktaUnderscore.chain(remediationValues).filter(field => {
    var _field$messages;

    return Array.isArray((_field$messages = field.messages) === null || _field$messages === void 0 ? void 0 : _field$messages.value) && field.messages.value.length;
  }).map(field => {
    return {
      property: formName ? `${formName}.${field.name}` : field.name,
      errorSummary: field.messages.value.map(getMessage),
      errorKey: field.messages.value.map(getMessageKey)
    };
  }).value();
};
/**
 * Although time complexity is O(n^2),
 * the `array` is actually very small (size < 5),
 * hence performance doesn't matter.
 */


const uniqWith = (array, comparator) => {
  if (!Array.isArray(array)) {
    return [];
  }

  if (!oktaUnderscore.isFunction(comparator) || array.length === 1) {
    return array;
  }

  const result = [];

  for (let i = 0; i < array.length; i++) {
    let seen = false;

    for (let j = i + 1; j < array.length; j++) {
      /* eslint max-depth: [2, 3] */
      if (comparator(array[i], array[j])) {
        seen = true;
        break;
      }
    }

    if (!seen) {
      result.push(array[i]);
    }
  }

  return result;
};
/**
 * returns errors
 * @example
 * errors = [
 *   {property : fieldName1, errorSummary: [errorMessage1]},
 *   {property : fieldName2, errorSummary: [errorMessage2]}
 *   {property : fieldName3, errorSummary: [errorMessage31, errorMessage32]}
 * ]
 */


const getRemediationErrors = res => {
  let errors = [];

  if (!res.remediation || !Array.isArray(res.remediation.value) || res.remediation.value.length === 0) {
    return errors;
  }

  let remediationFormFields = res.remediation.value[0].value;

  if (!Array.isArray(remediationFormFields)) {
    return errors;
  } // error at field


  errors.push(convertErrorMessageToErrorSummary(null, remediationFormFields));

  oktaUnderscore.each(remediationFormFields, remediationForm => {
    var _remediationForm$form;

    const formName = remediationForm.name; // error at form.value

    if (Array.isArray((_remediationForm$form = remediationForm.form) === null || _remediationForm$form === void 0 ? void 0 : _remediationForm$form.value)) {
      errors.push(convertErrorMessageToErrorSummary(formName, remediationForm.form.value));
    } // error at option.value.form.value


    if (Array.isArray(remediationForm.options)) {
      oktaUnderscore.each(remediationForm.options, option => {
        var _option$value, _option$value$form;

        if (Array.isArray((_option$value = option.value) === null || _option$value === void 0 ? void 0 : (_option$value$form = _option$value.form) === null || _option$value$form === void 0 ? void 0 : _option$value$form.value)) {
          errors.push(convertErrorMessageToErrorSummary(formName, option.value.form.value));
        }
      });
    }
  }); // API may return identical error on same field
  // thus run through `uniqWith`.
  // Check unit test for details.


  return uniqWith(oktaUnderscore.flatten(errors), oktaUnderscore.isEqual);
};
/**
 * return global error summary combined into one string
 * allErrors = 'error string1. error string2'
 */


const getGlobalErrors = res => {
  var _res$messages;

  let allErrors = [];

  if (Array.isArray((_res$messages = res.messages) === null || _res$messages === void 0 ? void 0 : _res$messages.value)) {
    allErrors = res.messages.value.map(getMessage);
  }

  return allErrors.join('. ');
};
/**
 * return array of error keys
 */


const getGlobalErrorKeys = res => {
  var _res$messages2;

  let allKeys = [];

  if (Array.isArray((_res$messages2 = res.messages) === null || _res$messages2 === void 0 ? void 0 : _res$messages2.value)) {
    allKeys = res.messages.value.map(getMessageKey);
  }

  return allKeys;
};

const convertFormErrors = response => {
  let errors = {
    errorCauses: getRemediationErrors(response),
    errorSummary: getGlobalErrors(response),
    errorSummaryKeys: getGlobalErrorKeys(response),
    errorIntent: response.intent
  };
  return {
    responseJSON: errors
  };
};

const isIonErrorResponse = (response = {}) => {
  // check if error format is an ION response by looking for version attribute.
  // a little sloppy.
  return response.version;
};

const isIdxSessionExpiredError = response => {
  var _response$context, _response$context$mes, _response$context$mes2, _response$context$mes3;

  const errorI18NKey = response === null || response === void 0 ? void 0 : (_response$context = response.context) === null || _response$context === void 0 ? void 0 : (_response$context$mes = _response$context.messages) === null || _response$context$mes === void 0 ? void 0 : (_response$context$mes2 = _response$context$mes.value[0]) === null || _response$context$mes2 === void 0 ? void 0 : (_response$context$mes3 = _response$context$mes2.i18n) === null || _response$context$mes3 === void 0 ? void 0 : _response$context$mes3.key;
  return errorI18NKey && errorI18NKey === 'idx.session.expired';
};

var IonResponseHelper = {
  convertFormErrors: convertFormErrors,
  isIonErrorResponse: isIonErrorResponse,
  isIdxSessionExpiredError: isIdxSessionExpiredError
};

export { IonResponseHelper as default };
//# sourceMappingURL=IonResponseHelper.js.map
