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
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import { HINTS, ATTR_FORMAT } from '../RemediationConstants.js';
import fn from '../../../util/CountryUtil.js';
import TimeZone from '../../view-builder/utils/TimeZone.js';

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

const ionOptionsToUiOptions = options => {
  const result = {};
  options.forEach(({
    value: value,
    label: label
  }) => {
    result[value] = label;
  });
  return result;
};

const getPasswordUiSchema = settings => ({
  type: 'password',
  params: {
    showPasswordToggle: settings.get('showPasswordToggle')
  }
});

const getCaptchaUiSchema = () => {
  return {
    type: 'captcha'
  };
};

const timezoneUISchema = {
  type: 'select',
  wide: true
};

const shouldRenderAsRadio = name => name.indexOf('methodType') >= 0 || name.indexOf('channel') >= 0;

const optionalType = ['text', 'radio', 'select'];

const populateUISchemaForDisplay = (uiSchema, ionField) => {
  var _ionField$value;

  const display = ionField === null || ionField === void 0 ? void 0 : (_ionField$value = ionField.value) === null || _ionField$value === void 0 ? void 0 : _ionField$value.value;
  uiSchema.type = display.inputType;

  if (display.inputType === 'radio') {
    uiSchema.options = display.options;
  } else if (display.inputType === 'select') {
    uiSchema.wide = true;

    if (display.format === ATTR_FORMAT.COUNTRY_CODE) {
      uiSchema.options = Object.assign({
        '': ''
      }, fn.getCountryCode());
    } else {
      //it will create a placeholder for dropdowns, by default it will show 'Select an Option'
      uiSchema.options = Object.assign({
        '': ''
      }, ionOptionsToUiOptions(display.options));
    }
  }
};

const populateUISchemaForRadio = (uiSchema, ionFormField) => {
  // e.g. { name: 'methodType', options: [ {label: 'sms'} ], type: 'string' | null }
  uiSchema.type = 'radio'; // set the default value to the first value.

  ionFormField.value = ionFormField.options[0].value;
};

const createUiSchemaForString = (ionFormField, remediationForm, transformedResp, createUISchema, settings) => {
  const uiSchema = {
    type: 'text'
  }; // e.g. { name: 'password', secret: true }

  if (ionFormField.secret === true) {
    Object.assign(uiSchema, getPasswordUiSchema(settings));
  }

  if (ionFormField.hint === HINTS.CAPTCHA) {
    Object.assign(uiSchema, getCaptchaUiSchema());
  }

  if (ionFormField.name === 'userProfile.timezone') {
    Object.assign(uiSchema, timezoneUISchema);
    uiSchema.options = Object.assign({
      '': ''
    }, TimeZone);
  }

  if (Array.isArray(ionFormField.options) && ionFormField.options[0] && ionFormField.options[0].value) {
    const ionField = ionFormField.options[0];

    if (ionField.label === 'display') {
      populateUISchemaForDisplay(uiSchema, ionField);
    } else if (shouldRenderAsRadio(ionFormField.name)) {
      populateUISchemaForRadio(uiSchema, ionFormField);
    } else {
      // default to select (dropdown). no particular reason (certainly can default to radio.)
      // e.g. { name: 'questionKey', options: [], type: 'string' | null }
      uiSchema.type = 'select';
      uiSchema.wide = true;
      uiSchema.options = ionOptionsToUiOptions(ionFormField.options);
    }
  } // set a label as 'Optional' for supported optional element types


  if (ionFormField.required === false && optionalType.includes(uiSchema.type)) {
    uiSchema.sublabel = loc('oie.form.field.optional', 'login');
  }

  return uiSchema;
};

export { createUiSchemaForString as default };
//# sourceMappingURL=ion-string-handler.js.map
