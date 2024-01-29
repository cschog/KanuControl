import '../../internals/BaseHeader.js';
import BaseFooter from '../../internals/BaseFooter.js';
import '../../internals/BaseForm.js';
import '../../internals/BaseFormWithPolling.js';
import '../../internals/BaseOktaVerifyChallengeView.js';
import '../../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import '../../../../v1/views/admin-consent/ScopeList.js';
import '../../../../v1/views/consent/ScopeList.js';
import '../captcha/CaptchaView.js';
import BaseAuthenticatorView from '../../components/BaseAuthenticatorView.js';
import Body from './ChallengeOktaVerifyCustomAppPushOnlyFormView.js';
import Body$1 from './ChallengeOktaVerifyCustomAppFormView.js';

// Common view for OV push and custom app.
var ChallengeOktaVerifyCustomAppDataView = BaseAuthenticatorView.extend({
  initialize: function () {
    BaseAuthenticatorView.prototype.initialize.apply(this, arguments);

    if (this.isPushOnlyWithAutoChallengeFlow()) {
      this.Body = Body;
    } else {
      this.Body = Body$1;
      this.Footer = BaseFooter;
    }
  },
  isPushOnlyWithAutoChallengeFlow: function () {
    const methodType = this.options.appState.getSchemaByName('authenticator.methodType');
    const hasAutoChallengeSchema = this.options.appState.getSchemaByName('authenticator.autoChallenge');
    const methodTypeOptions = methodType === null || methodType === void 0 ? void 0 : methodType.options;
    return hasAutoChallengeSchema && methodTypeOptions.length === 1 && methodTypeOptions[0].value === 'push';
  }
});

export { ChallengeOktaVerifyCustomAppDataView as default };
//# sourceMappingURL=ChallengeOktaVerifyCustomAppDataView.js.map
