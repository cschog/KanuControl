import _Handlebars2 from '../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { loc, View } from '../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import FooterSignout from '../views/shared/FooterSignout.js';
import TextBox from '../views/shared/TextBox.js';

var RecoveryChallengeController = FormController.extend({
  className: 'recovery-challenge',
  Model: {
    props: {
      passCode: ['string', true]
    },
    local: {
      ableToResend: 'boolean'
    },
    resendCode: function () {
      // Note: This does not require a trapAuthResponse because Backbone's
      // router will not navigate if the url path is the same
      this.limitResending();
      return this.doTransaction(function (transaction) {
        return transaction.resend();
      });
    },
    limitResending: function () {
      this.set({
        ableToResend: false
      });

      oktaUnderscore.delay(oktaUnderscore.bind(this.set, this), Enums.API_RATE_LIMIT, {
        ableToResend: true
      });
    },
    save: function () {
      return this.doTransaction(function (transaction) {
        return transaction.verify({
          passCode: this.get('passCode')
        });
      });
    }
  },
  Form: {
    autoSave: true,
    save: oktaUnderscore.partial(loc, 'mfa.challenge.verify', 'login'),
    title: function () {
      if (this.options.appState.get('factorType') === Enums.RECOVERY_FACTOR_TYPE_CALL) {
        return loc('recoveryChallenge.call.title', 'login');
      } else {
        return loc('recoveryChallenge.sms.title', 'login');
      }
    },
    className: 'recovery-challenge',
    initialize: function () {
      this.listenTo(this.model, 'error', function () {
        this.clearErrors();
      });
    },
    formChildren: function () {
      return [FormType.Button({
        title: loc('mfa.resendCode', 'login'),
        attributes: {
          'data-se': 'resend-button'
        },
        className: 'button sms-request-button margin-top-30',
        click: function () {
          this.model.resendCode();
        },
        initialize: function () {
          this.listenTo(this.model, 'change:ableToResend', function (model, ableToResend) {
            if (ableToResend) {
              this.options.title = loc('mfa.resendCode', 'login');
              this.enable();
              this.render();
            } else {
              this.options.title = loc('mfa.sent', 'login');
              this.disable();
              this.render();
            }
          });
        }
      }), FormType.Input({
        label: loc('mfa.challenge.enterCode.placeholder', 'login'),
        'label-top': true,
        className: 'enroll-sms-phone',
        name: 'passCode',
        input: TextBox,
        type: 'text'
      })];
    }
  },
  events: {
    'click .send-email-link': function (e) {
      e.preventDefault();
      const settings = this.model.settings;
      const username = this.options.appState.get('username');
      const recoveryType = this.options.appState.get('recoveryType');
      this.model.startTransaction(function (authClient) {
        // The user could have landed here via the Forgot Password/Unlock Account flow
        switch (recoveryType) {
          case Enums.RECOVERY_TYPE_PASSWORD:
            return authClient.forgotPassword({
              username: settings.transformUsername(username, Enums.FORGOT_PASSWORD),
              factorType: Enums.RECOVERY_FACTOR_TYPE_EMAIL
            });

          case Enums.RECOVERY_TYPE_UNLOCK:
            return authClient.unlockAccount({
              username: settings.transformUsername(username, Enums.UNLOCK_ACCOUNT),
              factorType: Enums.RECOVERY_FACTOR_TYPE_EMAIL
            });

          default:
            return;
        }
      });
    }
  },
  initialize: function () {
    const recoveryType = this.options.appState.get('recoveryType');
    let sendEmailLink;

    switch (recoveryType) {
      case Enums.RECOVERY_TYPE_PASSWORD:
        sendEmailLink = _Handlebars2.template({
          "compiler": [8, ">= 4.3.0"],
          "main": function (container, depth0, helpers, partials, data) {
            var lookupProperty = container.lookupProperty || function (parent, propertyName) {
              if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
                return parent[propertyName];
              }

              return undefined;
            };

            return container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
              "name": "i18n",
              "hash": {
                "bundle": "login",
                "code": "password.forgot.code.notReceived"
              },
              "data": data,
              "loc": {
                "start": {
                  "line": 1,
                  "column": 0
                },
                "end": {
                  "line": 1,
                  "column": 63
                }
              }
            }));
          },
          "useData": true
        });
        break;

      case Enums.RECOVERY_TYPE_UNLOCK:
        sendEmailLink = _Handlebars2.template({
          "compiler": [8, ">= 4.3.0"],
          "main": function (container, depth0, helpers, partials, data) {
            var lookupProperty = container.lookupProperty || function (parent, propertyName) {
              if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
                return parent[propertyName];
              }

              return undefined;
            };

            return container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
              "name": "i18n",
              "hash": {
                "bundle": "login",
                "code": "account.unlock.code.notReceived"
              },
              "data": data,
              "loc": {
                "start": {
                  "line": 1,
                  "column": 0
                },
                "end": {
                  "line": 1,
                  "column": 62
                }
              }
            }));
          },
          "useData": true
        });
        break;
    }

    if (sendEmailLink && this.settings.get('features.emailRecovery')) {
      this.add(View.extend({
        className: 'link send-email-link',
        tagName: 'a',
        attributes: {
          href: '#',
          'data-se': 'send-email-link'
        },
        template: sendEmailLink
      }));
    }

    if (!this.settings.get('features.hideBackToSignInForReset')) {
      this.addFooter(FooterSignout);
    }
  },
  postRender: function () {
    this.model.limitResending();
  }
});

export { RecoveryChallengeController as default };
//# sourceMappingURL=RecoveryChallengeController.js.map
