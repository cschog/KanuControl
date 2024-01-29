import BaseAuthenticatorEmailView from './BaseAuthenticatorEmailView.js';
import { getEnterCodeLink, getCheckYourEmailTitle } from './AuthenticatorEmailViewUtil.js';

const BaseAuthenticatorEmailForm = BaseAuthenticatorEmailView.prototype.Body;
const Body = BaseAuthenticatorEmailForm.extend(Object.assign({
  noButtonBar: true,
  resendEmailAction: 'currentAuthenticatorEnrollment-resend',
  events: {
    'click .enter-auth-code-instead-link': 'showAuthCodeEntry'
  },
  initialize: function () {
    var _this$options$current, _this$options$current2;

    BaseAuthenticatorEmailForm.prototype.initialize.apply(this, arguments);
    const {
      email: email
    } = ((_this$options$current = this.options.currentViewState.relatesTo) === null || _this$options$current === void 0 ? void 0 : (_this$options$current2 = _this$options$current.value) === null || _this$options$current2 === void 0 ? void 0 : _this$options$current2.profile) || {};
    const useEmailMagicLinkValue = this.isUseEmailMagicLink();

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
  },
  postRender: function () {
    BaseAuthenticatorEmailForm.prototype.postRender.apply(this, arguments);

    if (this.isUseEmailMagicLink()) {
      this.showCodeEntryField(false);
    } else {
      this.noButtonBar = false;
    }
  },
  isUseEmailMagicLink: function () {
    var _this$options$appStat, _this$options$appStat2;

    const useEmailMagicLink = (_this$options$appStat = this.options.appState.get('currentAuthenticatorEnrollment')) === null || _this$options$appStat === void 0 ? void 0 : (_this$options$appStat2 = _this$options$appStat.contextualData) === null || _this$options$appStat2 === void 0 ? void 0 : _this$options$appStat2.useEmailMagicLink;
    return useEmailMagicLink !== undefined ? useEmailMagicLink : true;
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
var ChallengeAuthenticatorEmailView = BaseAuthenticatorEmailView.extend({
  Body: Body
});

export { ChallengeAuthenticatorEmailView as default };
//# sourceMappingURL=ChallengeAuthenticatorEmailView.js.map
