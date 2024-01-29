import _Handlebars2 from '../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { View, createButton, loc } from '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import Backbone_ListView from '../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import fn$1 from '../../../util/FactorUtil.js';
import fn from '../../util/RouterUtil.js';

const cardinalityTextTpl = _Handlebars2.template({
  "compiler": [8, ">= 4.3.0"],
  "main": function (container, depth0, helpers, partials, data) {
    var helper,
        lookupProperty = container.lookupProperty || function (parent, propertyName) {
      if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
        return parent[propertyName];
      }

      return undefined;
    };

    return "<span class=\"factor-cardinality\">" + container.escapeExpression((helper = (helper = lookupProperty(helpers, "cardinalityText") || (depth0 != null ? lookupProperty(depth0, "cardinalityText") : depth0)) != null ? helper : container.hooks.helperMissing, typeof helper === "function" ? helper.call(depth0 != null ? depth0 : container.nullContext || {}, {
      "name": "cardinalityText",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 33
        },
        "end": {
          "line": 1,
          "column": 52
        }
      }
    }) : helper)) + "</span>";
  },
  "useData": true
});

const FactorListFactorRow = View.extend({
  tagName: 'li',
  className: 'enroll-factor-row clearfix',
  template: _Handlebars2.template({
    "1": function (container, depth0, helpers, partials, data) {
      var helper,
          lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<p>" + container.escapeExpression((helper = (helper = lookupProperty(helpers, "factorDescription") || (depth0 != null ? lookupProperty(depth0, "factorDescription") : depth0)) != null ? helper : container.hooks.helperMissing, typeof helper === "function" ? helper.call(depth0 != null ? depth0 : container.nullContext || {}, {
        "name": "factorDescription",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 235
          },
          "end": {
            "line": 1,
            "column": 256
          }
        }
      }) : helper)) + "</p>";
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

      return "<div class=\"enroll-factor-icon-container\"><div class=\"factor-icon enroll-factor-icon " + alias4((helper = (helper = lookupProperty(helpers, "iconClassName") || (depth0 != null ? lookupProperty(depth0, "iconClassName") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "iconClassName",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 85
          },
          "end": {
            "line": 1,
            "column": 102
          }
        }
      }) : helper)) + "\"></div></div><div class=\"enroll-factor-description\"><h3 class=\"enroll-factor-label\">" + alias4((helper = (helper = lookupProperty(helpers, "factorLabel") || (depth0 != null ? lookupProperty(depth0, "factorLabel") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "factorLabel",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 187
          },
          "end": {
            "line": 1,
            "column": 202
          }
        }
      }) : helper)) + "</h3>" + ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "factorDescription") : depth0, {
        "name": "if",
        "hash": {},
        "fn": container.program(1, data, 0),
        "inverse": container.noop,
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 207
          },
          "end": {
            "line": 1,
            "column": 267
          }
        }
      })) != null ? stack1 : "") + "<div class=\"enroll-factor-button\"></div></div>";
    },
    "useData": true
  }),
  attributes: function () {
    return {
      'data-se': this.model.get('factorName')
    };
  },
  children: function () {
    const children = [];
    const enrolled = this.model.get('enrolled');
    const required = this.model.get('required');
    const cardinality = this.model.get('cardinality');

    if (this.options.showInlineSetupButton) {
      return [[createButton({
        className: 'button',
        title: this.getSetupButtonText(),
        click: function () {
          this.options.appState.trigger('navigate', fn.createEnrollFactorUrl(this.model.get('provider'), this.model.get('factorType')));
        }
      }), '.enroll-factor-button']];
    } else if (enrolled) {
      children.push(['<span class="icon success-16-green"></span>', '.enroll-factor-label']);
    } else if (required) {
      children.push(['<span class="icon success-16-gray"></span>', '.enroll-factor-label']);
    }

    const cardinalityText = fn$1.getCardinalityText(enrolled, required, cardinality);

    if (cardinalityText) {
      children.push([cardinalityTextTpl({
        cardinalityText: cardinalityText
      }), '.enroll-factor-description']);
    }

    return children;
  },
  minimize: function () {
    this.$el.addClass('enroll-factor-row-min');
  },
  maximize: function () {
    this.$el.removeClass('enroll-factor-row-min');
  },
  getSetupButtonText: function () {
    return this.model.get('additionalEnrollment') ? loc('enroll.choices.setup.another', 'login') : loc('enroll.choices.setup', 'login');
  }
});
var FactorList = Backbone_ListView.extend({
  className: 'enroll-factor-list',
  item: FactorListFactorRow,
  itemSelector: '.list-content',
  template: _Handlebars2.template({
    "1": function (container, depth0, helpers, partials, data) {
      var helper,
          lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<h3 class=\"list-title\">" + container.escapeExpression((helper = (helper = lookupProperty(helpers, "listTitle") || (depth0 != null ? lookupProperty(depth0, "listTitle") : depth0)) != null ? helper : container.hooks.helperMissing, typeof helper === "function" ? helper.call(depth0 != null ? depth0 : container.nullContext || {}, {
        "name": "listTitle",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 40
          },
          "end": {
            "line": 1,
            "column": 53
          }
        }
      }) : helper)) + "</h3>";
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

      return ((stack1 = lookupProperty(helpers, "if").call(depth0 != null ? depth0 : container.nullContext || {}, depth0 != null ? lookupProperty(depth0, "listTitle") : depth0, {
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
            "column": 65
          }
        }
      })) != null ? stack1 : "") + "<ul class=\"list-content\"></ul>";
    },
    "useData": true
  }),
  getTemplateData: function () {
    const json = Backbone_ListView.prototype.getTemplateData.call(this);

    oktaUnderscore.extend(json, this);

    return json;
  },
  postRender: function () {
    if (this.options.minimize) {
      this.invoke('minimize');
    }
  }
});

export { FactorList as default };
//# sourceMappingURL=FactorList.js.map
