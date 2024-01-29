import _Handlebars2 from '../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { View, loc } from '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import AuthenticatorFooter from './AuthenticatorFooter.js';

const OKTA_AUTHENTICATOR = 'Okta_Authenticator';
const CantVerifyInfoVerifyFlowView = View.extend({
  id: 'help-description-container',
  className: 'help-description js-help-description',
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

      return "<h3>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "oie.verify.webauthn.cant.verify.biometric.authenticator.title"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 4
          },
          "end": {
            "line": 1,
            "column": 96
          }
        }
      })) + "</h3><br><p>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "oie.verify.webauthn.cant.verify.biometric.authenticator.description1"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 108
          },
          "end": {
            "line": 1,
            "column": 207
          }
        }
      })) + "</p><br><p>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "oie.verify.webauthn.cant.verify.biometric.authenticator.description2"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 218
          },
          "end": {
            "line": 1,
            "column": 317
          }
        }
      })) + "</p><br><h3>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "oie.verify.webauthn.cant.verify.security.key.title"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 329
          },
          "end": {
            "line": 1,
            "column": 410
          }
        }
      })) + "</h3><br><p>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "oie.verify.webauthn.cant.verify.security.key.description"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 422
          },
          "end": {
            "line": 1,
            "column": 509
          }
        }
      })) + "</p><br>";
    },
    "useData": true
  })
});
const CantVerifyInfoOVEnrollmentFlowView = View.extend({
  id: 'help-description-container',
  className: 'help-description js-help-description',
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

      return "<ol class=\"ov-enrollment-info\"><li>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "oie.verify.webauthn.cant.verify.enrollment.step1"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 35
          },
          "end": {
            "line": 1,
            "column": 114
          }
        }
      })) + "</li><br><li>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "oie.verify.webauthn.cant.verify.enrollment.step2"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 127
          },
          "end": {
            "line": 1,
            "column": 206
          }
        }
      })) + "</li><br><li>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "oie.verify.webauthn.cant.verify.enrollment.step3"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 219
          },
          "end": {
            "line": 1,
            "column": 298
          }
        }
      })) + "</li><br><li>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "oie.verify.webauthn.cant.verify.enrollment.step4"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 311
          },
          "end": {
            "line": 1,
            "column": 390
          }
        }
      })) + "</><br></ol>";
    },
    "useData": true
  })
});
var ChallengeWebauthnFooter = AuthenticatorFooter.extend({
  links: function () {
    const links = AuthenticatorFooter.prototype.links.apply(this, arguments);
    const cantVerifyInfoView = this.options.appState.get('app') && this.options.appState.get('app').name === OKTA_AUTHENTICATOR ? CantVerifyInfoOVEnrollmentFlowView : CantVerifyInfoVerifyFlowView;
    links.unshift({
      'label': loc('oie.verify.webauthn.cant.verify', 'login'),
      'name': 'cant-verify',
      'aria-controls': 'help-description-container',
      'class': 'link help js-help',
      'type': 'toggle-text-link',
      'additionalOptions': {
        view: cantVerifyInfoView,
        selector: '.js-help-description'
      }
    });
    return links;
  }
});

export { ChallengeWebauthnFooter as default };
//# sourceMappingURL=ChallengeWebauthnFooter.js.map
