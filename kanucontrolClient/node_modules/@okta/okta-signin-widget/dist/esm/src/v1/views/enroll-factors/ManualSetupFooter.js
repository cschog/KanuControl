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
import Enums from '../../../util/Enums.js';

var ManualSetupFooter = View.extend({
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

      return "<a href=\"#\" class=\"link help js-back\" data-se=\"back-link\">" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "mfa.backToFactors"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 58
          },
          "end": {
            "line": 1,
            "column": 106
          }
        }
      })) + "</a><a href=\"#\" class=\"link help goto js-goto\" data-se=\"goto-link\">" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "mfa.scanBarcode"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 173
          },
          "end": {
            "line": 1,
            "column": 219
          }
        }
      })) + "</a>";
    },
    "useData": true
  }),
  className: 'auth-footer',
  events: {
    'click .js-back': function (e) {
      e.preventDefault();
      this.back();
    },
    'click .js-goto': function (e) {
      e.preventDefault(); // go to a different screen with current auth status:
      // refresh the latest response

      this.model.startTransaction(function (authClient) {
        return authClient.tx.resume();
      });
    }
  },
  back: function () {
    this.state.set('navigateDir', Enums.DIRECTION_BACK);

    if (this.options.appState.get('prevLink')) {
      // Once we are in the MFA_ENROLL_ACTIVATE, we need to reset to the
      // correct state. Fortunately, this means that the router will
      // handle navigation once the request is finished.
      this.model.doTransaction(function (transaction) {
        return transaction.prev();
      });
    } else {
      this.options.appState.trigger('navigate', 'signin/enroll');
    }
  }
});

export { ManualSetupFooter as default };
//# sourceMappingURL=ManualSetupFooter.js.map
