import '../internals/BaseHeader.js';
import BaseFooter from '../internals/BaseFooter.js';
import BaseForm from '../internals/BaseForm.js';
import '../internals/BaseFormWithPolling.js';
import '../internals/BaseOktaVerifyChallengeView.js';
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
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import BaseView from '../internals/BaseView.js';
import '../components/AuthenticatorEnrollOptions.js';
import '../components/AuthenticatorVerifyOptions.js';
import '../../../util/FactorUtil.js';
import '../../../v1/views/admin-consent/ScopeList.js';
import '../../../v1/views/consent/ScopeList.js';
import './captcha/CaptchaView.js';
import { getFactorPageCustomLink } from '../utils/LinksUtil.js';

const UNLOCK_USER_SUCCESS_MESSAGE = 'oie.selfservice.unlock_user.landing.to.app.success.message';
const Body = BaseForm.extend({
  title: function () {
    if (this.isPasswordRecoveryFlow()) {
      return loc('password.reset.title.generic', 'login');
    }

    return loc('oie.select.authenticators.verify.title', 'login');
  },
  subtitle: function () {
    if (this.isPasswordRecoveryFlow()) {
      return loc('oie.password.reset.verification', 'login');
    }

    if (this.isUnlockSuccess()) {
      const container = '.o-form-error-container';
      const text = loc('oie.select.authenticators.verify.subtitle', 'login');
      this.add(`<div class="ion-messages-container"><p>${text}</p></div>`, container);
      return;
    }

    return loc('oie.select.authenticators.verify.subtitle', 'login');
  },
  isPasswordRecoveryFlow: function () {
    return this.options.appState.get('isPasswordRecovery');
  },
  noButtonBar: true,
  showMessages: function () {
    if (this.isUnlockSuccess()) {
      let options = {};
      options.subtitle = loc('oie.selfservice.unlock_user.landing.to.app.success.message', 'login');
      options.type = 'success';
      options = createCallout(options);
      BaseForm.prototype.showMessages.call(this, options);
      return;
    }

    BaseForm.prototype.showMessages.call(this);
  },
  isUnlockSuccess: function () {
    return this.options.appState.containsMessageWithI18nKey(UNLOCK_USER_SUCCESS_MESSAGE);
  }
});
var SelectAuthenticatorVerifyView = BaseView.extend({
  Body: Body,
  Footer: BaseFooter.extend({
    links: function () {
      return getFactorPageCustomLink(this.options.appState, this.options.settings);
    }
  })
});

export { Body, SelectAuthenticatorVerifyView as default };
//# sourceMappingURL=SelectAuthenticatorVerifyView.js.map
