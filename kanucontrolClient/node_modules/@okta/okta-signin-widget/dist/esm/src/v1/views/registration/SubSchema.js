import _Handlebars2 from '../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { View } from '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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

const SubSchemaSubSchema = View.extend({
  index: '',
  message: '',
  class: function () {
    return;
  },
  className: function () {
    return 'subschema-unsatisfied subschema-' + this.index;
  },
  template: _Handlebars2.template({
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
      var helper,
          lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<p class=\"default-schema\"><span class=\"icon icon-16\"></span>" + container.escapeExpression((helper = (helper = lookupProperty(helpers, "message") || (depth0 != null ? lookupProperty(depth0, "message") : depth0)) != null ? helper : container.hooks.helperMissing, typeof helper === "function" ? helper.call(depth0 != null ? depth0 : container.nullContext || {}, {
        "name": "message",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 60
          },
          "end": {
            "line": 1,
            "column": 71
          }
        }
      }) : helper)) + "</p>";
    },
    "useData": true
  }),
  getTemplateData: function () {
    return {
      message: this.message
    };
  }
});
var SubSchema = View.extend({
  className: 'subschema',
  children: function () {
    return this.subSchemas.map(function (subSchema, index) {
      const description = subSchema.get('description');
      const message = description; // TODO API should send translated strings instead of i18n code inside description
      // or send param with i18n code

      return SubSchemaSubSchema.extend({
        index: index,
        message: message
      });
    });
  }
});

export { SubSchema as default };
//# sourceMappingURL=SubSchema.js.map
