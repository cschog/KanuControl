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

const generatePasswordPolicyHtml = function (form, rulesList, prepend) {
  form.add(View.extend({
    tagName: 'section',
    template: _Handlebars2.template({
      "1": function (container, depth0, helpers, partials, data) {
        return "<li>" + container.escapeExpression(container.lambda(depth0, depth0)) + "</li>";
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

        return "<div class=\"password-authenticator--heading\">" + container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(alias1, {
          "name": "i18n",
          "hash": {
            "bundle": "login",
            "code": "password.complexity.requirements.header"
          },
          "data": data,
          "loc": {
            "start": {
              "line": 1,
              "column": 45
            },
            "end": {
              "line": 1,
              "column": 115
            }
          }
        })) + "</div><ul class=\"password-authenticator--list\">" + ((stack1 = lookupProperty(helpers, "each").call(alias1, depth0 != null ? lookupProperty(depth0, "rulesList") : depth0, {
          "name": "each",
          "hash": {},
          "fn": container.program(1, data, 0),
          "inverse": container.noop,
          "data": data,
          "loc": {
            "start": {
              "line": 1,
              "column": 162
            },
            "end": {
              "line": 1,
              "column": 207
            }
          }
        })) != null ? stack1 : "") + "</ul>";
      },
      "useData": true
    }),
    getTemplateData: () => ({
      rulesList: rulesList
    }),
    attributes: {
      'data-se': 'password-authenticator--rules'
    }
  }), {
    prepend: prepend,
    selector: '.o-form-fieldset-container'
  });
};

export { generatePasswordPolicyHtml };
//# sourceMappingURL=PasswordPolicyUtil.js.map
