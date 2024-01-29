import _Handlebars2 from '../../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { View } from '../../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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

var EnrollGoogleAuthenticatorBarcodeView = View.extend({
  className: 'oie-enroll-google-authenticator-barcode',
  template: _Handlebars2.template({
    "1": function (container, depth0, helpers, partials, data) {
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

      return "<div class=\"google-authenticator-setup-info-title barcode-setup-title\">" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "oie.enroll.google_authenticator.scanBarcode.title"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 83
          },
          "end": {
            "line": 1,
            "column": 163
          }
        }
      })) + "</div><div class=\"qrcode-info-container\"><p class=\"google-authenticator-setup-info\">" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "oie.enroll.google_authenticator.scanBarcode.description"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 247
          },
          "end": {
            "line": 1,
            "column": 333
          }
        }
      })) + "</p><div class=\"qrcode-container\"><img class=\"qrcode\" src=" + alias3((helper = (helper = lookupProperty(helpers, "href") || (depth0 != null ? lookupProperty(depth0, "href") : depth0)) != null ? helper : alias2, typeof helper === "function" ? helper.call(alias1, {
        "name": "href",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 391
          },
          "end": {
            "line": 1,
            "column": 399
          }
        }
      }) : helper)) + " alt=\"qr code\"></img><a href=\"#\" class=\"cannot-scan-link\">" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "oie.enroll.google_authenticator.scanBarcode.cannotScan"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 457
          },
          "end": {
            "line": 1,
            "column": 542
          }
        }
      })) + "</a></div></div>";
    },
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
      var stack1,
          lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return (stack1 = lookupProperty(helpers, "if").call(depth0 != null ? depth0 : container.nullContext || {}, depth0 != null ? lookupProperty(depth0, "href") : depth0, {
        "name": "if",
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
            "column": 565
          }
        }
      })) != null ? stack1 : "";
    },
    "useData": true
  }),
  getTemplateData: function () {
    var _contextualData$qrcod;

    const contextualData = this.options.appState.get('currentAuthenticator').contextualData;
    return {
      href: (_contextualData$qrcod = contextualData.qrcode) === null || _contextualData$qrcod === void 0 ? void 0 : _contextualData$qrcod.href
    };
  },
  events: {
    'click .cannot-scan-link': function (e) {
      e.preventDefault();
      this.options.model.set('viewToDisplay', 'manual');
    }
  }
});

export { EnrollGoogleAuthenticatorBarcodeView as default };
//# sourceMappingURL=EnrollGoogleAuthenticatorBarcodeView.js.map
