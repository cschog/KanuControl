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
import '../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import FormController from '../util/FormController.js';
import fn from '../util/RouterUtil.js';
import form from '../views/enroll-factors/EnterPasscodeForm.js';

const EnterPasscodePushFlowControllerFooter = View.extend({
  template: _Handlebars2.template({
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
      var lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<a href=\"#\" class=\"link help js-back\" data-se=\"back-link\">" + container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "oform.back"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 58
          },
          "end": {
            "line": 1,
            "column": 99
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
    }
  },
  back: function () {
    const url = fn.createActivateFactorUrl(this.options.appState.get('activatedFactorProvider'), 'push', 'manual');
    this.options.appState.trigger('navigate', url);
  }
});
var EnterPasscodePushFlowController = FormController.extend({
  className: 'activate-push',
  Model: function () {
    return {
      props: {
        factorId: ['string', true, this.options.appState.get('activatedFactorId')],
        passCode: ['string', true]
      },
      local: {
        __factorType__: ['string', false, this.options.factorType],
        __provider__: ['string', false, this.options.provider]
      },
      save: function () {
        return this.doTransaction(function (transaction) {
          return transaction.activate({
            passCode: this.get('passCode')
          });
        });
      }
    };
  },
  Form: form,
  Footer: EnterPasscodePushFlowControllerFooter
});

export { EnterPasscodePushFlowController as default };
//# sourceMappingURL=EnterPasscodePushFlowController.js.map
