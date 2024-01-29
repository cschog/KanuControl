import _Handlebars2 from '../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { View, loc, internal } from '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import Enums from '../../../util/Enums.js';

const {
  Util: Util
} = internal.util;
var FooterSignout = View.extend({
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

      return "<a href=\"#\" class=\"link " + alias4((helper = (helper = lookupProperty(helpers, "linkClassName") || (depth0 != null ? lookupProperty(depth0, "linkClassName") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "linkClassName",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 24
          },
          "end": {
            "line": 1,
            "column": 41
          }
        }
      }) : helper)) + "\" data-se=\"signout-link\">" + alias4((helper = (helper = lookupProperty(helpers, "linkText") || (depth0 != null ? lookupProperty(depth0, "linkText") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "linkText",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 66
          },
          "end": {
            "line": 1,
            "column": 78
          }
        }
      }) : helper)) + "</a>";
    },
    "useData": true
  }),
  className: 'auth-footer clearfix',
  events: {
    'click a[data-se="signout-link"]': 'handleSignout'
  },
  handleSignout: function (e) {
    e.preventDefault();
    const appState = this.options.appState;
    appState.trigger('signOut');
    const isSMSPasswordRecovery = appState.get('isSMSPasswordRecovery');
    this.model.doTransaction(function (transaction) {
      // `clearTransactionData` was added as a temporary fix for the issue reported in OKTA-487220
      // OKTA-515889 was created to address the underlying issue on the `transaction.cancel` level
      // and this temporary fix will be removed after that ticket is done
      return transaction.cancel().then(clearTransactionData);
    }).then(() => {
      if (this.settings.get('backToSignInUri') && !isSMSPasswordRecovery) {
        Util.redirect(this.settings.get('backToSignInUri'));
      } else {
        this.state.set('navigateDir', Enums.DIRECTION_BACK);
        appState.trigger('navigate', '');
      }
    });
  },
  getTemplateData: function () {
    return {
      linkClassName: oktaUnderscore.isUndefined(this.options.linkClassName) ? 'goto' : this.options.linkClassName,
      linkText: this.options.linkText || loc('goback', 'login')
    };
  }
}); // TODO: remove after OKTA-515889 is done

function clearTransactionData(transaction) {
  return { ...transaction,
    data: null
  };
}

export { FooterSignout as default };
//# sourceMappingURL=FooterSignout.js.map
