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
import AuthenticatorFooter from '../../components/AuthenticatorFooter.js';
import BaseAuthenticatorView from '../../components/BaseAuthenticatorView.js';
import { getForgotPasswordLink } from '../../utils/LinksUtil.js';
import { isCustomizedI18nKey } from '../../../ion/i18nTransformer.js';

const Body = BaseForm.extend({
  title: function () {
    return loc('oie.password.challenge.title', 'login');
  },
  save: function () {
    return loc('mfa.challenge.verify', 'login');
  },

  /**
   * Update UI schemas for customization from .widgetrc.js or Admin Customization settings page.
   * @returns Array
   */
  getUISchema: function () {
    const schemas = BaseForm.prototype.getUISchema.apply(this, arguments);
    const {
      settings: settings
    } = this.options;
    const passwordExplainLabeli18nKey = 'primaryauth.password.tooltip';
    const passwordSchema = schemas.find(({
      name: name
    }) => name === 'credentials.passcode');

    if (passwordSchema && isCustomizedI18nKey(passwordExplainLabeli18nKey, settings)) {
      passwordSchema.explain = loc(passwordExplainLabeli18nKey, 'login');
      passwordSchema['explain-top'] = true;
    }

    return schemas;
  }
});
const Footer = AuthenticatorFooter.extend({
  links: function () {
    let links = AuthenticatorFooter.prototype.links.apply(this, arguments);
    links = getForgotPasswordLink(this.options.appState, this.options.settings).concat(links);
    return links;
  }
});
var ChallengeAuthenticatorPasswordView = BaseAuthenticatorView.extend({
  Body: Body,
  Footer: Footer
});

export { ChallengeAuthenticatorPasswordView as default };
//# sourceMappingURL=ChallengeAuthenticatorPasswordView.js.map
