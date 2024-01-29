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
import 'u2f-api-polyfill';
import { U2FError } from '../../util/Errors.js';
import fn from '../../util/FactorUtil.js';
import fn$1 from '../../util/FidoUtil.js';
import FormController from '../util/FormController.js';
import FormType from '../util/FormType.js';
import HtmlErrorMessageView from '../views/mfa-verify/HtmlErrorMessageView.js';
import FooterMFA from '../views/shared/FooterMFA.js';

function getRegisteredKeysSequence(factors) {
  const keys = [];

  oktaUnderscore.each(factors, function (factor) {
    keys.push({
      version: factor.profile.version,
      keyHandle: factor.profile.credentialId
    });
  });

  return keys;
}

var VerifyU2FController = FormController.extend({
  className: 'mfa-verify verify-u2f',
  Model: {
    props: {
      rememberDevice: 'boolean'
    },
    initialize: function () {
      const rememberDevice = fn.getRememberDeviceValue(this.appState); // set the initial value for remember device (Cannot do this while defining the
      // local property because this.settings would not be initialized there yet).

      this.set('rememberDevice', rememberDevice);
    },
    save: function () {
      this.trigger('request');
      return this.doTransaction(function (transaction) {
        let factor;

        if (transaction.factorTypes) {
          factor = oktaUnderscore.findWhere(transaction.factorTypes, {
            factorType: 'u2f'
          });
        } else {
          factor = oktaUnderscore.findWhere(transaction.factors, {
            factorType: 'u2f',
            provider: 'FIDO'
          });
        }

        const self = this;
        return factor.verify().then(function (transaction) {
          let registeredKeys;
          let appId;
          let nonce;

          if (transaction.factors) {
            const factors = transaction.factors;
            appId = factors[0]['profile']['appId'];
            nonce = transaction.challenge.nonce;
            registeredKeys = getRegisteredKeysSequence(factors);
          } else {
            const factorData = transaction.factor;
            appId = factorData.profile.appId;
            nonce = factorData.challenge.nonce;
            registeredKeys = [{
              version: fn$1.getU2fVersion(),
              keyHandle: factorData.profile.credentialId
            }];
          }

          self.trigger('request');
          const deferred = Q.defer();
          u2f.sign(appId, nonce, registeredKeys, function (data) {
            self.trigger('errors:clear');

            if (data.errorCode && data.errorCode !== 0) {
              const isOneFactor = self.options.appState.get('factors').length === 1;
              deferred.reject(new U2FError({
                xhr: {
                  responseJSON: {
                    errorSummary: fn$1.getU2fVerifyErrorMessageByCode(data.errorCode, isOneFactor)
                  }
                }
              }));
            } else {
              const rememberDevice = !!self.get('rememberDevice');
              return factor.verify({
                clientData: data.clientData,
                signatureData: data.signatureData,
                rememberDevice: rememberDevice
              }).then(deferred.resolve);
            }
          });
          return deferred.promise;
        });
      });
    }
  },
  Form: {
    autoSave: true,
    hasSavingState: false,
    title: oktaUnderscore.partial(loc, 'factor.u2f', 'login'),
    className: 'verify-u2f-form',
    noCancelButton: true,
    save: oktaUnderscore.partial(loc, 'verify.u2f.retry', 'login'),
    noButtonBar: function () {
      return !fn$1.isU2fAvailable();
    },
    modelEvents: {
      request: '_startEnrollment',
      error: '_stopEnrollment'
    },
    formChildren: function () {
      const result = [];

      if (!fn$1.isU2fAvailable()) {
        let errorMessageKey = 'u2f.error.factorNotSupported';

        if (this.options.appState.get('factors').length === 1) {
          errorMessageKey = 'u2f.error.factorNotSupported.oneFactor';
        }

        result.push(FormType.View({
          View: new HtmlErrorMessageView({
            message: loc(errorMessageKey, 'login')
          })
        }, {
          selector: '.o-form-error-container'
        }));
      } else {
        result.push(FormType.View({
          View: View.extend({
            template: _Handlebars2.template({
              "compiler": [8, ">= 4.3.0"],
              "main": function (container, depth0, helpers, partials, data) {
                var alias1 = depth0 != null ? depth0 : container.nullContext || {},
                    alias2 = container.hooks.helperMissing,
                    alias3 = container.escapeExpression,
                    lookupProperty = container.lookupProperty || function (parent, propertyName) {
                  if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
                    return parent[propertyName];
                  }

                  return undefined;
                };

                return "<div class=\"u2f-verify-text\"><p>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
                  "name": "i18n",
                  "hash": {
                    "bundle": "login",
                    "code": "verify.u2f.instructions"
                  },
                  "data": data,
                  "loc": {
                    "start": {
                      "line": 1,
                      "column": 32
                    },
                    "end": {
                      "line": 1,
                      "column": 86
                    }
                  }
                })) + "</p><p>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
                  "name": "i18n",
                  "hash": {
                    "bundle": "login",
                    "code": "verify.u2f.instructionsBluetooth"
                  },
                  "data": data,
                  "loc": {
                    "start": {
                      "line": 1,
                      "column": 93
                    },
                    "end": {
                      "line": 1,
                      "column": 156
                    }
                  }
                })) + "</p><div data-se=\"u2f-waiting\" class=\"okta-waiting-spinner\"></div></div>";
              },
              "useData": true
            })
          })
        }));
      }

      if (this.options.appState.get('allowRememberDevice')) {
        result.push(FormType.Input({
          label: false,
          'label-top': true,
          placeholder: this.options.appState.get('rememberDeviceLabel'),
          className: 'margin-btm-0',
          name: 'rememberDevice',
          type: 'checkbox'
        }));
      }

      return result;
    },
    postRender: function () {
      oktaUnderscore.defer(() => {
        if (fn$1.isU2fAvailable()) {
          this.model.save();
        } else {
          this.$('[data-se="u2f-waiting"]').addClass('hide');
        }
      });
    },
    _startEnrollment: function () {
      this.$('.okta-waiting-spinner').removeClass('hide');
      this.$('.o-form-button-bar').hide();
    },
    _stopEnrollment: function () {
      this.$('.okta-waiting-spinner').addClass('hide');
      this.$('.o-form-button-bar').show();
    }
  },
  back: function () {// Empty function on verify controllers to prevent users
    // from navigating back during 'verify' using the browser's
    // back button. The URL will still change, but the view will not
    // More details in OKTA-135060.
  },
  Footer: FooterMFA
});

export { VerifyU2FController as default };
//# sourceMappingURL=VerifyU2FController.js.map
