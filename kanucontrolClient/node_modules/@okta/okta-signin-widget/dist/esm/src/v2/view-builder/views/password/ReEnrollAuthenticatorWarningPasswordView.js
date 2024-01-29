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
import BaseFooter from '../../internals/BaseFooter.js';
import '../../internals/BaseForm.js';
import '../../internals/BaseFormWithPolling.js';
import '../../internals/BaseOktaVerifyChallengeView.js';
import '../../internals/BaseView.js';
import '../../components/AuthenticatorEnrollOptions.js';
import '../../components/AuthenticatorVerifyOptions.js';
import '../../../../util/FactorUtil.js';
import '../../../../v1/views/admin-consent/ScopeList.js';
import '../../../../v1/views/consent/ScopeList.js';
import '../captcha/CaptchaView.js';
import EnrollAuthenticatorPasswordView from './EnrollAuthenticatorPasswordView.js';

const Body = EnrollAuthenticatorPasswordView.prototype.Body.extend({
  className: 'password-authenticator',
  subtitle: function () {
    if (this.options.settings.get('brandName')) {
      return loc('password.expiring.subtitle.specific', 'login', [this.options.settings.get('brandName')]);
    }
  },
  title: function () {
    const passwordPolicy = this.getPasswordPolicySettings() || {};
    const daysToExpiry = passwordPolicy.daysToExpiry;

    if (daysToExpiry > 0) {
      return loc('password.expiring.title', 'login', [daysToExpiry]);
    } else if (daysToExpiry === 0) {
      return loc('password.expiring.today', 'login');
    } else {
      return loc('password.expiring.soon', 'login');
    }
  },
  save: function () {
    return loc('password.expired.submit', 'login');
  },
  showMessages: function () {
    // if brandName is configured and messages is present, render as subtitle with brandName in context
    if (this.options.settings.get('brandName')) {
      return null;
    } // else if brandName is not set, render messages object sent from server as text


    EnrollAuthenticatorPasswordView.prototype.Body.prototype.showMessages.apply(this, arguments);
  }
});
const Footer = BaseFooter.extend({
  links: function () {
    const links = [];

    if (this.options.appState.hasRemediationObject('skip')) {
      links.push({
        'type': 'link',
        'label': loc('password.expiring.later', 'login'),
        'name': 'skip',
        'actionPath': 'skip'
      });
    }

    return links;
  }
});
var ReEnrollAuthenticatorWarningPasswordView = EnrollAuthenticatorPasswordView.extend({
  Body: Body,
  Footer: Footer
});

export { ReEnrollAuthenticatorWarningPasswordView as default };
//# sourceMappingURL=ReEnrollAuthenticatorWarningPasswordView.js.map
