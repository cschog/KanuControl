import _Handlebars2 from '../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { View } from '../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import oktaUnderscore from '../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import Enums from '../../util/Enums.js';

var ResendEmailView = View.extend({
  className: 'hide resend-email-infobox',
  template: _Handlebars2.template({
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
      var alias1 = depth0 != null ? depth0 : container.nullContext || {},
          alias2 = container.hooks.helperMissing,
          alias3 = container.escapeExpression,
          lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<div class=\"infobox infobox-warning\" aria-live=\"polite\"><span class=\"icon warning-16\"></span><p><span>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "email.code.not.received"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 102
          },
          "end": {
            "line": 1,
            "column": 156
          }
        }
      })) + "</span><a href=\"#\" class=\"resend-email-btn\">" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "email.button.resend"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 200
          },
          "end": {
            "line": 1,
            "column": 250
          }
        }
      })) + "</a></p></div>";
    },
    "useData": true
  }),
  events: {
    'click .resend-email-btn': 'resendEmail'
  },
  postRender: function () {
    this.showResendCallout();
  },
  showResendCallout: function () {
    oktaUnderscore.delay(() => {
      this.$el.removeClass('hide');
    }, Enums.API_RATE_LIMIT);
  },
  hideResendCallout: function () {
    this.$el.addClass('hide');
  },
  resendEmail: function (e) {
    e.preventDefault();
    this.hideResendCallout();
    this.model.resend().finally(this.showResendCallout.bind(this));
  }
});

export { ResendEmailView as default };
//# sourceMappingURL=ResendEmailView.js.map
