import BaseAuthenticatorView from '../../components/BaseAuthenticatorView.js';
import '../../internals/BaseHeader.js';
import '../../internals/BaseFooter.js';
import BaseForm from '../../internals/BaseForm.js';
import '../../internals/BaseFormWithPolling.js';
import '../../internals/BaseOktaVerifyChallengeView.js';
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
import oktaUnderscore from '../../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import '../../internals/BaseView.js';
import '../../components/AuthenticatorEnrollOptions.js';
import '../../components/AuthenticatorVerifyOptions.js';
import '../../../../util/FactorUtil.js';
import '../../../../v1/views/admin-consent/ScopeList.js';
import '../../../../v1/views/consent/ScopeList.js';
import '../captcha/CaptchaView.js';
import { getBiometricsErrorOptions } from '../../utils/ChallengeViewUtil.js';

// redirect is needed for Apple SSO Extension to intercept the request, because
// - XHR request is not interceptable
// - form post is interceptable, but some app (Outlook) closes the app after
// We may have a different approach/UX for GA

const Body = BaseForm.extend({
  noButtonBar: true,
  className: 'ion-form device-challenge-poll',
  title: function () {
    return loc('deviceTrust.sso.redirectText', 'login');
  },
  initialize: function () {
    var _this$options$current, _this$options$current2;

    BaseForm.prototype.initialize.apply(this, arguments);
    this.listenTo(this.model, 'error', () => {
      this.$('.spinner').hide();
    });
    this.add('<div class="credential-sso-extension"><div class="spinner"></div></div>');
    const isGetMethod = ((_this$options$current = this.options.currentViewState) === null || _this$options$current === void 0 ? void 0 : (_this$options$current2 = _this$options$current.method) === null || _this$options$current2 === void 0 ? void 0 : _this$options$current2.toLowerCase()) === 'get';
    this.model.set('useRedirect', isGetMethod);
    this.trigger('save', this.model);
  },
  showCustomFormErrorCallout: function (error) {
    const options = getBiometricsErrorOptions(error, false); // If not biometrics error, just show the returned error message

    if (oktaUnderscore.isEmpty(options)) {
      return false;
    }

    this.showMessages(createCallout(options));
    return true;
  }
});
var ChallengeOktaVerifySSOExtensionView = BaseAuthenticatorView.extend({
  Body: Body
});

export { ChallengeOktaVerifySSOExtensionView as default };
//# sourceMappingURL=ChallengeOktaVerifySSOExtensionView.js.map
