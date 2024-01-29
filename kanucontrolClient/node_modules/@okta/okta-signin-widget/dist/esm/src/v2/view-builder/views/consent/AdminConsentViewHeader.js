import _Handlebars2 from '../../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { View, loc } from '../../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import oktaUnderscore from '../../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';

const defaultLogo = '/img/logos/default.png';
const AdminConsentViewHeader = View.extend({
  className: 'consent-title detail-row',
  titleText: () => loc('oie.consent.scopes.admin.title', 'login'),
  hasIssuer: true,
  template: _Handlebars2.template({
    "1": function (container, depth0, helpers, partials, data) {
      var helper,
          lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<a href=\"" + container.escapeExpression((helper = (helper = lookupProperty(helpers, "clientURI") || (depth0 != null ? lookupProperty(depth0, "clientURI") : depth0)) != null ? helper : container.hooks.helperMissing, typeof helper === "function" ? helper.call(depth0 != null ? depth0 : container.nullContext || {}, {
        "name": "clientURI",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 26
          },
          "end": {
            "line": 1,
            "column": 39
          }
        }
      }) : helper)) + "\" class=\"client-logo-link\" target=\"_blank\">";
    },
    "3": function (container, depth0, helpers, partials, data) {
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

      return "<img class=\"client-logo custom-logo\" src=\"" + alias4((helper = (helper = lookupProperty(helpers, "customLogo") || (depth0 != null ? lookupProperty(depth0, "customLogo") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "customLogo",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 149
          },
          "end": {
            "line": 1,
            "column": 163
          }
        }
      }) : helper)) + "\" alt=\"" + alias4((helper = (helper = lookupProperty(helpers, "altText") || (depth0 != null ? lookupProperty(depth0, "altText") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "altText",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 170
          },
          "end": {
            "line": 1,
            "column": 181
          }
        }
      }) : helper)) + "\" aria-hidden=\"true\" />";
    },
    "5": function (container, depth0, helpers, partials, data) {
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

      return "<img class=\"client-logo default-logo\" src=\"" + alias4((helper = (helper = lookupProperty(helpers, "defaultLogo") || (depth0 != null ? lookupProperty(depth0, "defaultLogo") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "defaultLogo",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 255
          },
          "end": {
            "line": 1,
            "column": 270
          }
        }
      }) : helper)) + "\" alt=\"" + alias4((helper = (helper = lookupProperty(helpers, "altText") || (depth0 != null ? lookupProperty(depth0, "altText") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "altText",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 277
          },
          "end": {
            "line": 1,
            "column": 288
          }
        }
      }) : helper)) + "\" aria-hidden=\"true\" />";
    },
    "7": function (container, depth0, helpers, partials, data) {
      return "</a>";
    },
    "9": function (container, depth0, helpers, partials, data) {
      var helper,
          lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<div class=\"issuer no-translate\"><span>" + container.escapeExpression((helper = (helper = lookupProperty(helpers, "issuer") || (depth0 != null ? lookupProperty(depth0, "issuer") : depth0)) != null ? helper : container.hooks.helperMissing, typeof helper === "function" ? helper.call(depth0 != null ? depth0 : container.nullContext || {}, {
        "name": "issuer",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 493
          },
          "end": {
            "line": 1,
            "column": 503
          }
        }
      }) : helper)) + "</span></div>";
    },
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
      var stack1,
          helper,
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

      return ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "clientURI") : depth0, {
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
            "column": 89
          }
        }
      })) != null ? stack1 : "") + ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "customLogo") : depth0, {
        "name": "if",
        "hash": {},
        "fn": container.program(3, data, 0),
        "inverse": container.program(5, data, 0),
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 89
          },
          "end": {
            "line": 1,
            "column": 318
          }
        }
      })) != null ? stack1 : "") + ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "clientURI") : depth0, {
        "name": "if",
        "hash": {},
        "fn": container.program(7, data, 0),
        "inverse": container.noop,
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 318
          },
          "end": {
            "line": 1,
            "column": 346
          }
        }
      })) != null ? stack1 : "") + "<h1><span class=\"title-text\"><b class=\"no-translate\">" + alias4((helper = (helper = lookupProperty(helpers, "appName") || (depth0 != null ? lookupProperty(depth0, "appName") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "appName",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 399
          },
          "end": {
            "line": 1,
            "column": 410
          }
        }
      }) : helper)) + "</b>&nbsp;" + alias4((helper = (helper = lookupProperty(helpers, "titleText") || (depth0 != null ? lookupProperty(depth0, "titleText") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "titleText",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 420
          },
          "end": {
            "line": 1,
            "column": 433
          }
        }
      }) : helper)) + "</span>" + ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "issuer") : depth0, {
        "name": "if",
        "hash": {},
        "fn": container.program(9, data, 0),
        "inverse": container.noop,
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 440
          },
          "end": {
            "line": 1,
            "column": 523
          }
        }
      })) != null ? stack1 : "") + "</h1>";
    },
    "useData": true
  }),
  getTemplateData: function () {
    const {
      appState: appState
    } = this.options;
    const {
      label: label,
      clientUri: clientUri,
      logo: logo
    } = appState.get('app');
    const {
      issuer: issuerObj
    } = appState.get('authentication');
    const customLogo = logo === null || logo === void 0 ? void 0 : logo.href;
    const altText = loc('logo.for.the.app.alt.text', 'login');

    const appName = oktaUnderscore.escape(label);

    const clientURI = clientUri === null || clientUri === void 0 ? void 0 : clientUri.href;
    const issuer = this.hasIssuer ? issuerObj === null || issuerObj === void 0 ? void 0 : issuerObj.uri : null;
    const titleText = this.titleText();
    return {
      customLogo: customLogo,
      defaultLogo: defaultLogo,
      clientURI: clientURI,
      issuer: issuer,
      altText: altText,
      appName: appName,
      titleText: titleText
    };
  }
});

export { AdminConsentViewHeader as default };
//# sourceMappingURL=AdminConsentViewHeader.js.map
