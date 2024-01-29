import _Handlebars2 from '../../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { View, createButton, loc } from '../../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import Util from '../../../../util/Util.js';
import Enums from '../../../../util/Enums.js';
import { UNIVERSAL_LINK_POST_DELAY } from '../../utils/Constants.js';
import { FORMS } from '../../../ion/RemediationConstants.js';
import { appendLoginHint } from '../../utils/ChallengeViewUtil.js';

var SignInWithDeviceOption = View.extend({
  className: 'sign-in-with-device-option',
  template: _Handlebars2.template({
    "1": function (container, depth0, helpers, partials, data) {
      return "<div class=\"signin-with-ov-description\"></div>";
    },
    "3": function (container, depth0, helpers, partials, data) {
      var lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<div class=\"separation-line\"><span>" + container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "authbutton.divider.text"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 201
          },
          "end": {
            "line": 1,
            "column": 255
          }
        }
      })) + "</span></div>";
    },
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
      var stack1,
          alias1 = depth0 != null ? depth0 : container.nullContext || {},
          lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<div class=\"okta-verify-container\">" + ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "signInWithDeviceIsRequired") : depth0, {
        "name": "if",
        "hash": {},
        "fn": container.program(1, data, 0),
        "inverse": container.noop,
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 35
          },
          "end": {
            "line": 1,
            "column": 122
          }
        }
      })) != null ? stack1 : "") + "</div>" + ((stack1 = lookupProperty(helpers, "unless").call(alias1, depth0 != null ? lookupProperty(depth0, "signInWithDeviceIsRequired") : depth0, {
        "name": "unless",
        "hash": {},
        "fn": container.program(3, data, 0),
        "inverse": container.noop,
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 128
          },
          "end": {
            "line": 1,
            "column": 279
          }
        }
      })) != null ? stack1 : "");
    },
    "useData": true
  }),
  initialize: function () {
    var _deviceChallengePollR;

    const appState = this.options.appState;
    const deviceChallengePollRemediation = appState.hasRemediationObject(FORMS.LAUNCH_AUTHENTICATOR);
    const deviceChallenge = deviceChallengePollRemediation === null || deviceChallengePollRemediation === void 0 ? void 0 : (_deviceChallengePollR = deviceChallengePollRemediation.relatesTo) === null || _deviceChallengePollR === void 0 ? void 0 : _deviceChallengePollR.value;
    this.add(createButton({
      className: 'button',
      icon: 'okta-verify-authenticator',
      title: loc('oktaVerify.button', 'login'),
      click: function () {
        if (this.settings.get('features.engFastpassMultipleAccounts') && this.model.get('identifier')) {
          this.options.settings.set('identifier', encodeURIComponent(this.model.get('identifier')));
        }

        const isUVapproach = (deviceChallenge === null || deviceChallenge === void 0 ? void 0 : deviceChallenge.challengeMethod) === Enums.UNIVERSAL_LINK_CHALLENGE;

        if (isUVapproach) {
          var _this$options, _this$options$setting;

          // launch the Okta Verify app
          let deviceChallengeUrl = appendLoginHint(deviceChallenge.href, (_this$options = this.options) === null || _this$options === void 0 ? void 0 : (_this$options$setting = _this$options.settings) === null || _this$options$setting === void 0 ? void 0 : _this$options$setting.get('identifier'));
          Util.redirect(deviceChallengeUrl);
        }

        const isAppLinkapproach = (deviceChallenge === null || deviceChallenge === void 0 ? void 0 : deviceChallenge.challengeMethod) === Enums.APP_LINK_CHALLENGE;

        if (isAppLinkapproach) {
          var _this$options2, _this$options2$settin;

          // launch the Okta Verify app
          let deviceChallengeUrl = appendLoginHint(deviceChallenge.href, (_this$options2 = this.options) === null || _this$options2 === void 0 ? void 0 : (_this$options2$settin = _this$options2.settings) === null || _this$options2$settin === void 0 ? void 0 : _this$options2$settin.get('identifier'));
          Util.redirect(deviceChallengeUrl, window, true);
        } // OKTA-350084
        // For the universal link (iOS) and app link (Android) approach,
        // we need to 1. launch the Okta Verify app
        // and 2. take the enduser to the next step right away
        // In Safari, when Okta Verify app is not installed, step 1 responds with error immediately,
        // then step 2 will respond with error.
        // To avoid showing the error right before switching to the next UI,
        // wait for 500 milliseconds before invoking step 2


        Util.callAfterTimeout(() => {
          if (this.options.isRequired) {
            appState.trigger('saveForm', this.model);
          } else {
            appState.trigger('invokeAction', FORMS.LAUNCH_AUTHENTICATOR);
          }
        }, isUVapproach || isAppLinkapproach ? UNIVERSAL_LINK_POST_DELAY : 0);
      }
    }), '.okta-verify-container');
  },
  getTemplateData: function () {
    return {
      signInWithDeviceIsRequired: !!this.options.isRequired
    };
  },
  postRender: function () {
    if (this.options.isRequired) {
      var _this$options$appStat, _this$options$appStat2;

      const appLabel = (_this$options$appStat = this.options.appState.attributes) === null || _this$options$appStat === void 0 ? void 0 : (_this$options$appStat2 = _this$options$appStat.app) === null || _this$options$appStat2 === void 0 ? void 0 : _this$options$appStat2.label;
      const resourceLabel = appLabel ? loc('oktaVerify.appDescription', 'login', [appLabel]) : loc('oktaVerify.description', 'login');
      const ovDescContainer = this.$el.find('.signin-with-ov-description');
      ovDescContainer.text(resourceLabel);
    }
  }
});

export { SignInWithDeviceOption as default };
//# sourceMappingURL=SignInWithDeviceOption.js.map
