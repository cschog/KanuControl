import { loc, internal } from '../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import fn from '../../util/FactorUtil.js';
import FormController from '../util/FormController.js';
import FormType from '../util/FormType.js';
import HtmlErrorMessageView from '../views/mfa-verify/HtmlErrorMessageView.js';
import FooterMFA from '../views/shared/FooterMFA.js';

/*!
 * Copyright (c) 2018-2019, Okta, Inc. and/or its affiliates. All rights reserved.
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
  Util: Util
} = internal.util;
var VerifyCustomFactorController = FormController.extend({
  className: 'verify-custom-factor custom-factor-form',
  Model: {
    props: {
      rememberDevice: 'boolean'
    },
    initialize: function () {
      const rememberDevice = fn.getRememberDeviceValue(this.appState); // set the initial value for remember device (Cannot do this while defining the
      // local property because this.settings would not be initialized there yet).

      this.set('rememberDevice', rememberDevice);

      if (this.settings.get('features.skipIdpFactorVerificationBtn') && !this.appState.get('lastFailedChallengeFactorData')) {
        this.set('provider', 'CUSTOM');
        this.set('factorType', 'claims_provider');
        this.save();
      }
    },
    save: function () {
      const rememberDevice = !!this.get('rememberDevice');
      return this.manageTransaction((transaction, setTransaction) => {
        const data = {
          rememberDevice: rememberDevice
        };

        const factor = oktaUnderscore.findWhere(transaction.factors, {
          provider: this.get('provider'),
          factorType: this.get('factorType')
        });

        return factor.verify(data).then(trans => {
          setTransaction(trans);
          const url = this.appState.get('verifyCustomFactorRedirectUrl');

          if (url !== null) {
            Util.redirect(url);
          }
        }).catch(function (err) {
          throw err;
        });
      });
    }
  },
  Form: function () {
    const factors = this.options.appState.get('factors');
    const factor = factors.findWhere({
      provider: this.options.provider,
      factorType: this.options.factorType
    });
    const vendorName = factor.get('vendorName');
    const saveText = loc('mfa.challenge.verify', 'login');
    const lastFailedChallengeFactorData = this.options.appState.get('lastFailedChallengeFactorData');
    let subtitle = loc('verify.customFactor.subtitle', 'login', [vendorName]);

    if (this.settings.get('features.skipIdpFactorVerificationBtn') && !lastFailedChallengeFactorData) {
      subtitle = loc('verify.customFactor.subtitle.redirect', 'login', [vendorName]);
      this.listenTo(this.model, 'error', () => {
        subtitle = loc('verify.customFactor.subtitle', 'login', [vendorName]);
        this.$('.o-form-explain').text(subtitle);
      });
    }

    return {
      autoSave: true,
      title: vendorName,
      save: saveText,
      subtitle: subtitle,
      attributes: {
        'data-se': 'factor-custom'
      },
      initialize: function () {
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
      },
      formChildren: function () {
        const result = [];
        const lastFailedChallengeFactorData = this.options.appState.get('lastFailedChallengeFactorData');

        if (this.settings.get('features.skipIdpFactorVerificationBtn') && !lastFailedChallengeFactorData) {
          result.push(FormType.View({
            View: '<div data-se="custom-factor-waiting" class="okta-waiting-spinner"></div>'
          }));
        }

        if (lastFailedChallengeFactorData) {
          result.push(FormType.View({
            View: new HtmlErrorMessageView({
              message: lastFailedChallengeFactorData.errorMessage
            })
          }, {
            selector: '.o-form-error-container'
          }));
        }

        return result;
      }
    };
  },
  postRender: function () {
    if (this.settings.get('features.skipIdpFactorVerificationBtn') && !this.options.appState.get('lastFailedChallengeFactorData')) {
      this.$('.o-form-button-bar').hide();
      this.$('.okta-waiting-spinner').show();
    }
  },
  trapAuthResponse: function () {
    if (this.options.appState.get('isMfaChallenge')) {
      return true;
    }
  },
  back: function () {// Empty function on verify controllers to prevent users
    // from navigating back during 'verify' using the browser's
    // back button. The URL will still change, but the view will not
    // More details in OKTA-135060.
  },
  initialize: function () {
    this.model.set('provider', this.options.provider);
    this.model.set('factorType', this.options.factorType);

    if (this.settings.get('features.skipIdpFactorVerificationBtn')) {
      this.listenTo(this.model, 'error', () => {
        this.$('.okta-waiting-spinner').hide();
        this.$('.o-form-button-bar').show();
      });
    }

    this.addFooter(FooterMFA);
  }
});

export { VerifyCustomFactorController as default };
//# sourceMappingURL=VerifyCustomFactorController.js.map
