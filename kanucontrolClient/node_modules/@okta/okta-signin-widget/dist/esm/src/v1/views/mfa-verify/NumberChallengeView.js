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

var NumberChallengeView = View.extend({
  className: 'number-challenge-view',
  template: _Handlebars2.template({
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
      var stack1,
          helper,
          alias1 = depth0 != null ? depth0 : container.nullContext || {},
          alias2 = container.hooks.helperMissing,
          alias3 = container.escapeExpression,
          lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<p data-se=\"number-challenge-instruction\">" + ((stack1 = (lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "arguments": "number",
          "bundle": "login",
          "code": "oktaverify.numberchallenge.instruction"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 42
          },
          "end": {
            "line": 1,
            "column": 132
          }
        }
      })) != null ? stack1 : "") + "</p><div class=\"phone\"><div class=\"phone--body\"><div class=\"phone--screen\"><span class=\"phone--number\" data-se=\"challenge-number\">" + alias3((helper = (helper = lookupProperty(helpers, "number") || (depth0 != null ? lookupProperty(depth0, "number") : depth0)) != null ? helper : alias2, typeof helper === "function" ? helper.call(alias1, {
        "name": "number",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 262
          },
          "end": {
            "line": 1,
            "column": 272
          }
        }
      }) : helper)) + "</span></div><div class=\"phone--home-button\"></div></div></div><p>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "oktaverify.numberchallenge.explain"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 338
          },
          "end": {
            "line": 1,
            "column": 403
          }
        }
      })) + "</p>";
    },
    "useData": true
  }),
  initialize: function () {
    this.listenTo(this.options.appState, 'change:isWaitingForNumberChallenge', () => {
      if (this.options.appState.get('lastAuthResponse').status !== 'SUCCESS') {
        this.render();
      }
    });
  },
  getTemplateData: function () {
    const lastAuthResponse = this.options.appState.get('lastAuthResponse');

    if (!this.options.appState.get('isWaitingForNumberChallenge')) {
      return {
        number: null
      };
    }

    return {
      number: lastAuthResponse._embedded.factor._embedded.challenge.correctAnswer
    };
  }
});

export { NumberChallengeView as default };
//# sourceMappingURL=NumberChallengeView.js.map
