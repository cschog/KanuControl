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

const ConsentViewFooter = View.extend({
  className: 'consent-footer',
  template: _Handlebars2.template({
    "1": function (container, depth0, helpers, partials, data) {
      var stack1,
          helper,
          alias1 = depth0 != null ? depth0 : container.nullContext || {},
          alias2 = container.hooks.helperMissing,
          alias3 = container.escapeExpression,
          lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<a class=\"terms-of-service\" href=\"" + alias3((helper = (helper = lookupProperty(helpers, "termsOfService") || (depth0 != null ? lookupProperty(depth0, "termsOfService") : depth0)) != null ? helper : alias2, typeof helper === "function" ? helper.call(alias1, {
        "name": "termsOfService",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 56
          },
          "end": {
            "line": 1,
            "column": 74
          }
        }
      }) : helper)) + "\" target=\"_blank\">" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "consent.required.termsOfService"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 92
          },
          "end": {
            "line": 1,
            "column": 154
          }
        }
      })) + "</a>" + ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "privacyPolicy") : depth0, {
        "name": "if",
        "hash": {},
        "fn": container.program(2, data, 0),
        "inverse": container.noop,
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 158
          },
          "end": {
            "line": 1,
            "column": 226
          }
        }
      })) != null ? stack1 : "");
    },
    "2": function (container, depth0, helpers, partials, data) {
      return "<span class=\"no-translate\">&#8226</span>";
    },
    "4": function (container, depth0, helpers, partials, data) {
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

      return "<a class=\"privacy-policy\" href=\"" + alias3((helper = (helper = lookupProperty(helpers, "privacyPolicy") || (depth0 != null ? lookupProperty(depth0, "privacyPolicy") : depth0)) != null ? helper : alias2, typeof helper === "function" ? helper.call(alias1, {
        "name": "privacyPolicy",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 286
          },
          "end": {
            "line": 1,
            "column": 303
          }
        }
      }) : helper)) + "\" target=\"_blank\">" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "consent.required.privacyPolicy"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 321
          },
          "end": {
            "line": 1,
            "column": 382
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

      return ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "termsOfService") : depth0, {
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
            "column": 233
          }
        }
      })) != null ? stack1 : "") + ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "privacyPolicy") : depth0, {
        "name": "if",
        "hash": {},
        "fn": container.program(4, data, 0),
        "inverse": container.noop,
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 233
          },
          "end": {
            "line": 1,
            "column": 393
          }
        }
      })) != null ? stack1 : "");
    },
    "useData": true
  }),
  getTemplateData: function () {
    const appState = this.options.appState;
    const app = appState.get('app');
    return {
      termsOfService: app.termsOfService && app.termsOfService.href,
      privacyPolicy: app.privacyPolicy && app.privacyPolicy.href
    };
  }
});

export { ConsentViewFooter as default };
//# sourceMappingURL=EnduserConsentViewFooter.js.map
