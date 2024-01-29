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
import BaseAuthenticatorEmailView from './BaseAuthenticatorEmailView.js';
import { getEnterCodeLink, getCheckYourEmailTitle } from './AuthenticatorEmailViewUtil.js';

const BaseAuthenticatorEmailForm = BaseAuthenticatorEmailView.prototype.Body;
const Body = BaseAuthenticatorEmailForm.extend(Object.assign({
  resendEmailAction: 'currentAuthenticator-resend',
  initialize: function () {
    var _this$options$appStat;

    BaseAuthenticatorEmailForm.prototype.initialize.apply(this, arguments);
    const email = ((_this$options$appStat = this.options.appState.get('user')) === null || _this$options$appStat === void 0 ? void 0 : _this$options$appStat.identifier) || {};
    const useEmailMagicLinkValue = this.isUseEmailMagicLink();

    if (useEmailMagicLinkValue !== undefined) {
      this.noButtonBar = true;
      this.events['click .enter-auth-code-instead-link'] = 'showAuthCodeEntry';

      if (useEmailMagicLinkValue) {
        this.add(getEnterCodeLink(), {
          prepend: true,
          selector: '.o-form-error-container'
        });
      }

      this.add(getCheckYourEmailTitle(), {
        prepend: true,
        selector: '.o-form-error-container',
        options: {
          email: email,
          useEmailMagicLinkValue: useEmailMagicLinkValue
        }
      });
    } else {
      this.subtitle = loc('oie.email.enroll.subtitle', 'login');
    }
  },
  postRender: function () {
    if (this.isUseEmailMagicLink() !== undefined) {
      BaseAuthenticatorEmailForm.prototype.postRender.apply(this, arguments);

      if (this.isUseEmailMagicLink()) {
        this.showCodeEntryField(false);
      } else {
        this.noButtonBar = false;
      }
    }
  },
  isUseEmailMagicLink: function () {
    var _this$options$appStat2, _this$options$appStat3;

    return (_this$options$appStat2 = this.options.appState.get('currentAuthenticator')) === null || _this$options$appStat2 === void 0 ? void 0 : (_this$options$appStat3 = _this$options$appStat2.contextualData) === null || _this$options$appStat3 === void 0 ? void 0 : _this$options$appStat3.useEmailMagicLink;
  },
  showAuthCodeEntry: function () {
    this.noButtonBar = false;
    this.render();
    this.showCodeEntryField(true);
    this.removeEnterAuthCodeInsteadLink();
  },
  showCodeEntryField: function (show = true) {
    const $textField = this.$el.find('.o-form-fieldset-container');
    $textField.toggle(show);
  },
  removeEnterAuthCodeInsteadLink: function () {
    this.$el.find('.enter-auth-code-instead-link').remove();
  }
}));
var EnrollAuthenticatorEmailView = BaseAuthenticatorEmailView.extend({
  Body: Body
});

export { EnrollAuthenticatorEmailView as default };
//# sourceMappingURL=EnrollAuthenticatorEmailView.js.map
