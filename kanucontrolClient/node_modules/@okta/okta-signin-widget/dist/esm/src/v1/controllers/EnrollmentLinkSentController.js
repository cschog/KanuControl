import _Handlebars2 from '../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { View, loc } from '../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import oktaUnderscore from '../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import fn$1 from '../../util/CountryUtil.js';
import FormController from '../util/FormController.js';
import FormType from '../util/FormType.js';
import fn from '../util/RouterUtil.js';

const PUSH_INTERVAL = 6000; // Note: Keep-alive is set to 5 seconds - using 5 seconds here will result
// in network connection lost errors in Safari and IE.

const EnrollmentLinkSentControllerFooter = View.extend({
  template: _Handlebars2.template({
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
      var lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<a href=\"#\" class=\"link help js-back\" data-se=\"back-link\">" + container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "oform.back"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 58
          },
          "end": {
            "line": 1,
            "column": 99
          }
        }
      })) + "</a>";
    },
    "useData": true
  }),
  className: 'auth-footer',
  events: {
    'click .js-back': function (e) {
      e.preventDefault();
      this.back();
    }
  },
  back: function () {
    const url = fn.createActivateFactorUrl(this.options.appState.get('activatedFactorProvider'), this.options.appState.get('activatedFactorType'), 'manual');
    this.options.appState.trigger('navigate', url);
  }
});
const emailSentForm = {
  title: oktaUnderscore.partial(loc, 'enroll.totp.enrollViaEmail.title', 'login'),
  noButtonBar: true,
  attributes: {
    'data-se': 'sent-email-activation-link'
  },
  formChildren: [FormType.View({
    View: View.extend({
      template: _Handlebars2.template({
        "compiler": [8, ">= 4.3.0"],
        "main": function (container, depth0, helpers, partials, data) {
          var helper,
              alias1 = depth0 != null ? depth0 : container.nullContext || {},
              alias2 = container.hooks.helperMissing,
              alias3 = container.escapeExpression,
              lookupProperty = container.lookupProperty || function (parent, propertyName) {
            if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
              return parent[propertyName];
            }

            return undefined;
          };

          return "<p>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
            "name": "i18n",
            "hash": {
              "bundle": "login",
              "code": "enroll.totp.enrollViaEmail.msg"
            },
            "data": data,
            "loc": {
              "start": {
                "line": 1,
                "column": 3
              },
              "end": {
                "line": 1,
                "column": 64
              }
            }
          })) + "</p><p class=\"email-address\">" + alias3((helper = (helper = lookupProperty(helpers, "email") || (depth0 != null ? lookupProperty(depth0, "email") : depth0)) != null ? helper : alias2, typeof helper === "function" ? helper.call(alias1, {
            "name": "email",
            "hash": {},
            "data": data,
            "loc": {
              "start": {
                "line": 1,
                "column": 93
              },
              "end": {
                "line": 1,
                "column": 102
              }
            }
          }) : helper)) + "</p>";
        },
        "useData": true
      }),
      getTemplateData: function () {
        return {
          email: this.options.appState.get('userEmail')
        };
      }
    })
  })]
};
const smsSentForm = {
  title: oktaUnderscore.partial(loc, 'enroll.totp.enrollViaSms.title', 'login'),
  noButtonBar: true,
  attributes: {
    'data-se': 'sent-sms-activation-link'
  },
  formChildren: [FormType.View({
    View: View.extend({
      template: _Handlebars2.template({
        "compiler": [8, ">= 4.3.0"],
        "main": function (container, depth0, helpers, partials, data) {
          var helper,
              alias1 = depth0 != null ? depth0 : container.nullContext || {},
              alias2 = container.hooks.helperMissing,
              alias3 = container.escapeExpression,
              lookupProperty = container.lookupProperty || function (parent, propertyName) {
            if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
              return parent[propertyName];
            }

            return undefined;
          };

          return "<p>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
            "name": "i18n",
            "hash": {
              "bundle": "login",
              "code": "enroll.totp.enrollViaSms.msg"
            },
            "data": data,
            "loc": {
              "start": {
                "line": 1,
                "column": 3
              },
              "end": {
                "line": 1,
                "column": 62
              }
            }
          })) + "</p><p class=\"phone-number\">" + alias3((helper = (helper = lookupProperty(helpers, "phoneNumber") || (depth0 != null ? lookupProperty(depth0, "phoneNumber") : depth0)) != null ? helper : alias2, typeof helper === "function" ? helper.call(alias1, {
            "name": "phoneNumber",
            "hash": {},
            "data": data,
            "loc": {
              "start": {
                "line": 1,
                "column": 90
              },
              "end": {
                "line": 1,
                "column": 105
              }
            }
          }) : helper)) + "</p>";
        },
        "useData": true
      }),
      getTemplateData: function () {
        return {
          phoneNumber: this.model.get('fullPhoneNumber')
        };
      }
    })
  })]
};
var EnrollmentLinkSentController = FormController.extend({
  className: 'enroll-activation-link-sent',
  Model: function () {
    return {
      local: {
        countryCode: ['string', false, this.options.appState.get('userCountryCode')],
        phoneNumber: ['string', false, this.options.appState.get('userPhoneNumber')],
        __factorType__: ['string', false, this.options.factorType],
        __provider__: ['string', false, this.options.provider]
      },
      derived: {
        countryCallingCode: {
          deps: ['countryCode'],
          fn: function (countryCode) {
            return '+' + fn$1.getCallingCodeForCountry(countryCode);
          }
        },
        fullPhoneNumber: {
          deps: ['countryCallingCode', 'phoneNumber'],
          fn: function (countryCallingCode, phoneNumber) {
            return countryCallingCode + phoneNumber;
          }
        }
      }
    };
  },
  Form: function () {
    const activationType = this.options.appState.get('factorActivationType');

    switch (activationType) {
      case 'SMS':
        return smsSentForm;

      case 'EMAIL':
        return emailSentForm;

      default:
        throw new Error('Unknown activation option: ' + activationType);
    }
  },
  Footer: EnrollmentLinkSentControllerFooter,
  initialize: function () {
    this.pollForEnrollment();
  },
  remove: function () {
    return FormController.prototype.remove.apply(this, arguments);
  },
  pollForEnrollment: function () {
    return this.model.doTransaction(function (transaction) {
      return transaction.poll(PUSH_INTERVAL);
    });
  },
  trapAuthResponse: function () {
    if (this.options.appState.get('isWaitingForActivation')) {
      this.pollForEnrollment();
      return true;
    }
  }
});

export { EnrollmentLinkSentController as default };
//# sourceMappingURL=EnrollmentLinkSentController.js.map
