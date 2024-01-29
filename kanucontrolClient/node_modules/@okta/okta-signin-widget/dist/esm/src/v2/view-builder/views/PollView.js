import _Handlebars2 from '../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { View, loc } from '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import '../internals/BaseHeader.js';
import '../internals/BaseFooter.js';
import BaseForm from '../internals/BaseForm.js';
import '../internals/BaseFormWithPolling.js';
import '../internals/BaseOktaVerifyChallengeView.js';
import BaseView from '../internals/BaseView.js';
import '../components/AuthenticatorEnrollOptions.js';
import '../components/AuthenticatorVerifyOptions.js';
import '../../../util/FactorUtil.js';
import '../../../v1/views/admin-consent/ScopeList.js';
import '../../../v1/views/consent/ScopeList.js';
import './captcha/CaptchaView.js';
import { MS_PER_SEC } from '../utils/Constants.js';

const PollMessageView = View.extend({
  template: _Handlebars2.template({
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
      var lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<div class=\"ion-messages-container\"><p>" + container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
        "name": "i18n",
        "hash": {
          "$1": "<span class='strong'>$1</span>",
          "arguments": "countDownCounterValue",
          "bundle": "login",
          "code": "poll.form.message"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 39
          },
          "end": {
            "line": 1,
            "column": 157
          }
        }
      })) + "</p></div><div class=\"hide okta-waiting-spinner\"></div>";
    },
    "useData": true
  }),
  getTemplateData: function () {
    const countDownCounterValue = this.options;
    return {
      countDownCounterValue: countDownCounterValue
    };
  }
});
const Body = BaseForm.extend(Object.assign({
  title: function () {
    return loc('poll.form.title', 'login');
  },
  noButtonBar: true,
  initialize: function () {
    BaseForm.prototype.initialize.apply(this, arguments);
    const refreshInterval = this.options.appState.getCurrentViewState().refresh;
    this.refreshTimeout = setTimeout(() => {
      this.$el.find('.okta-waiting-spinner').show(); // start after a small delay so that the spinner does not get hidden too soon

      setTimeout(() => this.saveForm(this.model), 200);
    }, refreshInterval);
  },
  render: function () {
    BaseForm.prototype.render.apply(this, arguments);
    this.countDownCounterValue = Math.ceil(this.options.appState.getCurrentViewState().refresh / MS_PER_SEC);
    this.add(new PollMessageView(this.countDownCounterValue));
    this.startCountDown('.ion-messages-container span', MS_PER_SEC);
  },
  remove: function () {
    BaseForm.prototype.remove.apply(this, arguments);
    clearTimeout(this.refreshTimeout);
  },
  triggerAfterError: function () {
    BaseForm.prototype.triggerAfterError.apply(this, arguments);
    clearTimeout(this.refreshTimeout);
    this.stopCountDown();
    this.$el.find('.o-form-fieldset-container').empty();
  },
  startCountDown: function (selector, interval) {
    if (this.countDown) {
      clearInterval(this.countDown);
    }

    this.counterEl = this.$el.find(selector);
    this.countDown = setInterval(() => {
      if (this.counterEl.text() !== '0') {
        this.counterEl.text(this.counterEl.text() - 1);
      }
    }, interval, this);
  },
  stopCountDown: function () {
    if (this.countDown) {
      clearInterval(this.countDown);
    }
  }
}));
var PollView = BaseView.extend({
  Body: Body
});

export { PollView as default };
//# sourceMappingURL=PollView.js.map
