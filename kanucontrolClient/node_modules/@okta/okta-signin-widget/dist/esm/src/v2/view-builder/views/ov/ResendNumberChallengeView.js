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
import oktaUnderscore from '../../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import { WARNING_TIMEOUT } from '../../utils/Constants.js';

const ResendNumberChallengeView = View.extend({
  initialize: function () {
    this.listenTo(this.options.appState, 'showNumberChallengeWarning', () => {
      this.startWarningTimeout();
    });
    this.listenTo(this.options.appState, 'hideNumberChallengeWarning', () => {
      this.clearWarning();
    });
  },
  className: 'resend-number-challenge-warning hide',
  template: _Handlebars2.template({
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
      var lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<div class=\"okta-form-infobox-warning infobox infobox-warning\"><span class=\"icon warning-16\"></span><p>" + container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
        "name": "i18n",
        "hash": {
          "$1": "<a href='#' class='resend-number-challenge'>$1</a>",
          "bundle": "login",
          "code": "oie.numberchallenge.warning"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 103
          },
          "end": {
            "line": 1,
            "column": 217
          }
        }
      })) + "</p></div>";
    },
    "useData": true
  }),
  showWarning: function () {
    this.$el.removeClass('hide');
  },
  clearWarning: function () {
    this.$el.addClass('hide');
    clearTimeout(this.warningTimeout);
    this.startWarningTimeout();
  },
  startWarningTimeout: function () {
    this.warningTimeout = setTimeout(oktaUnderscore.bind(function () {
      this.showWarning();
    }, this), WARNING_TIMEOUT);
  }
});

export { ResendNumberChallengeView as default };
//# sourceMappingURL=ResendNumberChallengeView.js.map
