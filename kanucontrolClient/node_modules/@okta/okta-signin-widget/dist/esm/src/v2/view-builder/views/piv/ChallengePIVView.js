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
import PIVHeader from './PIVHeader.js';

const Body = BaseForm.extend({
  className: 'piv-cac-card',
  modelEvents: {
    request: 'startAuthentication',
    error: 'stopAuthentication'
  },
  initialize: function () {
    BaseForm.prototype.initialize.apply(this, arguments);
    this.model.set('useRedirect', true);
    this.addInstructions();
  },
  title: function () {
    return loc('piv.cac.title', 'login');
  },
  save: function () {
    return loc('retry', 'login');
  },
  addInstructions: function () {
    this.add(`<div class='piv-verify-text'>
        <p>${loc('piv.cac.card.insert', 'login')}</p>
        <div data-se='piv-waiting' class='okta-waiting-spinner'></div>
      </div>`);
  },
  startAuthentication: function () {
    this.$('.okta-waiting-spinner').show();
    this.$('.o-form-button-bar').hide();
  },
  stopAuthentication: function () {
    this.$('.okta-waiting-spinner').hide();
    this.$('.o-form-button-bar').show();
  }
});
var ChallengePIVView = BaseAuthenticatorView.extend({
  Header: PIVHeader,
  Body: Body,
  postRender: function () {
    BaseAuthenticatorView.prototype.postRender.apply(this, arguments);
    const messages = this.options.appState.get('messages') || {}; // if piv view has errors, show errors and stop authentication.
    // otherwise trigger authentication on page load

    if (Array.isArray(messages.value)) {
      this.form.stopAuthentication();
    } else {
      this.form.startAuthentication();
      this.form.trigger('save', this.model);
    }
  }
});

export { ChallengePIVView as default };
//# sourceMappingURL=ChallengePIVView.js.map
