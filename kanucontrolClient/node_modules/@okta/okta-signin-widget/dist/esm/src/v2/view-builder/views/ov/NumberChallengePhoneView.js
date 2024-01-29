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

const NumberChallengePhoneView = View.extend({
  className: 'number-challenge-section',
  template: _Handlebars2.template({
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
      var helper,
          alias1 = depth0 != null ? depth0 : container.nullContext || {},
          alias2 = container.hooks.helperMissing,
          alias3 = container.escapeExpression,
          lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<p>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "$1": "<span class='strong'>$1</span>",
          "arguments": "correctAnswer",
          "bundle": "login",
          "code": "oie.numberchallenge.instruction"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 3
          },
          "end": {
            "line": 1,
            "column": 127
          }
        }
      })) + "</p><div class=\"phone no-translate\"><div class=\"phone--body\"><div class=\"phone--screen\"><span class=\"phone--number\" data-se=\"challenge-number\">" + alias3((helper = (helper = lookupProperty(helpers, "correctAnswer") || (depth0 != null ? lookupProperty(depth0, "correctAnswer") : depth0)) != null ? helper : alias2, typeof helper === "function" ? helper.call(alias1, {
        "name": "correctAnswer",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 270
          },
          "end": {
            "line": 1,
            "column": 287
          }
        }
      }) : helper)) + "</span></div><div class=\"phone--home-button\"></div></div></div>";
    },
    "useData": true
  }),
  getTemplateData: function () {
    var _this$options$appStat, _this$options$appStat2;

    const correctAnswer = (_this$options$appStat = this.options.appState.get('currentAuthenticator')) === null || _this$options$appStat === void 0 ? void 0 : (_this$options$appStat2 = _this$options$appStat.contextualData) === null || _this$options$appStat2 === void 0 ? void 0 : _this$options$appStat2.correctAnswer;
    return {
      correctAnswer: correctAnswer
    };
  }
});

export { NumberChallengePhoneView as default };
//# sourceMappingURL=NumberChallengePhoneView.js.map
