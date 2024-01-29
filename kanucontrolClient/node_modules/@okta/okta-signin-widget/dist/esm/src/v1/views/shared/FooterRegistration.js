import _Handlebars2 from '../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { View, loc } from '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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

var FooterRegistration = View.extend({
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

      return "<div class=\"content-container\"><span class=\"registration-label\">" + alias4((helper = (helper = lookupProperty(helpers, "label") || (depth0 != null ? lookupProperty(depth0, "label") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "label",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 64
          },
          "end": {
            "line": 1,
            "column": 73
          }
        }
      }) : helper)) + "</span><a title=\"" + alias4((helper = (helper = lookupProperty(helpers, "text") || (depth0 != null ? lookupProperty(depth0, "text") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "text",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 90
          },
          "end": {
            "line": 1,
            "column": 98
          }
        }
      }) : helper)) + "\" aria-label=\"" + alias4((helper = (helper = lookupProperty(helpers, "text") || (depth0 != null ? lookupProperty(depth0, "text") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "text",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 112
          },
          "end": {
            "line": 1,
            "column": 120
          }
        }
      }) : helper)) + "\" class=\"registration-link\" href=\"#\">" + alias4((helper = (helper = lookupProperty(helpers, "text") || (depth0 != null ? lookupProperty(depth0, "text") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "text",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 157
          },
          "end": {
            "line": 1,
            "column": 165
          }
        }
      }) : helper)) + "</a></div>";
    },
    "useData": true
  }),
  className: 'registration-container',
  events: {
    'click a.registration-link': 'handleClickEvent'
  },
  handleClickEvent: function (e) {
    e.preventDefault();
    const clickHandler = this.settings.get('registration.click');

    if (clickHandler) {
      clickHandler();
    } else if (this.options.appState.get('isIdxStateToken')) {
      this.options.appState.trigger('navigate', 'signin/enroll-user');
    } else {
      this.options.appState.trigger('navigate', 'signin/register');
    }
  },
  getTemplateData: function () {
    const templateData = {
      label: loc('registration.signup.label', 'login'),
      text: loc('registration.signup.text', 'login')
    };
    return templateData;
  }
});

export { FooterRegistration as default };
//# sourceMappingURL=FooterRegistration.js.map
