import _Handlebars2 from '../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { View, internal } from '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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

const {
  Util: Util
} = internal.util;
var Footer = View.extend({
  template: _Handlebars2.template({
    "1": function (container, depth0, helpers, partials, data) {
      var lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<a href=\"#\" class=\"link help js-skip\" data-se=\"skip-link\">" + container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "password.expiring.later"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 78
          },
          "end": {
            "line": 1,
            "column": 132
          }
        }
      })) + "</a>";
    },
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
      var stack1,
          alias1 = depth0 != null ? depth0 : container.nullContext || {},
          lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "passwordWarn") : depth0, {
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
            "column": 143
          }
        }
      })) != null ? stack1 : "") + "<a href=\"#\" class=\"link help goto js-signout\" data-se=\"signout-link\">" + container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "signout"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 212
          },
          "end": {
            "line": 1,
            "column": 250
          }
        }
      })) + "</a>";
    },
    "useData": true
  }),
  className: 'auth-footer clearfix',
  events: {
    'click .js-signout': function (e) {
      e.preventDefault();
      const self = this;
      const authClient = self.settings.authClient;
      this.model.doTransaction(function (transaction) {
        return transaction.cancel();
      }).then(function () {
        return authClient.session.exists();
      }).then(function (sessionExists) {
        if (sessionExists) {
          return authClient.closeSession().catch(() => {});
        }
      }).then(function () {
        if (self.settings.get('backToSignInUri')) {
          Util.redirect(self.settings.get('backToSignInUri'));
        } else {
          self.state.set('navigateDir', Enums.DIRECTION_BACK);
          self.options.appState.trigger('navigate', '');
        }
      });
    },
    'click .js-skip': function (e) {
      e.preventDefault();
      this.model.doTransaction(function (transaction) {
        return transaction.skip();
      });
    }
  },
  getTemplateData: function () {
    return {
      passwordWarn: this.options.appState.get('isPwdExpiringSoon')
    };
  }
});

export { Footer as default };
//# sourceMappingURL=Footer.js.map
