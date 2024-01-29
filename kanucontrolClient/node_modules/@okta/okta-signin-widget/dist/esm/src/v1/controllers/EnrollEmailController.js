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
import FormController from '../util/FormController.js';
import FormType from '../util/FormType.js';
import Footer from '../views/enroll-factors/Footer.js';

const Model = {
  local: {
    factorType: 'string',
    provider: 'string'
  },
  save: function () {
    this.trigger('save');
    const factorOpt = this.pick('factorType', 'provider');
    return this.doTransaction(function (transaction) {
      const factor = oktaUnderscore.findWhere(transaction.factors, factorOpt);

      return factor.enroll();
    });
  }
};

const Form = function () {
  return {
    title: oktaUnderscore.partial(loc, 'email.enroll.title', 'login'),
    noButtonBar: false,
    autoSave: true,
    save: oktaUnderscore.partial(loc, 'email.button.send', 'login'),
    formChildren: [FormType.View({
      View: View.extend({
        attributes: {
          'data-se': 'enroll-email-content'
        },
        template: _Handlebars2.template({
          "compiler": [8, ">= 4.3.0"],
          "main": function (container, depth0, helpers, partials, data) {
            var lookupProperty = container.lookupProperty || function (parent, propertyName) {
              if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
                return parent[propertyName];
              }

              return undefined;
            };

            return container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
              "name": "i18n",
              "hash": {
                "bundle": "login",
                "code": "email.enroll.description"
              },
              "data": data,
              "loc": {
                "start": {
                  "line": 1,
                  "column": 0
                },
                "end": {
                  "line": 1,
                  "column": 55
                }
              }
            }));
          },
          "useData": true
        })
      })
    })]
  };
};

var EnrollEmailController = FormController.extend({
  className: 'enroll-email',
  Model: Model,
  Form: Form,
  Footer: Footer,
  initialize: function () {
    this.model.set(oktaUnderscore.pick(this.options, 'factorType', 'provider'));
  }
});

export { EnrollEmailController as default };
//# sourceMappingURL=EnrollEmailController.js.map
