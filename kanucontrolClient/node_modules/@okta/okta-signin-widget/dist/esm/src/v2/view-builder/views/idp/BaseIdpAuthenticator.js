import '../../internals/BaseHeader.js';
import '../../internals/BaseFooter.js';
import BaseForm from '../../internals/BaseForm.js';
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

const BaseIdPAuthenticatorBody = BaseForm.extend({
  initialize: function () {
    BaseForm.prototype.initialize.apply(this, arguments);
    this.model.set('useRedirect', true);
    this.add('<div class="okta-waiting-spinner"></div>');
  }
});
const BaseIdpAuthenticatorView = BaseAuthenticatorView.extend({
  postRender: function () {
    BaseAuthenticatorView.prototype.postRender.apply(this, arguments); // get the error messages

    const messages = this.options.appState.get('messages') || {}; // In case of failure, don't auto-redirect which will result in infinite redirects.
    // so catch the error and render to the user.

    if (this.settings.get('features.skipIdpFactorVerificationBtn') && !Array.isArray(messages.value)) {
      this.$('.o-form-button-bar').hide();
      this.$('.okta-waiting-spinner').show();
      this.form.trigger('save', this.model);
    } else {
      this.$('.okta-waiting-spinner').hide();
      this.$('.o-form-button-bar').show();
    }
  }
});

export { BaseIdPAuthenticatorBody, BaseIdpAuthenticatorView };
//# sourceMappingURL=BaseIdpAuthenticator.js.map
