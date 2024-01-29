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

const OV_NMC_FORCE_UPGRAGE_SERVER_KEY = 'oie.authenticator.app.method.push.force.upgrade.number_challenge';
const OV_UV_ENABLE_BIOMETRIC_SERVER_KEY = 'oie.authenticator.app.method.push.verify.enable.biometrics';
const Body = BaseForm.extend(Object.assign({
  className: 'okta-verify-resend-push',
  title: function () {
    return loc('oie.okta_verify.push.title', 'login');
  },
  save: function () {
    return loc('oie.okta_verify.push.resend', 'login');
  },
  showMessages: function () {
    let options = {};

    if (this.options.appState.containsMessageWithI18nKey(OV_NMC_FORCE_UPGRAGE_SERVER_KEY)) {
      // add a title for OV force upgrade
      options.title = loc('oie.numberchallenge.force.upgrade.title', 'login');
    } else if (this.options.appState.containsMessageWithI18nKey(OV_UV_ENABLE_BIOMETRIC_SERVER_KEY)) {
      // add a title for OV enable biometrics message during verification
      options.content = null;
      options.className = 'okta-verify-uv-callout-content';
      options.title = loc('oie.authenticator.app.method.push.verify.enable.biometrics.title', 'login');
      options.subtitle = loc('oie.authenticator.app.method.push.verify.enable.biometrics.description', 'login');
      options.type = 'error';
      options.bullets = [loc('oie.authenticator.app.method.push.verify.enable.biometrics.point1', 'login'), loc('oie.authenticator.app.method.push.verify.enable.biometrics.point2', 'login'), loc('oie.authenticator.app.method.push.verify.enable.biometrics.point3', 'login')];
      options = createCallout(options);
    }

    BaseForm.prototype.showMessages.call(this, options);
  }
}));
var ChallengeOktaVerifyResendPushView = BaseAuthenticatorView.extend({
  Body: Body
});

export { ChallengeOktaVerifyResendPushView as default };
//# sourceMappingURL=ChallengeOktaVerifyResendPushView.js.map
