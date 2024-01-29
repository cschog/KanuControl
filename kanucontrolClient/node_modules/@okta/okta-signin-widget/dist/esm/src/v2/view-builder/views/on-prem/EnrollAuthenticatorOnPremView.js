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
import '../../internals/BaseView.js';
import '../../components/AuthenticatorEnrollOptions.js';
import '../../components/AuthenticatorVerifyOptions.js';
import '../../../../util/FactorUtil.js';
import '../../../../v1/views/admin-consent/ScopeList.js';
import '../../../../v1/views/consent/ScopeList.js';
import '../captcha/CaptchaView.js';
import BaseAuthenticatorView from '../../components/BaseAuthenticatorView.js';

const ON_PREM_TOKEN_CHANGE_KEY = 'errors.E0000113';
const Body = BaseForm.extend({
  className: 'on-prem-authenticator-enroll',
  modelEvents: {
    'error': '_checkTokenChange'
  },
  title: function () {
    const vendorName = this.options.appState.getAuthenticatorDisplayName() || loc('oie.on_prem.authenticator.default.vendorName', 'login');
    return loc('oie.on_prem.enroll.title', 'login', [vendorName]);
  },
  _checkTokenChange: function (model, convertedErrors) {
    var _convertedErrors$resp;

    const errorSummaryKeys = convertedErrors === null || convertedErrors === void 0 ? void 0 : (_convertedErrors$resp = convertedErrors.responseJSON) === null || _convertedErrors$resp === void 0 ? void 0 : _convertedErrors$resp.errorSummaryKeys;

    if (errorSummaryKeys && errorSummaryKeys.includes(ON_PREM_TOKEN_CHANGE_KEY)) {
      // this means we are in change pin, so we should clear out answer input
      this.model.set('credentials.passcode', '');
      this.render();
    }
  },
  save: function () {
    return loc('mfa.challenge.verify', 'login');
  }
});
var EnrollAuthenticatorOnPremView = BaseAuthenticatorView.extend({
  Body: Body
});

export { EnrollAuthenticatorOnPremView as default };
//# sourceMappingURL=EnrollAuthenticatorOnPremView.js.map
