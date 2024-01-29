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
import Q from 'q';
import fn$1 from '../../util/CryptoUtil.js';
import { WebauthnAbortError, WebAuthnError } from '../../util/Errors.js';
import fn from '../../util/FactorUtil.js';
import FormController from '../util/FormController.js';
import FormType from '../util/FormType.js';
import webauthn from '../../util/webauthn.js';
import fn$2 from '../../util/BrowserFeatures.js';
import HtmlErrorMessageView from '../views/mfa-verify/HtmlErrorMessageView.js';
import FooterMFA from '../views/shared/FooterMFA.js';

function getAllowCredentials(factors) {
  const allowCredentials = [];

  oktaUnderscore.each(factors, function (factor) {
    allowCredentials.push({
      type: 'public-key',
      id: fn$1.strToBin(factor.profile.credentialId)
    });
  });

  return allowCredentials;
}

var VerifyWebauthnController = FormController.extend({
  className: 'mfa-verify verify-webauthn',
  Model: {
    props: {
      rememberDevice: 'boolean'
    },
    initialize: function () {
      const rememberDevice = fn.getRememberDeviceValue(this.appState); // set the initial value for remember device (Cannot do this while defining the
      // local property because this.settings would not be initialized there yet).

      this.set('rememberDevice', rememberDevice);
      this.appState.on('factorSwitched signOut', () => {
        if (this.webauthnAbortController) {
          this.webauthnAbortController.abort();
          this.webauthnAbortController = null;
        }
      });
    },
    save: function () {
      this.trigger('request');
      return this.doTransaction(function (transaction) {
        let factor;

        if (transaction.factorTypes) {
          factor = oktaUnderscore.findWhere(transaction.factorTypes, {
            factorType: 'webauthn'
          });
        } else {
          factor = oktaUnderscore.findWhere(transaction.factors, {
            factorType: 'webauthn',
            provider: 'FIDO'
          });
        }

        const self = this;
        return factor.verify().then(function (transaction) {
          let allowCredentials;
          let challenge;

          if (transaction.factors) {
            const factors = transaction.factors;
            challenge = transaction.challenge;
            allowCredentials = getAllowCredentials(factors);
          } else {
            const factorData = transaction.factor;
            challenge = factorData.challenge;
            allowCredentials = getAllowCredentials([factorData]);
          }

          self.trigger('request'); // verify via browser webauthn js

          const options = oktaUnderscore.extend({}, challenge, {
            allowCredentials: allowCredentials,
            challenge: fn$1.strToBin(challenge.challenge)
          }); // AbortController is not supported in IE11


          if (typeof AbortController !== 'undefined') {
            self.webauthnAbortController = new AbortController();
          }

          return new Q( // navigator.credentials is not supported in IE11
          // eslint-disable-next-line compat/compat
          navigator.credentials.get({
            publicKey: options,
            signal: self.webauthnAbortController && self.webauthnAbortController.signal
          })).then(function (assertion) {
            const rememberDevice = !!self.get('rememberDevice');
            return factor.verify({
              clientData: fn$1.binToStr(assertion.response.clientDataJSON),
              authenticatorData: fn$1.binToStr(assertion.response.authenticatorData),
              signatureData: fn$1.binToStr(assertion.response.signature),
              rememberDevice: rememberDevice
            });
          }).catch(function (error) {
            self.trigger('errors:clear'); // Do not display if it is abort error triggered by code when switching.
            // self.webauthnAbortController would be null if abort was triggered by code.

            if (!self.webauthnAbortController) {
              throw new WebauthnAbortError();
            } else {
              throw new WebAuthnError({
                xhr: {
                  responseJSON: {
                    errorSummary: error.message
                  }
                }
              });
            }
          }).finally(function () {
            // unset webauthnAbortController on successful authentication or error
            self.webauthnAbortController = null;
          });
        });
      });
    }
  },
  Form: {
    autoSave: true,
    hasSavingState: false,
    title: oktaUnderscore.partial(loc, 'factor.webauthn.biometric', 'login'),
    className: 'verify-webauthn-form',
    noCancelButton: true,
    save: oktaUnderscore.partial(loc, 'mfa.challenge.verify', 'login'),
    noButtonBar: function () {
      return !webauthn.isNewApiAvailable();
    },
    modelEvents: {
      request: '_startEnrollment',
      error: '_stopEnrollment'
    },
    formChildren: function () {
      const children = [];

      if (webauthn.isNewApiAvailable()) {
        children.push(FormType.View({
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

                return "<div class=\"webauthn-verify-text\"><p>" + container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
                  "name": "i18n",
                  "hash": {
                    "bundle": "login",
                    "code": "verify.webauthn.biometric.instructions"
                  },
                  "data": data,
                  "loc": {
                    "start": {
                      "line": 1,
                      "column": 37
                    },
                    "end": {
                      "line": 1,
                      "column": 106
                    }
                  }
                })) + "</p><div data-se=\"webauthn-waiting\" class=\"okta-waiting-spinner\"></div></div>";
              },
              "useData": true
            })
          })
        }));
      } else {
        let errorMessageKey = 'webauthn.biometric.error.factorNotSupported';

        if (this.options.appState.get('factors').length === 1) {
          errorMessageKey = 'webauthn.biometric.error.factorNotSupported.oneFactor';
        }

        children.push(FormType.View({
          View: new HtmlErrorMessageView({
            message: loc(errorMessageKey, 'login')
          })
        }, {
          selector: '.o-form-error-container'
        }));
      }

      if (this.options.appState.get('allowRememberDevice')) {
        children.push(FormType.Input({
          label: false,
          'label-top': true,
          placeholder: this.options.appState.get('rememberDeviceLabel'),
          className: 'margin-btm-0',
          name: 'rememberDevice',
          type: 'checkbox'
        }));
      }

      return children;
    },
    _startEnrollment: function () {
      this.$('.okta-waiting-spinner').show();
      this.$('.o-form-button-bar').hide();
    },
    _stopEnrollment: function () {
      this.$('.okta-waiting-spinner').hide();
      this.$('.o-form-button-bar [type="submit"]')[0].value = loc('verify.u2f.retry', 'login');
      this.$('.o-form-button-bar').show();
    }
  },
  postRender: function () {
    oktaUnderscore.defer(() => {
      // Trigger browser prompt automatically for other browsers for better UX.
      if (webauthn.isNewApiAvailable() && !fn$2.isSafari()) {
        this.model.save();
      }
    });
  },
  back: function () {// Empty function on verify controllers to prevent users
    // from navigating back during 'verify' using the browser's
    // back button. The URL will still change, but the view will not
    // More details in OKTA-135060.
  },
  Footer: FooterMFA
});

export { VerifyWebauthnController as default };
//# sourceMappingURL=VerifyWebauthnController.js.map
