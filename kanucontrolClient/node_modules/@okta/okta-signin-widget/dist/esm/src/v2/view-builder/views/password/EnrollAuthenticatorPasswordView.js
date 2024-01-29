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
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
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
import { getPasswordComplexityDescriptionForHtmlList, removeRequirementsFromError } from '../../utils/AuthenticatorUtil.js';
import '../../../../v1/views/admin-consent/ScopeList.js';
import '../../../../v1/views/consent/ScopeList.js';
import '../captcha/CaptchaView.js';
import BaseAuthenticatorView from '../../components/BaseAuthenticatorView.js';
import { generatePasswordPolicyHtml } from './PasswordPolicyUtil.js';

const Body = BaseForm.extend({
  title: function () {
    return loc('oie.password.enroll.title', 'login');
  },
  save: function () {
    return loc('oform.next', 'login');
  },
  initialize: function () {
    BaseForm.prototype.initialize.apply(this, arguments);
    const policy = this.getPasswordPolicySettings();
    this.displayPasswordPolicy(policy);
  },
  displayPasswordPolicy: function (policy) {
    if (policy) {
      const rulesList = getPasswordComplexityDescriptionForHtmlList(policy);
      generatePasswordPolicyHtml(this, rulesList, true);
    }
  },
  triggerAfterError: function (model, error) {
    error.responseJSON = removeRequirementsFromError(error.responseJSON);
    this.options.appState.trigger('afterError', error);
  },
  getPasswordPolicySettings: function () {
    var _relatesToObject$valu;

    // This will be overridden by following scenario since the policies could be different for those.
    // - password reset (`ReEnrollAuthenticatorPasswordView.js`)
    //
    const relatesToObject = this.options.currentViewState.relatesTo;
    return relatesToObject === null || relatesToObject === void 0 ? void 0 : (_relatesToObject$valu = relatesToObject.value) === null || _relatesToObject$valu === void 0 ? void 0 : _relatesToObject$valu.settings;
  },
  getUISchema: function () {
    const uiSchemas = BaseForm.prototype.getUISchema.apply(this, arguments);
    const confirmPassword = {
      name: 'confirmPassword',
      label: loc('oie.password.confirmPasswordLabel', 'login'),
      type: 'password',
      'label-top': true,
      params: {
        showPasswordToggle: this.settings.get('showPasswordToggle')
      }
    };
    const updatedSchema = [];

    for (let field of uiSchemas) {
      updatedSchema.push(field);

      if (field.name === 'credentials.passcode') {
        updatedSchema.push(confirmPassword);
      }
    }

    return updatedSchema;
  }
});
var EnrollAuthenticatorPasswordView = BaseAuthenticatorView.extend({
  Body: Body,
  createModelClass: function () {
    const ModelClass = BaseView.prototype.createModelClass.apply(this, arguments);
    const local = Object.assign({
      confirmPassword: {
        type: 'string',
        required: true
      }
    }, ModelClass.prototype.local);
    return ModelClass.extend({
      local: local,
      validate: function () {
        if (this.get('credentials.passcode') !== this.get('confirmPassword') && this.get('credential.value') !== this.get('confirmPassword')) {
          const errors = {
            'confirmPassword': loc('password.error.match', 'login')
          };
          return errors;
        } else {
          return null;
        }
      }
    });
  }
});

export { EnrollAuthenticatorPasswordView as default };
//# sourceMappingURL=EnrollAuthenticatorPasswordView.js.map
