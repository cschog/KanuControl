import _Handlebars2 from '../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { View, Form, loc } from '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import oktaUnderscore from '../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import Util from '../../../util/Util.js';
import NumberChallengeView from './NumberChallengeView.js';

const titleTpl = _Handlebars2.template({
  "compiler": [8, ">= 4.3.0"],
  "main": function (container, depth0, helpers, partials, data) {
    var stack1,
        helper,
        alias1 = depth0 != null ? depth0 : container.nullContext || {},
        alias2 = container.hooks.helperMissing,
        alias3 = "function",
        lookupProperty = container.lookupProperty || function (parent, propertyName) {
      if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
        return parent[propertyName];
      }

      return undefined;
    };

    return container.escapeExpression((helper = (helper = lookupProperty(helpers, "factorName") || (depth0 != null ? lookupProperty(depth0, "factorName") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
      "name": "factorName",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 0
        },
        "end": {
          "line": 1,
          "column": 14
        }
      }
    }) : helper)) + " (" + ((stack1 = (helper = (helper = lookupProperty(helpers, "deviceName") || (depth0 != null ? lookupProperty(depth0, "deviceName") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
      "name": "deviceName",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 16
        },
        "end": {
          "line": 1,
          "column": 32
        }
      }
    }) : helper)) != null ? stack1 : "") + ")";
  },
  "useData": true
}); // deviceName is escaped on BaseForm (see BaseForm's template)


const WARNING_TIMEOUT = 30000; // milliseconds

const PushFormwarningTemplate = View.extend({
  className: 'okta-form-infobox-warning infobox infobox-warning',
  attributes: {
    'aria-live': 'polite'
  },
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
var PushForm = Form.extend({
  className: 'mfa-verify-push',
  autoSave: true,
  noCancelButton: true,
  save: oktaUnderscore.partial(loc, 'oktaverify.send', 'login'),
  scrollOnError: false,
  layout: 'o-form-theme',
  attributes: {
    'data-se': 'factor-push'
  },
  events: {
    submit: 'submit'
  },
  initialize: function () {
    this.enabled = true;
    this.listenTo(this.options.appState, 'change:isMfaRejected', this.handleRejectStateChange);
    this.numberChallengeView = this.add(NumberChallengeView).last();
    this.listenTo(this.options.appState, 'change:isWaitingForNumberChallenge', function (state, isWaitingForNumberChallenge) {
      if (isWaitingForNumberChallenge || this.options.appState.get('lastAuthResponse').status === 'SUCCESS') {
        this.clearWarnings();
        this.$el.find('.button').hide();
        this.numberChallengeView.$el.show();
      } else {
        this.numberChallengeView.$el.hide();
        this.$el.find('.button').show();
      }
    });
    this.listenTo(this.options.appState, 'change:isMfaTimeout', function (state, isMfaTimeout) {
      this.setSubmitState(isMfaTimeout);

      if (isMfaTimeout) {
        this.showError(loc('oktaverify.timeout', 'login'));
      }
    });
    this.listenTo(this.options.appState, 'change:isMfaRequired', function (state, isMfaRequired) {
      if (isMfaRequired) {
        this.clearErrors();
        this.clearWarnings();
      }
    });
    this.title = titleTpl({
      factorName: this.model.get('factorLabel'),
      deviceName: this.model.get('deviceName')
    });
  },
  setSubmitState: function (ableToSubmit) {
    const button = this.$el.find('.button');
    const a11ySpan = this.$el.find('.accessibility-text');
    this.enabled = ableToSubmit;

    if (ableToSubmit) {
      button.removeClass('link-button-disabled');
      button.prop('value', loc('oktaverify.send', 'login'));
      button.prop('disabled', false);

      if (a11ySpan) {
        a11ySpan.remove();
      }
    } else {
      button.addClass('link-button-disabled');
      button.prop('value', loc('oktaverify.sent', 'login'));
      button.prop('disabled', true);
      this.add(`<span class='accessibility-text' role='alert'>${loc('oktaverify.sent', 'login')}</span>`);
    }
  },
  submit: function (e) {
    if (e !== undefined) {
      e.preventDefault();
    }

    if (this.enabled) {
      this.setSubmitState(false);
      this.doSave();
    }
  },
  postRender: function () {
    const factorsPolicyInfo = this.options.appState.get('factorsPolicyInfo');
    const id = this.model.get('id');
    const isAutoPushEnabled = this.settings.get('features.autoPush') && factorsPolicyInfo && factorsPolicyInfo[id] ? factorsPolicyInfo[id]['autoPushEnabled'] : false;

    if (isAutoPushEnabled) {
      this.model.set('autoPush', true); // bind after $el has been rendered, and trigger push once DOM is fully loaded

      oktaUnderscore.defer(oktaUnderscore.bind(this.submit, this));
    }
  },
  doSave: function () {
    let warningTimeout;
    this.clearErrors();
    this.clearWarnings();

    if (this.model.isValid()) {
      this.listenToOnce(this.model, 'error', function () {
        this.setSubmitState(true);
        this.clearWarnings();
        clearTimeout(warningTimeout);
      });
      this.trigger('save', this.model);
      warningTimeout = Util.callAfterTimeout(() => {
        if (!this.options.appState.get('isWaitingForNumberChallenge')) {
          this.showWarning(loc('oktaverify.warning', 'login'));
        }
      }, WARNING_TIMEOUT);
    }
  },
  showError: function (msg) {
    this.clearWarnings();
    this.model.trigger('error', this.model, {
      responseJSON: {
        errorSummary: msg
      }
    });
  },
  showWarning: function (msg) {
    this.clearWarnings();
    this.add(PushFormwarningTemplate, '.o-form-error-container', {
      options: {
        warning: msg
      }
    });
  },
  clearWarnings: function () {
    this.$('.okta-form-infobox-warning').remove();
  },
  handleRejectStateChange: function (state, isMfaRejected) {
    if (isMfaRejected) {
      this.setSubmitState(isMfaRejected);
      this.setRejectedErrorMessage();
    }
  },
  setRejectedErrorMessage: function () {
    // If rejection is due to outdated app, show error message per platform
    // else show user rejected message.
    if (this.options.appState.get('lastAuthResponse').factorResultMessage === 'OKTA_VERIFY_UPGRADE_REQUIRED') {
      if (this.options.appState.get('factor').profile.platform === 'IOS') {
        this.showError(loc('oktaverify.rejected.upgradeRequired.ios', 'login'));
      } else {
        this.showError(loc('oktaverify.rejected.upgradeRequired.android', 'login'));
      }
    } else {
      this.showError(loc('oktaverify.rejected', 'login'));
    }
  }
});

export { PushForm as default };
//# sourceMappingURL=PushForm.js.map
