import _Handlebars2 from '../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { loc, View } from '../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import fn from '../../util/FactorUtil.js';
import FormController from '../util/FormController.js';
import FormType from '../util/FormType.js';
import fn$1 from '../util/RouterUtil.js';
import ManualSetupFooter from '../views/enroll-factors/ManualSetupFooter.js';

var ManualSetupTotpController = FormController.extend({
  className: 'enroll-manual-totp',
  Model: function () {
    return {
      local: {
        sharedSecret: ['string', false, this.options.appState.get('sharedSecret')],
        __factorType__: ['string', false, this.options.factorType],
        __provider__: ['string', false, this.options.provider]
      }
    };
  },
  Form: {
    title: function () {
      const factorName = fn.getFactorLabel(this.model.get('__provider__'), this.model.get('__factorType__'));
      return loc('enroll.totp.title', 'login', [factorName]);
    },
    subtitle: oktaUnderscore.partial(loc, 'enroll.totp.cannotScanBarcode', 'login'),
    noButtonBar: true,
    attributes: {
      'data-se': 'step-manual-setup'
    },
    formChildren: function () {
      const instructions = this.settings.get('brandName') ? loc('enroll.totp.manualSetupInstructions.specific', 'login', [this.settings.get('brandName')]) : loc('enroll.totp.manualSetupInstructions.generic', 'login');
      return [FormType.View({
        View: View.extend({
          className: 'secret-key-instructions',
          attributes: {
            'data-se': 'secret-key-instructions'
          },
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

              return "<section aria-live=\"assertive\"><p class=\"okta-form-subtitle o-form-explain text-align-c\">" + alias4((helper = (helper = lookupProperty(helpers, "instructions") || (depth0 != null ? lookupProperty(depth0, "instructions") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
                "name": "instructions",
                "hash": {},
                "data": data,
                "loc": {
                  "start": {
                    "line": 1,
                    "column": 89
                  },
                  "end": {
                    "line": 1,
                    "column": 105
                  }
                }
              }) : helper)) + "</p><p class=\"shared-key margin-top-10\" tabindex=0 aria-label=\"" + alias4((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
                "name": "i18n",
                "hash": {
                  "arguments": "sharedSecretKey",
                  "bundle": "login",
                  "code": "enroll.totp.sharedSecretInstructions.aria.secretKey"
                },
                "data": data,
                "loc": {
                  "start": {
                    "line": 1,
                    "column": 168
                  },
                  "end": {
                    "line": 1,
                    "column": 278
                  }
                }
              })) + "\">" + alias4((helper = (helper = lookupProperty(helpers, "sharedSecretKey") || (depth0 != null ? lookupProperty(depth0, "sharedSecretKey") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
                "name": "sharedSecretKey",
                "hash": {},
                "data": data,
                "loc": {
                  "start": {
                    "line": 1,
                    "column": 280
                  },
                  "end": {
                    "line": 1,
                    "column": 299
                  }
                }
              }) : helper)) + "</p></section>";
            },
            "useData": true
          }),
          initialize: function () {
            this.listenTo(this.model, 'change:sharedSecret', this.render);
          },
          getTemplateData: function () {
            return {
              instructions: instructions,
              sharedSecretKey: this.model.get('sharedSecret')
            };
          }
        })
      }), FormType.Toolbar({
        noCancelButton: true,
        save: loc('oform.next', 'login')
      })];
    }
  },
  Footer: ManualSetupFooter,
  initialize: function () {
    this.listenTo(this.form, 'save', function () {
      const url = fn$1.createActivateFactorUrl(this.model.get('__provider__'), this.model.get('__factorType__'), 'activate');
      this.options.appState.trigger('navigate', url);
    });
  }
});

export { ManualSetupTotpController as default };
//# sourceMappingURL=ManualSetupTotpController.js.map
