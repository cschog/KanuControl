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

const Body = BaseForm.extend({
  title: function () {
    return loc('oie.security.question.challenge.title', 'login');
  },
  save: function () {
    return loc('mfa.challenge.verify', 'login');
  },
  getUISchema: function () {
    const uiSchemas = BaseForm.prototype.getUISchema.apply(this, arguments);
    const questionKey = uiSchemas.filter(s => s.name.indexOf('questionKey') >= 0);
    const answer = uiSchemas.filter(s => s.name.indexOf('answer') >= 0);
    /**
     * At verify SQ case,
     * 'questionKey' is read-only hence it won't show up in UI anyway.
     * 'answer' has label 'answer' but apparently UI want to display the
     * actual question (label of `questionKey`) as better user experience.
     */

    if (questionKey.length === 1 && answer.length === 1) {
      answer[0].label = questionKey[0].label;
    }

    return uiSchemas;
  }
});
var ChallengeAuthenticatorSecurityQuestion = BaseAuthenticatorView.extend({
  Body: Body
});

export { ChallengeAuthenticatorSecurityQuestion as default };
//# sourceMappingURL=ChallengeAuthenticatorSecurityQuestion.js.map
