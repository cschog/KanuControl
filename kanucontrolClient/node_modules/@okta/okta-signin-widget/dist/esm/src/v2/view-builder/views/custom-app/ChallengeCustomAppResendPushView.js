import { loc, createCallout } from '../../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import '../../internals/BaseView.js';
import '../../components/AuthenticatorEnrollOptions.js';
import '../../components/AuthenticatorVerifyOptions.js';
import '../../../../util/FactorUtil.js';
import '../../../../v1/views/admin-consent/ScopeList.js';
import '../../../../v1/views/consent/ScopeList.js';
import '../captcha/CaptchaView.js';
import BaseAuthenticatorView from '../../components/BaseAuthenticatorView.js';

const CUSTOM_APP_UV_ENABLE_BIOMETRIC_SERVER_KEY = 'oie.authenticator.custom_app.method.push.verify.enable.biometrics';
const Body = BaseForm.extend(Object.assign({
  className: 'custom-app-verify-resend-push',
  title: function () {
    return loc('oie.verify.custom_app.title', 'login', [this.options.appState.getAuthenticatorDisplayName()]);
  },
  save: function () {
    return loc('oie.custom_app.push.resend', 'login');
  },
  showMessages: function () {
    let options = {};

    if (this.options.appState.containsMessageWithI18nKey(CUSTOM_APP_UV_ENABLE_BIOMETRIC_SERVER_KEY)) {
      // add a title, subtitle, and bullet-points for 
      // Custom App enable biometrics message during verification
      options.content = null;
      options.className = 'okta-verify-uv-callout-content';
      options.title = loc('oie.authenticator.custom_app.method.push.verify.enable.biometrics.title', 'login', [this.options.appState.getAuthenticatorDisplayName()]);
      options.subtitle = loc('oie.authenticator.custom_app.method.push.verify.enable.biometrics.description', 'login');
      options.type = 'error';
      options.bullets = [loc('oie.authenticator.custom_app.method.push.verify.enable.biometrics.point1', 'login'), loc('oie.authenticator.custom_app.method.push.verify.enable.biometrics.point2', 'login', [this.options.appState.getAuthenticatorDisplayName()]), loc('oie.authenticator.custom_app.method.push.verify.enable.biometrics.point3', 'login', [this.options.appState.getAuthenticatorDisplayName()])];
      options = createCallout(options);
    }

    BaseForm.prototype.showMessages.call(this, options);
  }
}));
var ChallengeCustomAppResendPushView = BaseAuthenticatorView.extend({
  Body: Body
});

export { ChallengeCustomAppResendPushView as default };
//# sourceMappingURL=ChallengeCustomAppResendPushView.js.map
