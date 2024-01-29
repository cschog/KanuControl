import _Handlebars2 from '../../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import '../../internals/BaseHeader.js';
import '../../internals/BaseFooter.js';
import BaseForm from '../../internals/BaseForm.js';
import '../../internals/BaseFormWithPolling.js';
import '../../internals/BaseOktaVerifyChallengeView.js';
import { View } from '../../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import oktaJQueryStatic from '../../../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import oktaUnderscore from '../../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import BaseView from '../../internals/BaseView.js';
import '../../components/AuthenticatorEnrollOptions.js';
import '../../components/AuthenticatorVerifyOptions.js';
import '../../../../util/FactorUtil.js';
import '../../../../v1/views/admin-consent/ScopeList.js';
import '../../../../v1/views/consent/ScopeList.js';
import '../captcha/CaptchaView.js';
import ConsentViewForm from './ConsentViewForm.js';
import AdminConsentViewHeader from './AdminConsentViewHeader.js';
import ConsentViewFooter from './EnduserConsentViewFooter.js';
import ScopeCheckBox from '../../components/ScopeCheckBox.js';

const granularConsentViewHeader = AdminConsentViewHeader.extend({
  hasIssuer: false,
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
      })) != null ? stack1 : "") + "<h1><span class=\"title-text\">" + container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(alias1, {
        "name": "i18n",
        "hash": {
          "$2": "<p>$2</p>",
          "$1": "<b class='no-translate'>$1</b>",
          "arguments": "appName",
          "bundle": "login",
          "code": "oie.consent.scopes.granular.title"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 375
          },
          "end": {
            "line": 1,
            "column": 511
          }
        }
      })) + "</span></h1>";
    },
    "useData": true
  })
});
const granularConsentViewForm = ConsentViewForm.extend({
  cancel: BaseForm.prototype.cancel,
  getUISchema: function () {
    const uiSchemas = BaseForm.prototype.getUISchema.apply(this, arguments);
    uiSchemas.forEach(schema => {
      if (schema.type === 'checkbox') {
        oktaUnderscore.assign(schema, {
          input: ScopeCheckBox,
          options: {
            desc: schema.desc,
            mutable: schema.mutable,
            // need to extract scope name because it is in a subform (optedScopes.name)
            scopeName: schema.name.substring(schema.name.indexOf('.') + 1)
          }
        });
      }
    });
    return uiSchemas;
  }
});
const GranularConsentAgreementText = View.extend({
  className: 'consent-description',
  template: _Handlebars2.template({
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
      var lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<p>" + container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "oie.consent.scopes.granular.description"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 3
          },
          "end": {
            "line": 1,
            "column": 73
          }
        }
      })) + "</p>";
    },
    "useData": true
  })
});
var GranularConsentView = BaseView.extend({
  Header: granularConsentViewHeader,
  Body: granularConsentViewForm,
  Footer: ConsentViewFooter,
  postRender: function () {
    const scopeList = this.$el.find('.o-form-fieldset-container'); // Show consent agreement text

    scopeList.before(new GranularConsentAgreementText().render().el); // Re-order scopes so mandatory ones are on bottom

    this.$(':disabled').each(function () {
      scopeList.append(oktaJQueryStatic(this).closest('.o-form-fieldset'));
    });
  }
});

export { GranularConsentView as default };
//# sourceMappingURL=GranularConsentView.js.map
