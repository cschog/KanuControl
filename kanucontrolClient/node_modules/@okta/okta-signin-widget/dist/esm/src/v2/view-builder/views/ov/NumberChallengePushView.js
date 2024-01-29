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
import '../../internals/BaseForm.js';
import BaseFormWithPolling from '../../internals/BaseFormWithPolling.js';
import '../../internals/BaseOktaVerifyChallengeView.js';
import '../../internals/BaseView.js';
import '../../components/AuthenticatorEnrollOptions.js';
import '../../components/AuthenticatorVerifyOptions.js';
import '../../../../util/FactorUtil.js';
import '../../../../v1/views/admin-consent/ScopeList.js';
import '../../../../v1/views/consent/ScopeList.js';
import '../captcha/CaptchaView.js';
import polling from '../shared/polling.js';
import ResendNumberChallengeView from './ResendNumberChallengeView.js';
import NumberChallengePhoneView from './NumberChallengePhoneView.js';

const Body = BaseFormWithPolling.extend(Object.assign({
  noButtonBar: true,
  className: 'okta-verify-number-challenge',
  events: {
    'click a.resend-number-challenge': 'handleResendNumberChallenge'
  },
  handleResendNumberChallenge: function () {
    this.options.appState.trigger('invokeAction', 'currentAuthenticator-resend'); // hide the warning

    this.options.appState.trigger('hideNumberChallengeWarning');
  },
  title: function () {
    return loc('oie.okta_verify.push.sent', 'login');
  },
  initialize: function () {
    BaseFormWithPolling.prototype.initialize.apply(this, arguments);
    this.add(NumberChallengePhoneView);
  },
  triggerAfterError: function () {
    BaseFormWithPolling.prototype.triggerAfterError.apply(this, arguments);
    this.stopPolling();
    this.$el.find('.o-form-fieldset-container').empty();
  },
  postRender: function () {
    BaseFormWithPolling.prototype.postRender.apply(this, arguments);
    this.startPoll();
  },
  startPoll: function () {
    this.startPolling();
    this.addWarning();
  },
  stopPoll: function () {
    this.stopPolling();
  },
  addWarning: function () {
    this.add(ResendNumberChallengeView, '.o-form-error-container');
    this.options.appState.trigger('showNumberChallengeWarning');
  },
  remove: function () {
    BaseFormWithPolling.prototype.remove.apply(this, arguments);
    this.stopPolling();
  }
}, polling));

export { Body as default };
//# sourceMappingURL=NumberChallengePushView.js.map
