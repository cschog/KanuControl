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

const OV_UV_ENABLE_BIOMETRIC_SERVER_KEY = 'oie.authenticator.oktaverify.method.totp.verify.enable.biometrics';
const Body = BaseForm.extend(Object.assign({
  className: 'okta-verify-totp-challenge',
  title: function () {
    return loc('oie.okta_verify.totp.title', 'login');
  },
  save: function () {
    return loc('mfa.challenge.verify', 'login');
  },
  showCustomFormErrorCallout: function (error) {
    var _error$responseJSON;

    const errorSummaryKeys = error === null || error === void 0 ? void 0 : (_error$responseJSON = error.responseJSON) === null || _error$responseJSON === void 0 ? void 0 : _error$responseJSON.errorSummaryKeys;
    let options;

    if (errorSummaryKeys && errorSummaryKeys.includes(OV_UV_ENABLE_BIOMETRIC_SERVER_KEY)) {
      options = {
        type: 'error',
        className: 'okta-verify-uv-callout-content',
        title: loc('oie.authenticator.app.method.push.verify.enable.biometrics.title', 'login'),
        subtitle: loc('oie.authenticator.app.method.push.verify.enable.biometrics.description', 'login'),
        bullets: [loc('oie.authenticator.app.method.push.verify.enable.biometrics.point1', 'login'), loc('oie.authenticator.app.method.push.verify.enable.biometrics.point2', 'login'), loc('oie.authenticator.app.method.push.verify.enable.biometrics.point3', 'login')]
      };
      this.showMessages(createCallout(options));
      return true;
    }
  }
}));
var ChallengeOktaVerifyTotpView = BaseAuthenticatorView.extend({
  Body: Body
});

export { ChallengeOktaVerifyTotpView as default };
//# sourceMappingURL=ChallengeOktaVerifyTotpView.js.map
