import { Form, loc } from '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import TextBox from '../shared/TextBox.js';

/*!
 * Copyright (c) 2015-2016, Okta, Inc. and/or its affiliates. All rights reserved.
 * The Okta software accompanied by this notice is provided pursuant to the Apache License, Version 2.0 (the "License.")
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 */
var TOTPForm = Form.extend({
  className: 'mfa-verify-totp',
  autoSave: true,
  noCancelButton: true,
  save: oktaUnderscore.partial(loc, 'mfa.challenge.verify', 'login'),
  scrollOnError: false,
  layout: 'o-form-theme',
  attributes: {
    'data-se': 'factor-totp'
  },
  initialize: function () {
    const factorName = this.model.get('factorLabel');
    const maskPasswordField = this.model.get('provider') === 'RSA' || this.model.get('provider') === 'DEL_OATH';
    this.title = factorName;

    if (this.model.get('isFactorTypeVerification')) {
      this.subtitle = loc('mfa.challenge.totp.subtitle.multiple', 'login', [factorName]);
    } else {
      this.subtitle = loc('mfa.challenge.title', 'login', [factorName]);
    }

    this.addInput({
      label: loc('mfa.challenge.enterCode.placeholder', 'login'),
      'label-top': true,
      className: 'o-form-fieldset o-form-label-top auth-passcode',
      name: 'answer',
      input: TextBox,
      type: maskPasswordField ? 'password' : 'tel'
    });

    if (this.options.appState.get('allowRememberDevice')) {
      this.addInput({
        label: false,
        'label-top': true,
        placeholder: this.options.appState.get('rememberDeviceLabel'),
        className: 'margin-btm-0',
        name: 'rememberDevice',
        type: 'checkbox'
      });
    }

    if (this.model.get('provider') === 'RSA' || this.model.get('provider') === 'DEL_OATH') {
      this.listenTo(this.model, 'error', (source, error) => {
        if (error && error.status === 409) {
          // 409 means we are in change pin, so we should clear out answer input
          this.$('.auth-passcode input').val('');
          this.$('.auth-passcode input').trigger('change');
          this.$('.auth-passcode input').focus();
        }
      });
    }
  }
});

export { TOTPForm as default };
//# sourceMappingURL=TOTPForm.js.map
