import _Handlebars2 from '../../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { View, loc, createButton } from '../../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import { AUTHENTICATOR_KEY } from '../../../ion/RemediationConstants.js';
import '../../../../v1/views/admin-consent/ScopeList.js';
import '../../../../v1/views/consent/ScopeList.js';
import '../captcha/CaptchaView.js';
import polling from './polling.js';
import { WARNING_TIMEOUT } from '../../utils/Constants.js';
import BaseAuthenticatorView from '../../components/BaseAuthenticatorView.js';

const warningTemplate = View.extend({
  className: 'okta-form-infobox-warning infobox infobox-warning',
  template: _Handlebars2.template({
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
      var helper,
          lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<span class=\"icon warning-16\"></span><p>" + container.escapeExpression((helper = (helper = lookupProperty(helpers, "warning") || (depth0 != null ? lookupProperty(depth0, "warning") : depth0)) != null ? helper : container.hooks.helperMissing, typeof helper === "function" ? helper.call(depth0 != null ? depth0 : container.nullContext || {}, {
        "name": "warning",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 40
          },
          "end": {
            "line": 1,
            "column": 51
          }
        }
      }) : helper)) + "</p>";
    },
    "useData": true
  })
});
const Body = BaseFormWithPolling.extend(Object.assign({
  noButtonBar: true,
  title: function () {
    return this.isOV() ? loc('oie.okta_verify.push.title', 'login') : loc('oie.verify.custom_app.title', 'login', [this.options.appState.getAuthenticatorDisplayName()]);
  },
  initialize: function () {
    BaseFormWithPolling.prototype.initialize.apply(this, arguments); // 'hasSavingState' would be true by default.
    // Setting it to false when auth key is okta_verify or custom_app with autochallenge schema
    // So that 'o-form-saving' css class is not added while polling and checkbox remains enabled.

    if ((this.isOV() || this.isCustomApp()) && this.isAutoChallengeSupported()) {
      this.hasSavingState = false;
    }

    this.listenTo(this.model, 'error', this.stopPoll);
    this.addView();
  },
  addView: function () {
    this.add(createButton({
      className: 'button button-wide button-primary send-push link-button-disabled',
      title: this.isOV() ? loc('oie.okta_verify.push.sent', 'login') : loc('oie.custom_app.push.sent', 'login'),
      click: e => {
        e.preventDefault();
      }
    }));
    this.add(`<span class='accessibility-text' role='alert'>
        ${this.isOV() ? loc('oie.okta_verify.push.sent', 'login') : loc('oie.custom_app.push.sent', 'login')}</span>`);
  },
  render: function () {
    BaseFormWithPolling.prototype.render.apply(this, arguments);
    const checkbox = this.$el.find('[data-se="o-form-fieldset-autoChallenge"]');

    if (!this.isAutoChallengeSupported()) {
      checkbox.length && checkbox.hide();
    } else if (this.isOV() || this.isCustomApp()) {
      // Move checkbox below the button
      // Checkbox is rendered by BaseForm using remediation response and
      // hence by default always gets added above buttons.
      checkbox.length && this.$el.find('.o-form-fieldset-container').append(checkbox);
    }
  },
  postRender: function () {
    BaseFormWithPolling.prototype.postRender.apply(this, arguments);
    const className = this.isOV() ? 'okta-verify-push-challenge' : ' custom-app-push-challenge';
    this.$el.addClass(className);
    this.startPoll();
  },
  startPoll: function () {
    this.startPolling();
    this.warningTimeout = setTimeout(() => {
      const warningText = this.isOV() ? loc('oktaverify.warning', 'login') : loc('oie.custom_app.push.warning', 'login', [this.options.appState.getAuthenticatorDisplayName()]);
      this.showWarning(warningText);
    }, WARNING_TIMEOUT);
  },
  stopPoll: function () {
    this.stopPolling();
    this.clearWarning();
  },
  showWarning: function (msg) {
    this.clearWarning();
    this.add(warningTemplate, '.o-form-error-container', {
      options: {
        warning: msg
      }
    });
  },
  clearWarning: function () {
    if (this.$('.o-form-error-container div').hasClass('okta-form-infobox-warning')) {
      this.$('.okta-form-infobox-warning').remove();
    }

    clearTimeout(this.warningTimeout);
  },
  remove: function () {
    BaseFormWithPolling.prototype.remove.apply(this, arguments);
    this.stopPolling();
  },
  isOV: function () {
    return this.options.appState.get('authenticatorKey') === AUTHENTICATOR_KEY.OV;
  },
  isCustomApp: function () {
    return this.options.appState.get('authenticatorKey') === AUTHENTICATOR_KEY.CUSTOM_APP;
  },
  isAutoChallengeSupported: function () {
    return this.options.appState.getSchemaByName('autoChallenge') !== null && this.options.appState.getSchemaByName('autoChallenge') !== undefined;
  }
}, polling));
const AuthenticatorView = BaseAuthenticatorView.extend({
  Body: Body
});

export { Body, AuthenticatorView as default };
//# sourceMappingURL=ChallengePushView.js.map
