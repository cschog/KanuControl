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
import fn from '../../util/CountryUtil.js';
import fn$1 from '../../util/FactorUtil.js';
import FormController from '../util/FormController.js';
import FormType from '../util/FormType.js';
import fn$2 from '../util/RouterUtil.js';
import Footer from '../views/enroll-factors/ManualSetupPushFooter.js';
import PhoneTextBox from '../views/enroll-factors/PhoneTextBox.js';

function goToFactorActivation(view, step) {
  const url = fn$2.createActivateFactorUrl(view.options.appState.get('activatedFactorProvider'), view.options.appState.get('activatedFactorType'), step);
  view.options.appState.trigger('navigate', url);
}

function setStateValues(view) {
  let userPhoneNumber;
  let userCountryCode;

  if (view.model.get('activationType') === 'SMS') {
    userCountryCode = view.model.get('countryCode');
    userPhoneNumber = view.model.get('phoneNumber');
  }

  view.options.appState.set({
    factorActivationType: view.model.get('activationType'),
    userCountryCode: userCountryCode,
    userPhoneNumber: userPhoneNumber
  });
}

function enrollFactor(view, factorType) {
  return view.model.doTransaction(function (transaction) {
    return transaction.prev().then(function (trans) {
      const factor = oktaUnderscore.findWhere(trans.factors, {
        factorType: factorType,
        provider: 'OKTA'
      });

      return factor.enroll();
    }).then(function (trans) {
      let textActivationLinkUrl;
      let emailActivationLinkUrl;
      let sharedSecret;
      const res = trans.data;

      if (res && res._embedded && res._embedded.factor && res._embedded.factor._embedded && res._embedded.factor._embedded.activation) {
        const factor = res._embedded.factor; // Shared secret

        sharedSecret = factor._embedded.activation.sharedSecret;

        if (factor._embedded.activation._links && factor._embedded.activation._links.send) {
          const activationSendLinks = factor._embedded.activation._links.send;

          const smsItem = oktaUnderscore.findWhere(activationSendLinks, {
            name: 'sms'
          }); // SMS activation url


          textActivationLinkUrl = smsItem ? smsItem.href : null; // Email activation url

          const emailItem = oktaUnderscore.findWhere(activationSendLinks, {
            name: 'email'
          });

          emailActivationLinkUrl = emailItem ? emailItem.href : null;
        }
      }

      view.model.set({
        SMS: textActivationLinkUrl,
        EMAIL: emailActivationLinkUrl,
        sharedSecret: sharedSecret
      });
      return trans;
    });
  });
}

