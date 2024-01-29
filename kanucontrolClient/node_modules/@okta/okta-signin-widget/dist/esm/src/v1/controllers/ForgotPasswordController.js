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
import Enums from '../../util/Enums.js';
import FormController from '../util/FormController.js';
import FormType from '../util/FormType.js';
import Util from '../../util/Util.js';
import fn from '../util/ValidationUtil.js';
import ContactSupport from '../views/shared/ContactSupport.js';
import TextBox from '../views/shared/TextBox.js';

const ForgotPasswordControllernoFactorsError = View.extend({
  template: _Handlebars2.template({
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
      var lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<div class=\"okta-form-infobox-error infobox infobox-error\" role=\"alert\"><span class=\"icon error-16\"></span><p>" + container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "password.forgot.noFactorsEnabled"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 110
          },
          "end": {
            "line": 1,
            "column": 173
          }
        }
      })) + "</p></div>";
    },
    "useData": true
  })
});
const ForgotPasswordControllerFooter = View.extend({
  template: _Handlebars2.template({
    "1": function (container, depth0, helpers, partials, data) {
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
          "code": "goback"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 94
          },
          "end": {
            "line": 1,
            "column": 131
          }
        }
      })) + "</a>";
    },
    "3": function (container, depth0, helpers, partials, data) {
      var lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<a href=\"#\" class=\"link goto js-contact-support\">" + container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "mfa.noAccessToEmail"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 220
          },
          "end": {
            "line": 1,
            "column": 270
          }
        }
      })) + "</a>";
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

      return ((stack1 = lookupProperty(helpers, "unless").call(alias1, depth0 != null ? lookupProperty(depth0, "hideBackToSignInForReset") : depth0, {
        "name": "unless",
        "hash": {},
        "fn": container.program(1, data, 0),
        "inverse": container.noop,
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 0
          },
          "end": {
            "line": 1,
            "column": 146
          }
        }
      })) != null ? stack1 : "") + ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "helpSupportNumber") : depth0, {
        "name": "if",
        "hash": {},
        "fn": container.program(3, data, 0),
        "inverse": container.noop,
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 146
          },
          "end": {
            "line": 1,
            "column": 281
          }
        }
      })) != null ? stack1 : "");
    },
    "useData": true
  }),
  className: 'auth-footer',
  events: {
    'click .js-back': function (e) {
      e.preventDefault();
      this.back();
    },
    'click .js-contact-support': function (e) {
      e.preventDefault();
      this.state.trigger('contactSupport');
      this.$('.js-contact-support').hide();
    }
  },
  getTemplateData: function () {
    return {
      hideBackToSignInForReset: this.settings.get('features.hideBackToSignInForReset'),
      helpSupportNumber: this.settings.get('helpSupportNumber')
    };
  },
  back: function () {
    this.state.set('navigateDir', Enums.DIRECTION_BACK);
    this.options.appState.trigger('navigate', '');
  }
});
var ForgotPasswordController = FormController.extend({
  className: 'forgot-password',
  Model: {
    props: {
      username: ['string', true],
      factorType: ['string', true]
    },
    validate: function () {
      return fn.validateUsername(this);
    },
    save: function () {
      const self = this;
      const relayState = this.settings.get('relayState');
      this.startTransaction(function (authClient) {
        return authClient.forgotPassword({
          username: self.settings.transformUsername(self.get('username'), Enums.FORGOT_PASSWORD),
          factorType: self.get('factorType'),
          relayState: relayState
        });
      }).catch(function () {//need empty fail handler on model to display errors on form
      });
    }
  },
  Form: {
    noButtonBar: true,
    title: oktaUnderscore.partial(loc, 'password.reset', 'login'),
    formChildren: function () {
      const smsEnabled = this.settings.get('features.smsRecovery');
      /*eslint complexity: [2, 9] max-statements: [2, 23] */

      const callEnabled = this.settings.get('features.callRecovery');
      const emailEnabled = this.settings.get('features.emailRecovery');
      const noFactorsEnabled = !(smsEnabled || callEnabled || emailEnabled);
      const formChildren = [];

      if (noFactorsEnabled) {
        this.add(ForgotPasswordControllernoFactorsError, '.o-form-error-container');
      } else {
        formChildren.push(FormType.Input({
          label: loc('password.forgot.email.or.username.placeholder', 'login'),
          'label-top': true,
          explain: Util.createInputExplain('password.forgot.email.or.username.tooltip', 'password.forgot.email.or.username.placeholder', 'login'),
          'explain-top': true,
          name: 'username',
          input: TextBox,
          inputId: 'account-recovery-username',
          autoComplete: 'username',
          type: 'text',
          inlineValidation: false
        }));

        if (smsEnabled || callEnabled) {
          formChildren.push(FormType.View({
            View: View.extend({
              template: _Handlebars2.template({
                "compiler": [8, ">= 4.3.0"],
                "main": function (container, depth0, helpers, partials, data) {
                  var lookupProperty = container.lookupProperty || function (parent, propertyName) {
                    if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
                      return parent[propertyName];
                    }

                    return undefined;
                  };

                  return "<p class=\"mobile-recovery-hint\">" + container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
                    "name": "i18n",
                    "hash": {
                      "arguments": "mobileFactors",
                      "bundle": "login",
                      "code": "recovery.mobile.hint"
                    },
                    "data": data,
                    "loc": {
                      "start": {
                        "line": 1,
                        "column": 32
                      },
                      "end": {
                        "line": 1,
                        "column": 109
                      }
                    }
                  })) + "</p>";
                },
                "useData": true
              }),
              getTemplateData: function () {
                let mobileFactors;

                if (smsEnabled && callEnabled) {
                  mobileFactors = loc('recovery.smsOrCall');
                } else if (callEnabled) {
                  mobileFactors = loc('recovery.call');
                } else {
                  mobileFactors = loc('recovery.sms');
                }

                return {
                  mobileFactors: mobileFactors
                };
              }
            })
          }));
        }

        if (smsEnabled) {
          this.$el.addClass('forgot-password-sms-enabled');
          formChildren.push(this.createRecoveryFactorButton('sms-button', 'password.forgot.sendText', Enums.RECOVERY_FACTOR_TYPE_SMS, this));
          this.setDefaultFactorType(Enums.RECOVERY_FACTOR_TYPE_SMS);
        }

        if (callEnabled) {
          this.$el.addClass('forgot-password-call-enabled');
          formChildren.push(this.createRecoveryFactorButton('call-button', 'password.forgot.call', Enums.RECOVERY_FACTOR_TYPE_CALL, this));
          this.setDefaultFactorType(Enums.RECOVERY_FACTOR_TYPE_CALL);
        }

        if (emailEnabled) {
          this.$el.addClass('forgot-password-email-enabled');
          formChildren.push(this.createRecoveryFactorButton('email-button', 'password.forgot.sendEmail', Enums.RECOVERY_FACTOR_TYPE_EMAIL, this));
          this.setDefaultFactorType(Enums.RECOVERY_FACTOR_TYPE_EMAIL);
        }
      }

      return formChildren;
    },
    initialize: function () {
      this.listenTo(this.state, 'contactSupport', function () {
        this.add(ContactSupport, '.o-form-error-container');
      });
      this.listenTo(this, 'save', function () {
        this.options.appState.set('username', this.model.get('username'));
        this.model.save();
      });
    },
    setDefaultFactorType: function (factorType) {
      if (oktaUnderscore.isEmpty(this.model.get('factorType'))) {
        this.model.set('factorType', factorType);
      }
    },
    createRecoveryFactorButton: function (className, labelCode, factorType, form) {
      return FormType.Button({
        attributes: {
          'data-se': className
        },
        className: 'button button-primary button-wide ' + className,
        title: loc(labelCode, 'login'),
        click: function () {
          form.clearErrors();

          if (this.model.isValid()) {
            this.model.set('factorType', factorType);
            form.trigger('save', this.model);
          }
        }
      });
    }
  },
  Footer: ForgotPasswordControllerFooter,
  initialize: function () {
    this.options.appState.unset('username');
  }
});

export { ForgotPasswordController as default };
//# sourceMappingURL=ForgotPasswordController.js.map
