import _Handlebars2 from '../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { internal } from '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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

let {
  TextBox: TextBox
} = internal.views.forms.inputs;
var PhoneTextBox = TextBox.extend({
  template: _Handlebars2.template({
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
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

      return "<span class=\"okta-form-label-inline o-form-label-inline\">" + alias4((helper = (helper = lookupProperty(helpers, "countryCallingCode") || (depth0 != null ? lookupProperty(depth0, "countryCallingCode") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "countryCallingCode",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 57
          },
          "end": {
            "line": 1,
            "column": 79
          }
        }
      }) : helper)) + "</span><span class=\"okta-form-input-field input-fix o-form-control\"><input type=\"" + alias4((helper = (helper = lookupProperty(helpers, "type") || (depth0 != null ? lookupProperty(depth0, "type") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "type",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 160
          },
          "end": {
            "line": 1,
            "column": 168
          }
        }
      }) : helper)) + "\" placeholder=\"" + alias4((helper = (helper = lookupProperty(helpers, "placeholder") || (depth0 != null ? lookupProperty(depth0, "placeholder") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "placeholder",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 183
          },
          "end": {
            "line": 1,
            "column": 198
          }
        }
      }) : helper)) + "\" name=\"" + alias4((helper = (helper = lookupProperty(helpers, "name") || (depth0 != null ? lookupProperty(depth0, "name") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "name",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 206
          },
          "end": {
            "line": 1,
            "column": 214
          }
        }
      }) : helper)) + "\" id=\"" + alias4((helper = (helper = lookupProperty(helpers, "inputId") || (depth0 != null ? lookupProperty(depth0, "inputId") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "inputId",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 220
          },
          "end": {
            "line": 1,
            "column": 231
          }
        }
      }) : helper)) + "\" value=\"" + alias4((helper = (helper = lookupProperty(helpers, "value") || (depth0 != null ? lookupProperty(depth0, "value") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "value",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 240
          },
          "end": {
            "line": 1,
            "column": 249
          }
        }
      }) : helper)) + "\" autocomplete=\"tel\"></span>";
    },
    "useData": true
  }),
  initialize: function () {
    this.listenTo(this.model, 'change:countryCallingCode', function () {
      this.$('.o-form-label-inline').text(this.model.get('countryCallingCode'));
    });
  },
  preRender: function () {
    this.options.countryCallingCode = this.model.get('countryCallingCode');
  },
  postRender: function () {
    // This is a hack - once inputGroups are done, get rid of it!!
    this.$el.removeClass('input-fix o-form-control');

    oktaUnderscore.defer(() => {
      this.$el.parent().addClass('o-form-input-group');
    });
  }
});

export { PhoneTextBox as default };
//# sourceMappingURL=PhoneTextBox.js.map
