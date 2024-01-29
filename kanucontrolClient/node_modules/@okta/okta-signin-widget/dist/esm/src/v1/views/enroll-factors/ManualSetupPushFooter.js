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
import oktaUnderscore from '../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import fn from '../../util/RouterUtil.js';

function goToFactorActivation(appState) {
  const url = fn.createActivateFactorUrl(appState.get('activatedFactorProvider'), appState.get('activatedFactorType'));
  appState.trigger('navigate', url);
}

var Footer = View.extend({
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
      e.preventDefault();

      const goToFactor = oktaUnderscore.partial(goToFactorActivation, this.options.appState);

      this.options.appState.unset('factorActivationType');
      this.model.doTransaction(function (transaction) {
        return transaction.prev().then(function (trans) {
          const factor = oktaUnderscore.findWhere(trans.factors, {
            factorType: 'push',
            provider: 'OKTA'
          });

          return factor.enroll();
        });
      }).then(goToFactor);
    }
  },
  back: function () {
    const self = this;
    self.options.appState.unset('factorActivationType');

    if (self.options.appState.get('prevLink')) {
      this.model.doTransaction(function (transaction) {
        return transaction.prev();
      }).then(function () {
        // we trap 'MFA_ENROLL' response that's why we need to trigger navigation from here
        self.options.appState.trigger('navigate', 'signin/enroll');
      });
    } else {
      self.options.appState.trigger('navigate', 'signin/enroll');
    }
  }
});

export { Footer as default };
//# sourceMappingURL=ManualSetupPushFooter.js.map
