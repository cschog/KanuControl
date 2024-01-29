import _Handlebars2 from '../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { View, loc } from '../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import Enums from '../../util/Enums.js';
import FormController from '../util/FormController.js';
import FormType from '../util/FormType.js';

var RegistrationCompleteController = FormController.extend({
  events: {
    'click .back-btn': function (e) {
      e.preventDefault();
      this.back();
    }
  },
  back: function () {
    this.state.set('navigateDir', Enums.DIRECTION_BACK);
    this.options.appState.trigger('navigate', '');
  },
  className: 'registration-complete',
  Model: function () {},
  initialize: function () {
    this.settings.callGlobalSuccess(Enums.ACTIVATION_EMAIL_SENT, {
      username: this.options.appState.get('username')
    });
  },
  Form: {
    noButtonBar: true,
    formChildren: function () {
      return [FormType.View({
        View: View.extend({
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

              return "<div class=\"container\"><span class=\"title-icon icon icon-16 confirm-16-green\"></span><h2 class=\"title\">" + alias4((helper = (helper = lookupProperty(helpers, "title") || (depth0 != null ? lookupProperty(depth0, "title") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
                "name": "title",
                "hash": {},
                "data": data,
                "loc": {
                  "start": {
                    "line": 1,
                    "column": 103
                  },
                  "end": {
                    "line": 1,
                    "column": 112
                  }
                }
              }) : helper)) + "</h2><div class=\"desc\">" + alias4((helper = (helper = lookupProperty(helpers, "desc") || (depth0 != null ? lookupProperty(depth0, "desc") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
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
              }) : helper)) + "</div></div><a href=\"#\" class=\"back-btn\" data-se=\"back-link\">" + alias4((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
                "name": "i18n",
                "hash": {
                  "bundle": "login",
                  "code": "goback"
                },
                "data": data,
                "loc": {
                  "start": {
                    "line": 1,
                    "column": 204
                  },
                  "end": {
                    "line": 1,
                    "column": 241
                  }
                }
              })) + "</a>";
            },
            "useData": true
          }),
          getTemplateData: function () {
            return {
              desc: loc('registration.complete.confirm.text', 'login'),
              title: loc('registration.complete.title', 'login')
            };
          }
        })
      })];
    }
  }
});

export { RegistrationCompleteController as default };
//# sourceMappingURL=RegistrationCompleteController.js.map