var ManualSetupPushController = FormController.extend({
  className: 'enroll-manual-push',
  Model: function () {
    return {
      local: {
        activationType: ['string', true, this.options.appState.get('factorActivationType') || 'SMS'],
        countryCode: ['string', false, this.options.appState.get('userCountryCode')],
        phoneNumber: 'string',
        SMS: ['string', false, this.options.appState.get('textActivationLinkUrl')],
        EMAIL: ['string', false, this.options.appState.get('emailActivationLinkUrl')],
        sharedSecret: ['string', false, this.options.appState.get('sharedSecret')],
        __factorType__: ['string', false, this.options.factorType],
        __provider__: ['string', false, this.options.provider]
      },
      derived: {
        countryCallingCode: {
          deps: ['countryCode'],
          fn: function (countryCode) {
            return '+' + fn.getCallingCodeForCountry(countryCode);
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
  Form: {
    title: function () {
      const factorName = fn$1.getFactorLabel(this.model.get('__provider__'), this.model.get('__factorType__'));
      return loc('enroll.totp.title', 'login', [factorName]);
    },
    subtitle: oktaUnderscore.partial(loc, 'enroll.totp.cannotScanBarcode', 'login'),
    noButtonBar: true,
    attributes: {
      'data-se': 'step-manual-setup'
    },
    formChildren: function () {
      const instructions = this.settings.get('brandName') ? loc('enroll.totp.sharedSecretInstructions.specific', 'login', [this.settings.get('brandName')]) : loc('enroll.totp.sharedSecretInstructions.generic', 'login');
      const children = [FormType.Input({
        name: 'activationType',
        label: loc('mfa.setupOptions', 'login'),
        type: 'select',
        wide: true,
        options: {
          SMS: loc('enroll.totp.sendSms', 'login'),
          EMAIL: loc('enroll.totp.sendEmail', 'login'),
          MANUAL: loc('enroll.totp.setupManually', 'login')
        }
      }), FormType.Input({
        label: loc('mfa.country', 'login'),
        name: 'countryCode',
        type: 'select',
        wide: true,
        options: fn.getCountries(),
        showWhen: {
          activationType: 'SMS'
        }
      }), FormType.Input({
        label: loc('mfa.phoneNumber.placeholder', 'login'),
        'label-top': true,
        className: 'enroll-sms-phone',
        name: 'phoneNumber',
        input: PhoneTextBox,
        type: 'text',
        showWhen: {
          activationType: 'SMS'
        }
      }), FormType.View({
        View: View.extend({
          className: 'secret-key-instructions',
          attributes: {
            'data-se': 'secret-key-instructions'
          },
          template: _Handlebars2.template({
            "compiler": [8, ">= 4.3.0"],
            "main": function (container, depth0, helpers, partials, data) {
              var helper,
                  alias1 = depth0 != null ? depth0 : container.nullContext || {},
                  alias2 = container.hooks.helperMissing,
                  alias3 = "function",
                  alias4 = container.escapeExpression,
                  lookupProperty = container.lookupProperty || function (parent, propertyName) {
                if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
                  return parent[propertyName];
                }

                return undefined;
              };

              return "<section aria-live=\"assertive\"><p class=\"okta-form-subtitle o-form-explain text-align-c\">" + alias4((helper = (helper = lookupProperty(helpers, "instructions") || (depth0 != null ? lookupProperty(depth0, "instructions") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
                "name": "instructions",
                "hash": {},
                "data": data,
                "loc": {
                  "start": {
                    "line": 1,
                    "column": 89
                  },
                  "end": {
                    "line": 1,
                    "column": 105
                  }
                }
              }) : helper)) + "</p><p class=\"shared-key margin-top-10\" tabindex=0 aria-label=\"" + alias4((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
                "name": "i18n",
                "hash": {
                  "arguments": "sharedSecretKey",
                  "bundle": "login",
                  "code": "enroll.totp.sharedSecretInstructions.aria.secretKey"
                },
                "data": data,
                "loc": {
                  "start": {
                    "line": 1,
                    "column": 168
                  },
                  "end": {
                    "line": 1,
                    "column": 278
                  }
                }
              })) + "\">" + alias4((helper = (helper = lookupProperty(helpers, "sharedSecretKey") || (depth0 != null ? lookupProperty(depth0, "sharedSecretKey") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
                "name": "sharedSecretKey",
                "hash": {},
                "data": data,
                "loc": {
                  "start": {
                    "line": 1,
                    "column": 280
                  },
                  "end": {
                    "line": 1,
                    "column": 299
                  }
                }
              }) : helper)) + "</p></section>";
            },
            "useData": true
          }),
          initialize: function () {
            this.listenTo(this.model, 'change:sharedSecret', this.render);
          },
          getTemplateData: function () {
            return {
              instructions: instructions,
              sharedSecretKey: this.model.get('sharedSecret')
            };
          }
        }),
        showWhen: {
          activationType: 'MANUAL'
        }
      }), FormType.View({
        View: View.extend({
          template: _Handlebars2.template({
            "compiler": [8, ">= 4.3.0"],
            "main": function (container, depth0, helpers, partials, data) {
              return "<div data-type=\"next-button-wrap\"></div>";
            },
            "useData": true
          })
        }),
        showWhen: {
          activationType: 'MANUAL'
        }
      }), FormType.Button({
        title: loc('oform.next', 'login'),
        className: 'button button-primary button-wide button-next',
        attributes: {
          'data-se': 'next-button'
        },
        click: () => {
          setStateValues(this);
          goToFactorActivation(this, 'passcode');
        }
      }, '[data-type="next-button-wrap"]'), FormType.Toolbar({
        noCancelButton: true,
        save: loc('oform.send', 'login'),
        showWhen: {
          activationType: function (val) {
            return val === 'SMS' || val === 'EMAIL';
          }
        }
      })];
      return children;
    }
  },
  Footer: Footer,
  initialize: function () {
    this.setInitialModel(); // Move this logic to a model when AuthClient supports sending email and sms

    this.listenTo(this.form, 'save', function () {
      const self = this;
      this.model.doTransaction(function (transaction) {
        const activationType = this.get('activationType').toLowerCase();
        const opts = {};

        if (activationType === 'sms') {
          opts.profile = {
            phoneNumber: this.get('fullPhoneNumber')
          };
        }

        return transaction.factor.activation.send(activationType, opts).then(function (trans) {
          setStateValues(self); // Note: Need to defer because OktaAuth calls our router success
          // handler on the next tick - if we immediately called, appState would
          // still be populated with the last response

          oktaUnderscore.defer(function () {
            goToFactorActivation(self, 'sent');
          });

          return trans;
        });
      });
    });
    this.listenTo(this.model, 'change:activationType', function (model, value) {
      this.form.clearErrors();

      if (value === 'MANUAL' && this.options.appState.get('activatedFactorType') !== 'token:software:totp') {
        enrollFactor(this, 'token:software:totp');
      } else if (this.options.appState.get('activatedFactorType') !== 'push') {
        enrollFactor(this, 'push');
      }
    });
  },
  setInitialModel: function () {
    if (this.options.appState.get('factorActivationType') === 'SMS') {
      this.model.set({
        countryCode: this.options.appState.get('userCountryCode') || 'US',
        phoneNumber: this.options.appState.get('userPhoneNumber')
      });
    }
  },
  trapAuthResponse: function () {
    if (this.options.appState.get('isMfaEnrollActivate') || this.options.appState.get('isMfaEnroll')) {
      return true;
    }
  }
});

export { ManualSetupPushController as default };
//# sourceMappingURL=ManualSetupPushController.js.map
