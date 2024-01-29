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
import oktaUnderscore from '../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import '../../../../packages/@okta/qtip2/dist/jquery.qtip.js';

var ScopeItem = View.extend({
  className: 'scope-item',
  template: _Handlebars2.template({
    "1": function (container, depth0, helpers, partials, data) {
      return "<span class=\"scope-item-tooltip icon form-help-16\"></span>";
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

      return "<div class=\"" + alias4((helper = (helper = lookupProperty(helpers, "classNames") || (depth0 != null ? lookupProperty(depth0, "classNames") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "classNames",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 12
          },
          "end": {
            "line": 1,
            "column": 26
          }
        }
      }) : helper)) + "\"><p>" + alias4((helper = (helper = lookupProperty(helpers, "name") || (depth0 != null ? lookupProperty(depth0, "name") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "name",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 31
          },
          "end": {
            "line": 1,
            "column": 39
          }
        }
      }) : helper)) + "</p></div>" + ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "description") : depth0, {
        "name": "if",
        "hash": {},
        "fn": container.program(1, data, 0),
        "inverse": container.noop,
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 49
          },
          "end": {
            "line": 1,
            "column": 133
          }
        }
      })) != null ? stack1 : "");
    },
    "useData": true
  }),
  getTemplateData: function () {
    const {
      name: name,
      description: description,
      isCustomized: isCustomized
    } = this.options;
    const baseClass = 'scope-item-text';
    const classNames = name === 'openid' || isCustomized ? `${baseClass} no-translate` : baseClass;
    return {
      classNames: classNames,
      name: name,
      description: description
    };
  },
  postRender: function () {
    this.$('.scope-item-tooltip').qtip({
      content: {
        text: oktaUnderscore.escape(this.options.description)
      },
      style: {
        classes: 'okta-tooltip qtip-custom qtip-shadow'
      },
      position: {
        my: 'bottom right',
        target: 'mouse'
      }
    });
  }
});

export { ScopeItem as default };
//# sourceMappingURL=ScopeItem.js.map
