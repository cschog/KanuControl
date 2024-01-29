import { loc, createCallout } from '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import oktaUnderscore from '../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import '../internals/BaseHeader.js';
import '../internals/BaseFooter.js';
import BaseForm from '../internals/BaseForm.js';
import '../internals/BaseFormWithPolling.js';
import '../internals/BaseOktaVerifyChallengeView.js';
import BaseView from '../internals/BaseView.js';
import '../components/AuthenticatorEnrollOptions.js';
import '../components/AuthenticatorVerifyOptions.js';
import '../../../util/FactorUtil.js';
import { INTERSTITIAL_REDIRECT_VIEW } from '../../ion/RemediationConstants.js';
import '../../../v1/views/admin-consent/ScopeList.js';
import '../../../v1/views/consent/ScopeList.js';
import './captcha/CaptchaView.js';
import CustomAccessDeniedErrorMessage from './shared/CustomAccessDeniedErrorMessage.js';

const CUSTOM_ACCESS_DENIED_KEY = 'security.access_denied_custom_message';
const UNLOCK_USER_SUCCESS_MESSAGE = 'oie.selfservice.unlock_user.landing.to.app.success.message';
const Body = BaseForm.extend({
  title: function () {
    let titleString = loc('oie.success.text.signingIn', 'login'); // For more info on the API response available in appState, see IdxResponseBuilder.java

    const app = this.options.appState.get('app');
    const user = this.options.appState.get('user'); // If the option is not set, we treat that as being the case where we don't render the spinner.
    // This is to account for the customer hosted scenario, because by default okta-core will pass in
    // the correct value as set by the option in the Admin UI (which by default is "DEFAULT").

    if (!this.redirectView || this.redirectView === INTERSTITIAL_REDIRECT_VIEW.NONE) {
      titleString = loc('oie.success.text.signingIn.with.ellipsis', 'login');
      return titleString;
    }

    if (oktaUnderscore.isEmpty(app)) {
      return titleString;
    }

    const {
      label: appInstanceName,
      name: appDisplayName
    } = app;
    const {
      identifier: userEmail
    } = user || {};
    const appName = appInstanceName ? appInstanceName : appDisplayName;

    if (appName && userEmail && !this.settings.get('features.showIdentifier')) {
      titleString = loc('oie.success.text.signingIn.with.appName.and.identifier', 'login', [appName, userEmail]);
    } else if (appName) {
      titleString = loc('oie.success.text.signingIn.with.appName', 'login', [appName]);
    }

    return titleString;
  },
  showMessages: function () {
    if (this.isUnlockSuccess()) {
      const container = '.o-form-error-container';
      const text = loc('oie.selfservice.unlock_user.landing.to.app.signing.in.message', 'login');
      this.add(`<div class="ion-messages-container"><p>${text}</p></div>`, container);
      return;
    } else if (this.options.appState.containsMessageStartingWithI18nKey(CUSTOM_ACCESS_DENIED_KEY)) {
      const {
        message: message,
        links: links
      } = this.options.appState.get('messages').value.find(msg => msg.i18n.key === CUSTOM_ACCESS_DENIED_KEY);
      this.add(createCallout({
        type: 'error',
        content: new CustomAccessDeniedErrorMessage({
          message: message,
          links: links
        })
      }));
      return;
    }

    BaseForm.prototype.showMessages.call(this);
  },
  isUnlockSuccess: function () {
    return this.options.appState.containsMessageWithI18nKey(UNLOCK_USER_SUCCESS_MESSAGE);
  },
  noButtonBar: true,
  initialize: function () {
    BaseForm.prototype.initialize.apply(this, arguments);
    this.redirectView = this.settings.get('interstitialBeforeLoginRedirect');
    this.model.set('useRedirect', true);
    this.trigger('save', this.model);
  },
  render: function () {
    BaseForm.prototype.render.apply(this, arguments);

    if (this.redirectView === INTERSTITIAL_REDIRECT_VIEW.DEFAULT) {
      this.add('<div class="okta-waiting-spinner"></div>');
    }
  }
});
var AutoRedirectView = BaseView.extend({
  Body: Body
});

export { AutoRedirectView as default };
//# sourceMappingURL=AutoRedirectView.js.map
