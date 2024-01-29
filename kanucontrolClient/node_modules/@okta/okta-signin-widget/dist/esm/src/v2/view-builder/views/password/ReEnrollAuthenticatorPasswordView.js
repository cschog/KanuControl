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
import EnrollAuthenticatorPasswordView from './EnrollAuthenticatorPasswordView.js';

const Body = EnrollAuthenticatorPasswordView.prototype.Body.extend({
  className: 'password-authenticator',
  title: function () {
    const title = this.options.settings.get('brandName') ? loc('password.expired.title.specific', 'login', [this.options.settings.get('brandName')]) : loc('password.expired.title.generic', 'login');
    return title;
  },
  save: function () {
    return loc('password.expired.submit', 'login');
  },
  getPasswordPolicySettings: function () {
    var _this$options$appStat, _this$options$appStat2;

    return ((_this$options$appStat = this.options.appState.get('recoveryAuthenticator')) === null || _this$options$appStat === void 0 ? void 0 : _this$options$appStat.settings) || ((_this$options$appStat2 = this.options.appState.get('enrollmentAuthenticator')) === null || _this$options$appStat2 === void 0 ? void 0 : _this$options$appStat2.settings);
  }
});
var ReEnrollAuthenticatorPasswordView = EnrollAuthenticatorPasswordView.extend({
  Body: Body
});

export { ReEnrollAuthenticatorPasswordView as default };
//# sourceMappingURL=ReEnrollAuthenticatorPasswordView.js.map
