import { createCallout, loc } from '../../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import BaseFormWithPolling from '../../internals/BaseFormWithPolling.js';
import '../../internals/BaseOktaVerifyChallengeView.js';
import '../../internals/BaseView.js';
import '../../components/AuthenticatorEnrollOptions.js';
import '../../components/AuthenticatorVerifyOptions.js';
import '../../../../util/FactorUtil.js';
import '../../../../v1/views/admin-consent/ScopeList.js';
import '../../../../v1/views/consent/ScopeList.js';
import '../captcha/CaptchaView.js';
import email from '../shared/email.js';
import BaseAuthenticatorView from '../../components/BaseAuthenticatorView.js';
import BaseResendView from '../shared/BaseResendView.js';

const ResendView = BaseResendView.extend({
  className: 'hide resend-email-view',
  events: {
    'click a.resend-link': 'handelResendLink'
  },
  initialize: function () {
    this.add(createCallout({
      content: `${loc('email.code.not.received', 'login')}
        <a class='resend-link'>${loc('email.button.resend', 'login')}</a>`,
      type: 'warning'
    }));
  },
  handelResendLink: function () {
    this.options.appState.trigger('invokeAction', this.options.resendEmailAction); // Hide warning, but reinitiate to show warning again after some threshold of polling

    if (!this.$el.hasClass('hide')) {
      this.$el.addClass('hide');
    }

    this.showCalloutAfterTimeout();
  }
});
const Body = BaseFormWithPolling.extend(Object.assign({
  save: function () {
    return loc('mfa.challenge.verify', 'login');
  },
  initialize: function () {
    BaseFormWithPolling.prototype.initialize.apply(this, arguments);
    this.add(ResendView, {
      selector: '.o-form-error-container',
      options: {
        resendEmailAction: this.resendEmailAction
      }
    });
    this.startPolling();
  },
  saveForm: function () {
    BaseForm.prototype.saveForm.apply(this, arguments);
    this.stopPolling();
  },
  remove: function () {
    BaseForm.prototype.remove.apply(this, arguments);
    this.stopPolling();
  },
  triggerAfterError: function (model, error) {
    var _error$responseJSON, _error$responseJSON$e;

    BaseForm.prototype.triggerAfterError.apply(this, arguments);
    const isFormPolling = !!this.polling;
    this.stopPolling();

    if ((_error$responseJSON = error.responseJSON) !== null && _error$responseJSON !== void 0 && (_error$responseJSON$e = _error$responseJSON.errorSummaryKeys) !== null && _error$responseJSON$e !== void 0 && _error$responseJSON$e.includes('idx.session.expired')) {
      // Do NOT resume polling since session is invalid and polling is already stopped
      return;
    }

    if (this.isRateLimitError(error)) {
      // When polling encounter rate limit error, wait 60 sec for rate limit bucket to reset
      // before polling again & hide error message
      if (isFormPolling) {
        setTimeout(() => {
          model.trigger('clearFormError');
        }, 0);
      }

      this.startPolling(60000);
    } else {
      this.startPolling(this.options.appState.get('dynamicRefreshInterval'));
    }
  },
  isRateLimitError: function (error) {
    var _error$responseJSON2, _error$responseJSON2$, _error$responseJSON3, _error$responseJSON4;

    return ((_error$responseJSON2 = error.responseJSON) === null || _error$responseJSON2 === void 0 ? void 0 : (_error$responseJSON2$ = _error$responseJSON2.errorSummaryKeys) === null || _error$responseJSON2$ === void 0 ? void 0 : _error$responseJSON2$.includes('tooManyRequests') // IDX API error
    ) || ((_error$responseJSON3 = error.responseJSON) === null || _error$responseJSON3 === void 0 ? void 0 : _error$responseJSON3.errorCode) === 'E0000047' && !((_error$responseJSON4 = error.responseJSON) !== null && _error$responseJSON4 !== void 0 && _error$responseJSON4.errorIntent); // Standard API error
  }
}, email));
var BaseAuthenticatorEmailView = BaseAuthenticatorView.extend({
  Body: Body
});

export { BaseAuthenticatorEmailView as default };
//# sourceMappingURL=BaseAuthenticatorEmailView.js.map
