import _Handlebars2 from '../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { loc, internal } from '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import { doesI18NKeyExist } from '../../ion/i18nTransformer.js';

const CheckBox = internal.views.forms.inputs.CheckBox;
var ScopeCheckBox = CheckBox.extend({
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

      return "<input type=\"checkbox\" name=\"" + alias4((helper = (helper = lookupProperty(helpers, "name") || (depth0 != null ? lookupProperty(depth0, "name") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "name",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 29
          },
          "end": {
            "line": 1,
            "column": 37
          }
        }
      }) : helper)) + "\" id=\"" + alias4((helper = (helper = lookupProperty(helpers, "inputId") || (depth0 != null ? lookupProperty(depth0, "inputId") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "inputId",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 43
          },
          "end": {
            "line": 1,
            "column": 54
          }
        }
      }) : helper)) + "\"/><label for=\"" + alias4((helper = (helper = lookupProperty(helpers, "inputId") || (depth0 != null ? lookupProperty(depth0, "inputId") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "inputId",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 69
          },
          "end": {
            "line": 1,
            "column": 80
          }
        }
      }) : helper)) + "\" data-se-for-name=\"" + alias4((helper = (helper = lookupProperty(helpers, "name") || (depth0 != null ? lookupProperty(depth0, "name") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "name",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 100
          },
          "end": {
            "line": 1,
            "column": 108
          }
        }
      }) : helper)) + "\"><b>" + alias4((helper = (helper = lookupProperty(helpers, "placeholder") || (depth0 != null ? lookupProperty(depth0, "placeholder") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "placeholder",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 113
          },
          "end": {
            "line": 1,
            "column": 128
          }
        }
      }) : helper)) + "</b><p>" + alias4((helper = (helper = lookupProperty(helpers, "desc") || (depth0 != null ? lookupProperty(depth0, "desc") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "desc",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 135
          },
          "end": {
            "line": 1,
            "column": 143
          }
        }
      }) : helper)) + "</p></label>";
    },
    "useData": true
  }),
  getLocalizedLabel: function () {
    const key = `consent.scopes.${this.options.options.scopeName}.label`;
    return doesI18NKeyExist(key) ? loc(key, 'login') : this.options.placeholder || this.options.options.scopeName;
  },
  getLocalizedDesc: function () {
    const key = `consent.scopes.${this.options.options.scopeName}.desc`;
    return doesI18NKeyExist(key) ? loc(key, 'login') : this.options.options.desc;
  },
  isCustomizedScope: function () {
    return !doesI18NKeyExist(`consent.scopes.${this.options.options.scopeName}.label`);
  },

  /**
   * @Override
   */
  enable: function () {
    if (this.options.options.mutable) {
      this.$(':input').prop('disabled', false);
    }
  },

  /**
   * @Override
   */
  editMode: function () {
    this.$el.html(this.template(oktaUnderscore.extend(oktaUnderscore.omit(this.options, 'placeholder'), {
      placeholder: this.getLocalizedLabel(),
      desc: this.getLocalizedDesc()
    })));
    this.$(':checkbox').prop('checked', this.getModelValue() || false);
    this.$('input').customInput();
    this.model.trigger('form:resize');

    if (!this.options.options.mutable) {
      const input = this.$('input').get(0);
      this.$(input.parentElement).addClass('o-form-read-mode');
      this.$(':checkbox').prop('disabled', true);
    }

    if (this.options.options.scopeName === 'openid' || this.isCustomizedScope()) {
      this.$('label > b').addClass('no-translate');

      if (this.isCustomizedScope()) {
        this.$('label > p').addClass('no-translate');
      }
    }

    return this;
  }
});

export { ScopeCheckBox as default };
//# sourceMappingURL=ScopeCheckBox.js.map
