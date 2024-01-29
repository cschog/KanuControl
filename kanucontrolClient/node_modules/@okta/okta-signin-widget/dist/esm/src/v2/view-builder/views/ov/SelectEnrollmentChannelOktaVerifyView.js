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
import oktaUnderscore from '../../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
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
import fn from '../../../../util/BrowserFeatures.js';

const Body = BaseForm.extend({
  title: function () {
    return fn.isAndroid() || fn.isIOS() ? loc('oie.enroll.okta_verify.setup.title', 'login') : loc('oie.enroll.okta_verify.select.channel.title', 'login');
  },
  getUISchema: function () {
    var _channelField$options;

    const schemas = BaseForm.prototype.getUISchema.apply(this, arguments); // filter selected channel

    const channelField = oktaUnderscore.find(schemas, schema => schema.name === 'authenticator.channel');

    channelField.options = oktaUnderscore.filter(channelField === null || channelField === void 0 ? void 0 : channelField.options, option => {
      var _this$options$appStat, _this$options$appStat2;

      return option.value !== ((_this$options$appStat = this.options.appState.get('currentAuthenticator')) === null || _this$options$appStat === void 0 ? void 0 : (_this$options$appStat2 = _this$options$appStat.contextualData) === null || _this$options$appStat2 === void 0 ? void 0 : _this$options$appStat2.selectedChannel);
    });
    channelField.value = (_channelField$options = channelField.options[0]) === null || _channelField$options === void 0 ? void 0 : _channelField$options.value;
    channelField.sublabel = null;
    this.model.set('authenticator.channel', channelField.value);
    const description = {
      View: loc('oie.enroll.okta_verify.select.channel.description', 'login'),
      selector: '.o-form-fieldset-container'
    };
    return [description, ...schemas];
  }
});
var SelectEnrollmentChannelOktaVerifyView = BaseAuthenticatorView.extend({
  Body: Body
});

export { SelectEnrollmentChannelOktaVerifyView as default };
//# sourceMappingURL=SelectEnrollmentChannelOktaVerifyView.js.map
