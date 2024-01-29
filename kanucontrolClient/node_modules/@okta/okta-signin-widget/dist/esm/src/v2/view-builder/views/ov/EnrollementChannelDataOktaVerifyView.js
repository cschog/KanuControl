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
import Model from '../../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
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
import fn from '../../../../util/CountryUtil.js';
import SwitchEnrollChannelLinkView from './SwitchEnrollChannelLinkView.js';

const Body = BaseForm.extend({
  className: 'oie-enroll-ov-data',
  title: function () {
    return this.options.appState.get('currentAuthenticator').contextualData.selectedChannel === 'email' ? loc('oie.enroll.okta_verify.enroll.channel.email.title', 'login') : loc('oie.enroll.okta_verify.enroll.channel.sms.title', 'login');
  },
  save: function () {
    return loc('oie.enroll.okta_verify.setupLink', 'login');
  },
  getUISchema: function () {
    const uiSchemas = BaseForm.prototype.getUISchema.apply(this, arguments);

    const phoneNumberUISchema = oktaUnderscore.find(uiSchemas, ({
      name: name
    }) => name === 'phoneNumber');

    const phoneNumberUISchemaIndex = oktaUnderscore.findIndex(uiSchemas, ({
      name: name
    }) => name === 'phoneNumber');

    const countryUISchema = {
      'label-top': true,
      label: loc('country.label', 'login'),
      type: 'select',
      options: fn.getCountries(),
      name: 'country',
      wide: true
    }; // Create an input group - serves as a display wrapper

    const phoneNumberWithCodeUISchema = {
      label: loc('mfa.phoneNumber.placeholder', 'login'),
      type: 'group',
      modelType: 'string',
      'label-top': true,
      name: 'phoneCode',
      input: [{
        type: 'label',

        /* eslint-disable-next-line @okta/okta/no-unlocalized-text */
        label: `+${this.model.get('phoneCode')}`,
        className: 'country-code-label no-translate'
      }, Object.assign({}, phoneNumberUISchema)]
    };

    if (phoneNumberUISchemaIndex !== -1) {
      // Replace phoneNumberUISchema and add countryUISchema before phone.
      uiSchemas.splice(phoneNumberUISchemaIndex, 1, countryUISchema, phoneNumberWithCodeUISchema);
    }

    const description = {
      View: this.options.appState.get('currentAuthenticator').contextualData.selectedChannel === 'email' ? loc('oie.enroll.okta_verify.channel.email.description', 'login') : loc('oie.enroll.okta_verify.channel.sms.description', 'login'),
      selector: '.o-form-fieldset-container'
    };
    uiSchemas.push(description);
    return uiSchemas;
  },
  handlePhoneCodeChange: function () {
    const countryCodeField = this.el.querySelector('.country-code-label');
    countryCodeField.innerText = `+${this.model.get('phoneCode')}`;
  },
  initialize: function () {
    BaseForm.prototype.initialize.apply(this, arguments);

    if (this.options.appState.get('currentAuthenticator').contextualData.selectedChannel === 'sms') {
      this.listenTo(this.model, 'change:phoneCode', this.handlePhoneCodeChange.bind(this));
    }
  }
});
var EnrollementChannelDataOktaVerifyView = BaseAuthenticatorView.extend({
  Body: Body,
  createModelClass: function () {
    const ModelClass = BaseView.prototype.createModelClass.apply(this, arguments);

    if (this.options.appState.get('currentAuthenticator').contextualData.selectedChannel !== 'sms') {
      return ModelClass;
    }

    const local = Object.assign({
      country: {
        // Set default country to "US"
        'value': 'US',
        'type': 'string'
      }
    }, ModelClass.prototype.local);
    const derived = Object.assign({
      phoneCode: {
        deps: ['country'],
        fn: function (country) {
          return fn.getCallingCodeForCountry(country);
        }
      }
    }, ModelClass.prototype.derived);
    return ModelClass.extend({
      local: local,
      derived: derived,
      toJSON: function () {
        const modelJSON = Model.prototype.toJSON.call(this, arguments);
        const phoneCode = this.get('phoneCode'); // Add country code..

        let formattedPhoneNumber = `+${phoneCode}${modelJSON.phoneNumber}`; // Override phone with formatted number..

        modelJSON.phoneNumber = formattedPhoneNumber;
        return modelJSON;
      }
    });
  },
  postRender: function () {
    this.add(SwitchEnrollChannelLinkView, 'form');
  }
});

export { EnrollementChannelDataOktaVerifyView as default };
//# sourceMappingURL=EnrollementChannelDataOktaVerifyView.js.map
