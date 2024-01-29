import '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import { AUTHENTICATOR_KEY } from '../RemediationConstants.js';

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
/**
 * Example of the option like
 * @param {AuthenticatorOption[]} options
 * @param {( AuthenticatorEnrollment[] || Authenticator[] )} authenticators
 */

function createOVOptions(options = []) {
  var _ovItem$value, _ovItem$value$form, _ovItem$value$form$va;

  // Split OV into individual entries for verification (one for each method).
  const ovItem = options.find(option => option.relatesTo.key === AUTHENTICATOR_KEY.OV);
  const methodTypeObj = ovItem === null || ovItem === void 0 ? void 0 : (_ovItem$value = ovItem.value) === null || _ovItem$value === void 0 ? void 0 : (_ovItem$value$form = _ovItem$value.form) === null || _ovItem$value$form === void 0 ? void 0 : (_ovItem$value$form$va = _ovItem$value$form.value) === null || _ovItem$value$form$va === void 0 ? void 0 : _ovItem$value$form$va.find(v => v.name === 'methodType');
  const methodOptions = methodTypeObj === null || methodTypeObj === void 0 ? void 0 : methodTypeObj.options;
  let signedNonceOption;

  if (methodOptions) {
    const ovOptions = [];
    methodOptions.forEach(method => {
      // get value object from the ov item
      const value = [...ovItem.value.form.value]; // get index of the methodType object within the value object

      const methodTypeIndex = ovItem.value.form.value.findIndex(v => v.name === 'methodType'); // create a new methodType object that removes the options array and
      // has a value of the current method

      const newMethodTypeObj = Object.assign(oktaUnderscore.omit(methodTypeObj, 'options'), method); // set the methodType field to be required and immutable in our UI,
      // so it is always sent to the backend.

      newMethodTypeObj.required = true;
      newMethodTypeObj.mutable = false; // replace old methodType object with the new one

      value.splice(methodTypeIndex, 1, newMethodTypeObj); // return a new ov item for a specific method

      const newItem = Object.assign({}, ovItem, {
        label: method.label,
        value: {
          form: {
            value: value
          }
        }
      });

      if (method.value === 'signed_nonce') {
        signedNonceOption = newItem;
      } else {
        ovOptions.push(newItem);
      }
    });
    const ovIndex = options.findIndex(option => option.relatesTo.key === AUTHENTICATOR_KEY.OV);
    options.splice(ovIndex, 1, ...ovOptions); // ReArrange fastpass in options based on deviceKnown

    if (signedNonceOption) {
      ovItem.relatesTo.deviceKnown ? options.unshift(signedNonceOption) : options.push(signedNonceOption);
    }
  }
}

function createAuthenticatorOptions(options = []) {
  createOVOptions(options);
  return options.map(option => {
    var _option$value, _option$value$form, _option$relatesTo;

    const value = ((_option$value = option.value) === null || _option$value === void 0 ? void 0 : (_option$value$form = _option$value.form) === null || _option$value$form === void 0 ? void 0 : _option$value$form.value) || []; // Each authenticator option has list of ION field.
    // Currently we only support merely selecting one of options
    // rather than pop up another page to collect extra data
    // (in order to fill value for `mutable: true; value: null` fields).
    // The only reason of such design is to simplify widget implementation
    // but could subject to change in later releases.
    // Thus only surface up fields that are `required: true; mutable: false`
    // which implies it already has `value`.

    const valueObject = value.filter(v => v.required === true && v.mutable === false).reduce((init, v) => {
      return Object.assign(init, {
        [v.name]: v.value
      });
    }, {});
    return {
      label: option.label,
      value: valueObject,
      relatesTo: option.relatesTo,
      authenticatorKey: (_option$relatesTo = option.relatesTo) === null || _option$relatesTo === void 0 ? void 0 : _option$relatesTo.key
    };
  });
}

function getAuthenticatorsEnrollUiSchema({
  options: options
}) {
  return {
    type: 'authenticatorEnrollSelect',
    options: createAuthenticatorOptions(options)
  };
}

function getAuthenticatorsVerifyUiSchema({
  options: options
}) {
  return {
    type: 'authenticatorVerifySelect',
    options: createAuthenticatorOptions(options)
  };
}
/**
  * Create ui schema for ION field that has type 'object'.
  */


function createUiSchemaForObject(ionFormField, remediationForm, transformedResp, createUISchema, settings) {
  const uiSchema = {};

  if (ionFormField.name === 'authenticator' && (remediationForm.name === 'select-authenticator-authenticate' || remediationForm.name === 'select-authenticator-unlock-account')) {
    // 1. when `select-authenticator-authenticator`,
    // create customize type so that display authenticator list as selectable list
    Object.assign(uiSchema, getAuthenticatorsVerifyUiSchema(ionFormField));
  } else if (ionFormField.name === 'authenticator' && remediationForm.name === 'select-authenticator-enroll') {
    // 2. when `select-authenticator-enroll`,
    // create customize type so that display authenticator list as selectable list
    Object.assign(uiSchema, getAuthenticatorsEnrollUiSchema(ionFormField));
  } else if (Array.isArray(ionFormField.options)) {
    // 3. For other cases, create ui schema for each `option` in order to render
    // different view for each option.
    //
    // e.g. { "name": "credentials", "type": "object", options: [ {value: {form: value:[]} ]
    uiSchema.optionsUiSchemas = ionFormField.options.map(opt => {
      return createUISchema(transformedResp, {
        value: [{
          name: ionFormField.name,
          value: opt.value
        }]
      }, settings);
    });
    uiSchema.options = ionFormField.options.map((opt, index) => {
      return {
        label: opt.label,
        value: index
      };
    }); // assume (default to) use radio buttons to switch sub-schema.

    uiSchema.type = 'radio';
    uiSchema.value = '0';
    uiSchema.name = `sub_schema_local_${ionFormField.name}`;
  }

  return uiSchema;
}

export { createOVOptions, createUiSchemaForObject as default };
//# sourceMappingURL=ion-object-handler.js.map
