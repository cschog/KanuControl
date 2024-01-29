import { FORMS } from '../RemediationConstants.js';
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

const getCheckboxUiSchema = ({
  label: label,
  type: type,
  required: required
}) => ({
  // For Remember Me checkbox, we need the label only on the right side of it.
  placeholder: label,
  label: false,
  // Separating prop type for Backbone.Model
  // from html input type
  modelType: type,
  // uiSchema type is the html input type desired.
  type: 'checkbox',
  required: required || false
});

const getRadioUiSchema = ({
  label: label,
  required: required,
  options: options
}) => ({
  displayName: label,
  type: 'radio',
  required: required,
  options: options[0].value.value.options,
  sublabel: required ? null : loc('oie.form.field.optional', 'login')
});

const getCheckboxUiSchemaWithDefaultValue = ({
  label: label,
  type: type
}) => ({
  placeholder: label,
  label: false,
  modelType: type,
  type: 'checkbox',
  // set required true so default value can be passed to optional attributes as well
  required: true,
  value: false
});

const createUiSchemaForBoolean = (ionFormField, remediationForm) => {
  var _ionFormField$options, _ionFormField$options2, _ionFormField$options3, _ionFormField$options4, _ionFormField$options5, _ionFormField$options6;

  if ([FORMS.CONSENT_ENDUSER, FORMS.CONSENT_ADMIN].includes(remediationForm.name)) {
    const scopes = remediationForm.scopes.map(({
      name: name,
      label: label,
      desc: desc
    }) => {
      return {
        name: name,
        displayName: label,
        description: desc
      };
    }); // setting 'type' here to add a specific View in FormInputFactory.create

    const type = remediationForm.name;
    return {
      type: type,
      scopes: scopes,
      options: ionFormField.options
    };
  } else if (Array.isArray(ionFormField.options) && ((_ionFormField$options = ionFormField.options[0]) === null || _ionFormField$options === void 0 ? void 0 : (_ionFormField$options2 = _ionFormField$options.value) === null || _ionFormField$options2 === void 0 ? void 0 : (_ionFormField$options3 = _ionFormField$options2.value) === null || _ionFormField$options3 === void 0 ? void 0 : _ionFormField$options3.inputType) === 'radio') {
    return getRadioUiSchema(ionFormField);
  } else if (Array.isArray(ionFormField.options) && ((_ionFormField$options4 = ionFormField.options[0]) === null || _ionFormField$options4 === void 0 ? void 0 : (_ionFormField$options5 = _ionFormField$options4.value) === null || _ionFormField$options5 === void 0 ? void 0 : (_ionFormField$options6 = _ionFormField$options5.value) === null || _ionFormField$options6 === void 0 ? void 0 : _ionFormField$options6.inputType) === 'checkbox') {
    return getCheckboxUiSchemaWithDefaultValue(ionFormField);
  } else {
    return getCheckboxUiSchema(ionFormField);
  }
};

export { createUiSchemaForBoolean as default };
//# sourceMappingURL=ion-boolean-handler.js.map
