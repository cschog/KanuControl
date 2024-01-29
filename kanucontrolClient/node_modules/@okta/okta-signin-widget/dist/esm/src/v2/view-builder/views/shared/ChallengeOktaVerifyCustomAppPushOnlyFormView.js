import '../../internals/BaseHeader.js';
import '../../internals/BaseFooter.js';
import BaseForm from '../../internals/BaseForm.js';
import '../../internals/BaseFormWithPolling.js';
import '../../internals/BaseOktaVerifyChallengeView.js';
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
import '../../internals/BaseView.js';
import '../../components/AuthenticatorEnrollOptions.js';
import '../../components/AuthenticatorVerifyOptions.js';
import '../../../../util/FactorUtil.js';
import { AUTHENTICATOR_KEY } from '../../../ion/RemediationConstants.js';
import '../../../../v1/views/admin-consent/ScopeList.js';
import '../../../../v1/views/consent/ScopeList.js';
import '../captcha/CaptchaView.js';

// Common view for OV push and custom app.
const Body = BaseForm.extend(Object.assign({
  className: function () {
    return this.isOV() ? 'okta-verify-send-push-form' : 'custom-app-send-push-form';
  },
  save: function () {
    return this.options.appState.get('authenticatorKey') === AUTHENTICATOR_KEY.OV ? loc('oie.okta_verify.sendPushButton', 'login') : loc('oie.custom_app.sendPushButton', 'login');
  },
  title: function () {
    return this.isOV() ? loc('oie.okta_verify.push.title', 'login') : loc('oie.custom_app.push.title', 'login');
  },
  render: function () {
    BaseForm.prototype.render.apply(this, arguments); // Move checkbox below the button
    // Checkbox is rendered by BaseForm using remediation response and 
    // hence by default always gets added above buttons.

    const checkbox = this.$el.find('[data-se="o-form-fieldset-authenticator.autoChallenge"]');
    checkbox.length && this.$el.find('.o-form-button-bar').after(checkbox);
  },
  getUISchema: function () {
    const uiSchemas = BaseForm.prototype.getUISchema.apply(this, arguments);
    return uiSchemas.filter(schema => schema.name !== 'authenticator.methodType');
  },
  isOV: function () {
    return this.options.appState.get('authenticatorKey') === AUTHENTICATOR_KEY.OV;
  }
}));

export { Body as default };
//# sourceMappingURL=ChallengeOktaVerifyCustomAppPushOnlyFormView.js.map
