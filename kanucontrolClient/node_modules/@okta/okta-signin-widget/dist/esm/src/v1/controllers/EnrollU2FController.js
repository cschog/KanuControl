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
import fn from '../../util/FidoUtil.js';
import FormController from '../util/FormController.js';
import FormType from '../util/FormType.js';
import Footer from '../views/enroll-factors/Footer.js';
import HtmlErrorMessageView from '../views/mfa-verify/HtmlErrorMessageView.js';

var EnrollU2FController = FormController.extend({
  className: 'enroll-u2f',
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
          factorType: 'u2f',
          provider: 'FIDO'
        });

        return factor.enroll();
      });
    },
    activate: function () {
      this.set('__enrolled__', true);
      this.trigger('errors:clear');
      return this.doTransaction(function (transaction) {
        const activation = transaction.factor.activation;
        const appId = activation.appId;
        const registerRequests = [{
          version: fn.getU2fVersion(),
          challenge: activation.nonce
        }];
        const self = this;
        const deferred = Q.defer();
        u2f.register(appId, registerRequests, [], function (data) {
          self.trigger('errors:clear');

          if (data.errorCode && data.errorCode !== 0) {
            deferred.reject(new U2FError({
              xhr: {
                responseJSON: {
                  errorSummary: fn.getU2fEnrollErrorMessageByCode(data.errorCode)
                }
              }
            }));
          } else {
            deferred.resolve(transaction.activate({
              registrationData: data.registrationData,
              version: data.version,
              challenge: data.challenge,
              clientData: data.clientData
            }));
          }
        });
        return deferred.promise;
      });
    }
  },
  Form: {
    title: oktaUnderscore.partial(loc, 'enroll.u2f.title', 'login'),
    save: oktaUnderscore.partial(loc, 'enroll.u2f.save', 'login'),
    noCancelButton: true,
    hasSavingState: false,
    autoSave: true,
    className: 'enroll-u2f-form',
    noButtonBar: function () {
      return !fn.isU2fAvailable();
    },
    modelEvents: {
      request: '_startEnrollment',
      error: '_stopEnrollment'
    },
    formChildren: function () {
      const result = [];

      if (!fn.isU2fAvailable()) {
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
        //There is html in enroll.u2f.general2 in our properties file, reason why is unescaped
        result.push(FormType.View({
          View: View.extend({
            template: _Handlebars2.template({
              "compiler": [8, ">= 4.3.0"],
              "main": function (container, depth0, helpers, partials, data) {
                var stack1,
                    alias1 = depth0 != null ? depth0 : container.nullContext || {},
                    alias2 = container.hooks.helperMissing,
                    lookupProperty = container.lookupProperty || function (parent, propertyName) {
                  if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
                    return parent[propertyName];
                  }

                  return undefined;
                };

                return "<div class=\"u2f-instructions\"><ol><li>" + ((stack1 = (lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
                  "name": "i18n",
                  "hash": {
                    "bundle": "login",
                    "code": "enroll.u2f.general2"
                  },
                  "data": data,
                  "loc": {
                    "start": {
                      "line": 1,
                      "column": 38
                    },
                    "end": {
                      "line": 1,
                      "column": 90
                    }
                  }
                })) != null ? stack1 : "") + "</li><li>" + container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
                  "name": "i18n",
                  "hash": {
                    "bundle": "login",
                    "code": "enroll.u2f.general3"
                  },
                  "data": data,
                  "loc": {
                    "start": {
                      "line": 1,
                      "column": 99
                    },
                    "end": {
                      "line": 1,
                      "column": 149
                    }
                  }
                })) + "</li></ol></div>";
              },
              "useData": true
            })
          })
        }));
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

                return "<div class=\"u2f-enroll-text hide\"><p>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
                  "name": "i18n",
                  "hash": {
                    "bundle": "login",
                    "code": "enroll.u2f.instructions"
                  },
                  "data": data,
                  "loc": {
                    "start": {
                      "line": 1,
                      "column": 37
                    },
                    "end": {
                      "line": 1,
                      "column": 91
                    }
                  }
                })) + "</p><p>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
                  "name": "i18n",
                  "hash": {
                    "bundle": "login",
                    "code": "enroll.u2f.instructionsBluetooth"
                  },
                  "data": data,
                  "loc": {
                    "start": {
                      "line": 1,
                      "column": 98
                    },
                    "end": {
                      "line": 1,
                      "column": 161
                    }
                  }
                })) + "</p><div data-se=\"u2f-devices\" class=\"u2f-devices-images\"><div class=\"u2f-usb\"></div><div class=\"u2f-bluetooth\"></div></div><div data-se=\"u2f-waiting\" class=\"okta-waiting-spinner\"></div></div>";
              },
              "useData": true
            })
          })
        }));
      }

      return result;
    },
    _startEnrollment: function () {
      this.$('.u2f-instructions').addClass('hide');
      this.$('.u2f-enroll-text').removeClass('hide');
      this.$('.o-form-button-bar').hide();
    },
    _stopEnrollment: function () {
      this.$('.u2f-instructions').removeClass('hide');
      this.$('.u2f-enroll-text').addClass('hide');
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

export { EnrollU2FController as default };
//# sourceMappingURL=EnrollU2FController.js.map
