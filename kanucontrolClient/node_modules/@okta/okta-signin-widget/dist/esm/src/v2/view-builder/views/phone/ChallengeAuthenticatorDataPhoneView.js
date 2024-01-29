import { loc } from '../../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import oktaUnderscore from '../../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import '../../internals/BaseHeader.js';
import '../../internals/BaseFooter.js';
import BaseForm from '../../internals/BaseForm.js';
import '../../internals/BaseFormWithPolling.js';
import '../../internals/BaseOktaVerifyChallengeView.js';
import BaseView from '../../internals/BaseView.js';
import '../../components/AuthenticatorEnrollOptions.js';
import '../../components/AuthenticatorVerifyOptions.js';
import '../../../../util/FactorUtil.js';
import '../../../../v1/views/admin-consent/ScopeList.js';
import '../../../../v1/views/consent/ScopeList.js';
import '../captcha/CaptchaView.js';
import BaseAuthenticatorView from '../../components/BaseAuthenticatorView.js';

const Body = BaseForm.extend({
  className: 'phone-authenticator-challenge',
  events: {
    'click a.phone-authenticator-challenge__link': 'handleSecondaryLinkClick'
  },
  title: function () {
    return loc('oie.phone.verify.title', 'login');
  },
  save: function () {
    return this.model.get('primaryMode') === 'sms' ? loc('oie.phone.sms.primaryButton', 'login') : loc('oie.phone.call.primaryButton', 'login');
  },
  handleSecondaryLinkClick: function (e) {
    e.preventDefault(); // Call the API to send a code via secondary mode

    const secondaryMode = this.model.get('secondaryMode');
    this.model.set('authenticator.methodType', secondaryMode);
    this.saveForm(this.model);
  },
  initialize: function () {
    // This is needed when user clicks on secondary (say voice) and call fails with ratelimit server-side error.
    // Then the user clicks primary button (say sms) and right methodType needs to be sent.
    this.model.on('error', () => this.model.set('authenticator.methodType', this.model.get('primaryMode')));
    BaseForm.prototype.initialize.apply(this, arguments);
    const sendText = this.model.get('primaryMode') === 'sms' ? loc('oie.phone.verify.sms.sendText', 'login') : loc('oie.phone.verify.call.sendText', 'login');
    const carrierChargesText = loc('oie.phone.carrier.charges', 'login');
    const extraCssClasses = this.model.get('phoneNumber') !== loc('oie.phone.alternate.title', 'login') ? 'strong no-translate' : ''; // Courage doesn't support HTML, hence creating a subtitle here.

    this.add(`<div class="okta-form-subtitle" data-se="o-form-explain">${sendText}
        <span ${extraCssClasses ? 'class="' + extraCssClasses + '"' : ''}>${this.model.escape('phoneNumber')}</span>
        <p>${carrierChargesText}</p>
      </div>`);
  },
  getUISchema: function () {
    // Change the UI schema to not display radios here.
    const uiSchemas = BaseForm.prototype.getUISchema.apply(this, arguments);
    return uiSchemas.filter(schema => schema.name !== 'authenticator.methodType');
  },
  render: function () {
    BaseForm.prototype.render.apply(this, arguments);
    const secondaryMode = this.model.get('secondaryMode');

    if (secondaryMode) {
      const secondaryButtonTitle = secondaryMode === 'sms' ? loc('oie.phone.sms.secondaryButton', 'login') : loc('oie.phone.call.secondaryButton', 'login');
      this.add(`<a href="#"
            class="link phone-authenticator-challenge__link"
            data-se="phone-authenticator-challenge__link">${secondaryButtonTitle}</a>`, '.o-form-button-bar');
    }
  }
});
var ChallengeAuthenticatorDataPhoneView = BaseAuthenticatorView.extend({
  Body: Body,
  createModelClass: function ({
    uiSchema: uiSchema
  }) {
    // It is important to get methods from here to maintain single source of truth.
    const {
      options: methods
    } = oktaUnderscore.find(uiSchema, schema => schema.name === 'authenticator.methodType');

    const relatesToObject = this.options.currentViewState.relatesTo;
    const {
      profile: profile
    } = (relatesToObject === null || relatesToObject === void 0 ? void 0 : relatesToObject.value) || {};
    const ModelClass = BaseView.prototype.createModelClass.apply(this, arguments);
    const local = Object.assign({
      primaryMode: {
        'value': methods[0].value,
        'type': 'string'
      },
      secondaryMode: {
        'value': methods[1] && methods[1].value,
        'type': 'string'
      },
      phoneNumber: {
        'value': profile !== null && profile !== void 0 && profile.phoneNumber ? profile.phoneNumber : loc('oie.phone.alternate.title', 'login'),
        'type': 'string'
      }
    }, ModelClass.prototype.local);
    return ModelClass.extend({
      local: local
    });
  }
});

export { ChallengeAuthenticatorDataPhoneView as default };
//# sourceMappingURL=ChallengeAuthenticatorDataPhoneView.js.map
