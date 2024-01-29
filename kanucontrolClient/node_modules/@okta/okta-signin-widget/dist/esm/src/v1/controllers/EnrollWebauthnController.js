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
import fn$1 from '../../util/BrowserFeatures.js';
import fn from '../../util/CryptoUtil.js';
import { WebauthnAbortError, WebAuthnError } from '../../util/Errors.js';
import FormController from '../util/FormController.js';
import FormType from '../util/FormType.js';
import webauthn from '../../util/webauthn.js';
import Footer from '../views/enroll-factors/Footer.js';
import HtmlErrorMessageView from '../views/mfa-verify/HtmlErrorMessageView.js';

function getExcludeCredentials(credentials) {
  const excludeCredentials = [];

  oktaUnderscore.each(credentials, function (credential) {
    excludeCredentials.push({
      type: 'public-key',
      id: fn.strToBin(credential.id)
    });
  });

  return excludeCredentials;
}

var EnrollWebauthnController = FormController.extend({
  className: 'enroll-webauthn',
  Model: {
    local: {
      __enrolled__: 'boolean'
    },
    save: function () {
      this.trigger('request');

      if (this.get('__enrolled__')) {
        return this.activate();
      }

      return this.doTransaction(function (transaction) {
        const factor = oktaUnderscore.findWhere(transaction.factors, {
          factorType: 'webauthn',
          provider: 'FIDO'
        });

        return factor.enroll();
      });
    },
    activate: function () {
      this.set('__enrolled__', true);
      this.trigger('errors:clear');
      this.appState.on('backToFactors', () => {
        if (this.webauthnAbortController) {
          this.webauthnAbortController.abort();
          this.webauthnAbortController = null;
        }
      });
      return this.doTransaction(function (transaction) {
        const activation = transaction.factor.activation; // enroll via browser webauthn js

        const self = this;

        if (webauthn.isNewApiAvailable()) {
          const options = oktaUnderscore.extend({}, activation, {
            challenge: fn.strToBin(activation.challenge),
            user: {
              id: fn.strToBin(activation.user.id),
              name: activation.user.name,
              displayName: activation.user.displayName
            },
            excludeCredentials: getExcludeCredentials(activation.excludeCredentials)
          }); // AbortController is not supported in IE11


          if (typeof AbortController !== 'undefined') {
            self.webauthnAbortController = new AbortController();
          }

          return new Q(navigator.credentials.create({
            publicKey: options,
            signal: self.webauthnAbortController && self.webauthnAbortController.signal
          })).then(function (newCredential) {
            return transaction.activate({
              attestation: fn.binToStr(newCredential.response.attestationObject),
              clientData: fn.binToStr(newCredential.response.clientDataJSON)
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
        }
      });
    }
  },
  Form: {
    title: oktaUnderscore.partial(loc, 'enroll.webauthn.biometric.title', 'login'),
    save: oktaUnderscore.partial(loc, 'enroll.webauthn.save', 'login'),
    noCancelButton: true,
    hasSavingState: false,
    autoSave: true,
    className: 'enroll-webauthn-form',
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
        //enroll.webauthn.biometric.instructions.edge is unescaped because it contains html
        children.push(FormType.View({
          View: View.extend({
            className: 'webauthn-enroll-text',
            template: _Handlebars2.template({
              "1": function (container, depth0, helpers, partials, data) {
                var stack1,
                    lookupProperty = container.lookupProperty || function (parent, propertyName) {
                  if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
                    return parent[propertyName];
                  }

                  return undefined;
                };

                return "<div class=\"webauthn-edge-text\"><p>" + ((stack1 = (lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
                  "name": "i18n",
                  "hash": {
                    "bundle": "login",
                    "code": "enroll.webauthn.biometric.instructions.edge"
                  },
                  "data": data,
                  "loc": {
                    "start": {
                      "line": 1,
                      "column": 173
                    },
                    "end": {
                      "line": 1,
                      "column": 249
                    }
                  }
                })) != null ? stack1 : "") + "</p></div>";
              },
              "3": function (container, depth0, helpers, partials, data) {
                var stack1,
                    lookupProperty = container.lookupProperty || function (parent, propertyName) {
                  if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
                    return parent[propertyName];
                  }

                  return undefined;
                };

                return "<div class=\"webauthn-restrictions-text\"><p>" + ((stack1 = (lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
                  "name": "i18n",
                  "hash": {
                    "bundle": "login",
                    "code": "enroll.webauthn.instructions.noSupportForBiometric"
                  },
                  "data": data,
                  "loc": {
                    "start": {
                      "line": 1,
                      "column": 340
                    },
                    "end": {
                      "line": 1,
                      "column": 423
                    }
                  }
                })) != null ? stack1 : "") + "</p></div>";
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

                return "<div class=\"webauthn-enroll-instructions\"><p>" + container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(alias1, {
                  "name": "i18n",
                  "hash": {
                    "bundle": "login",
                    "code": "enroll.webauthn.biometric.instructions"
                  },
                  "data": data,
                  "loc": {
                    "start": {
                      "line": 1,
                      "column": 45
                    },
                    "end": {
                      "line": 1,
                      "column": 114
                    }
                  }
                })) + "</p></div>" + ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "isEdge") : depth0, {
                  "name": "if",
                  "hash": {},
                  "fn": container.program(1, data, 0),
                  "inverse": container.noop,
                  "data": data,
                  "loc": {
                    "start": {
                      "line": 1,
                      "column": 124
                    },
                    "end": {
                      "line": 1,
                      "column": 266
                    }
                  }
                })) != null ? stack1 : "") + ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "onlySupportsSecurityKey") : depth0, {
                  "name": "if",
                  "hash": {},
                  "fn": container.program(3, data, 0),
                  "inverse": container.noop,
                  "data": data,
                  "loc": {
                    "start": {
                      "line": 1,
                      "column": 266
                    },
                    "end": {
                      "line": 1,
                      "column": 440
                    }
                  }
                })) != null ? stack1 : "") + "<div data-se=\"webauthn-waiting\" class=\"okta-waiting-spinner hide\"></div>";
              },
              "useData": true
            }),
            getTemplateData: function () {
              return {
                isEdge: fn$1.isEdge(),
                onlySupportsSecurityKey: (fn$1.isFirefox() || fn$1.isSafari()) && fn$1.isMac()
              };
            }
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

      return children;
    },
    _startEnrollment: function () {
      this.$('.okta-waiting-spinner').show();
      this.$('.o-form-button-bar').hide();
    },
    _stopEnrollment: function () {
      this.$('.okta-waiting-spinner').hide();
      this.$('.o-form-button-bar').show();
    }
  },
  Footer: Footer,
  trapAuthResponse: function () {
    if (this.options.appState.get('isMfaEnrollActivate')) {
      this.model.activate();
      return true;
    }
  }
});

export { EnrollWebauthnController as default };
//# sourceMappingURL=EnrollWebauthnController.js.map
