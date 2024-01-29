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
import oktaUnderscore from '../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import fn$1 from '../../util/FactorUtil.js';
import FormController from '../util/FormController.js';
import FormType from '../util/FormType.js';
import Util from '../../util/Util.js';
import fn from '../util/ValidationUtil.js';
import FooterSignout from '../views/shared/FooterSignout.js';
import PasswordRequirements from '../views/shared/PasswordRequirements.js';
import TextBox from '../views/shared/TextBox.js';

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
var PasswordResetController = FormController.extend({
  className: 'password-reset',
  Model: {
    props: {
      newPassword: ['string', true],
      confirmPassword: ['string', true]
    },
    validate: function () {
      return fn.validatePasswordMatch(this);
    },
    save: function () {
      this.trigger('save');
      const self = this;
      return this.doTransaction(function (transaction) {
        return transaction.resetPassword({
          newPassword: self.get('newPassword')
        });
      });
    }
  },
  Form: {
    save: oktaUnderscore.partial(loc, 'password.reset', 'login'),
    title: function () {
      return this.settings.get('brandName') ? loc('password.reset.title.specific', 'login', [this.settings.get('brandName')]) : loc('password.reset.title.generic', 'login');
    },
    subtitle: function () {
      const policy = this.options.appState.get('policy');

      if (!policy || this.settings.get('features.showPasswordRequirementsAsHtmlList')) {
        return;
      }

      return fn$1.getPasswordComplexityDescription(policy);
    },
    parseErrorMessage: function (responseJSON) {
      const policy = this.options.appState.get('policy');

      if (!!policy && this.settings.get('features.showPasswordRequirementsAsHtmlList')) {
        /*
          - This is a specific case where don't want to repeat the requirements again in the error message, since this
            is already shown in the description. The description as bullet-points itself should give an indication
            of the requirements.
          - We cannot check for error code this in this case, as the error code is shared between
            requirements not met message, common password message, etc. So error summary is the only differentiating
            factor. Replace the password requirements string with empty string in this case.
        */
        responseJSON = fn$1.removeRequirementsFromError(responseJSON, policy);
      }

      return responseJSON;
    },
    formChildren: function () {
      let children = [];

      if (this.settings.get('features.showPasswordRequirementsAsHtmlList')) {
        children.push(FormType.View({
          View: new PasswordRequirements({
            policy: this.options.appState.get('policy')
          })
        }));
      }

      children = children.concat([FormType.Input({
        className: 'margin-btm-5',
        label: loc('password.newPassword.placeholder', 'login'),
        'label-top': true,
        explain: Util.createInputExplain('password.newPassword.tooltip', 'password.newPassword.placeholder', 'login'),
        'explain-top': true,
        name: 'newPassword',
        input: TextBox,
        type: 'password',
        autoComplete: 'new-password'
      }), FormType.Input({
        label: loc('password.confirmPassword.placeholder', 'login'),
        'label-top': true,
        explain: Util.createInputExplain('password.confirmPassword.tooltip', 'password.confirmPassword.placeholder', 'login'),
        'explain-top': true,
        name: 'confirmPassword',
        input: TextBox,
        type: 'password',
        autoComplete: 'new-password'
      })]);
      return children;
    }
  },
  initialize: function () {
    this.listenTo(this.form, 'save', function () {
      const creds = {
        username: this.options.appState.get('userEmail'),
        password: this.model.get('newPassword')
      };
      this.settings.processCreds(creds).then(oktaUnderscore.bind(this.model.save, this.model));
    });

    if (!this.settings.get('features.hideBackToSignInForReset')) {
      this.addFooter(FooterSignout);
    }
  }
});

export { PasswordResetController as default };
//# sourceMappingURL=PasswordResetController.js.map
