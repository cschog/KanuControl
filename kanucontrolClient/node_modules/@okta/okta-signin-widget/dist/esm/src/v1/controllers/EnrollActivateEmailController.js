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
import ResendEmailView from '../views/ResendEmailView.js';
import Footer from '../views/enroll-factors/Footer.js';

const Model = {
  props: {
    passCode: 'string'
  },
  resend: function () {
    this.trigger('form:clear-errors');
    return this.doTransaction(function (transaction) {
      // authn support multiple `resend` hence has to specify name.
      return transaction.resend('email');
    });
  },
  save: function () {
    this.trigger('save');
    const formData = this.toJSON();
    return this.doTransaction(function (transaction) {
      return transaction.activate(formData);
    });
  }
};

const Form = function () {
  return {
    title: oktaUnderscore.partial(loc, 'email.enroll.title', 'login'),
    noButtonBar: false,
    autoSave: true,
    save: oktaUnderscore.partial(loc, 'oform.verify', 'login'),
    // TODO: deprecated by mfa.challenge.verify
    formChildren: [// message
    FormType.View({
      View: View.extend({
        className: 'enroll-activate-email-content',
        attributes: {
          'data-se': 'enroll-activate-email-content'
        },
        // Why use `{{{` for the description?
        // - factorEmail is actually an HTML fragment which
        //   is created via another handlebar template and used for bold the email address.
        template: _Handlebars2.template({
          "compiler": [8, ">= 4.3.0"],
          "main": function (container, depth0, helpers, partials, data) {
            var stack1,
                lookupProperty = container.lookupProperty || function (parent, propertyName) {
              if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
                return parent[propertyName];
              }

              return undefined;
            };

            return (stack1 = (lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
              "name": "i18n",
              "hash": {
                "arguments": "factorEmail",
                "bundle": "login",
                "code": "email.mfa.email.sent.description"
              },
              "data": data,
              "loc": {
                "start": {
                  "line": 1,
                  "column": 0
                },
                "end": {
                  "line": 1,
                  "column": 89
                }
              }
            })) != null ? stack1 : "";
          },
          "useData": true
        }),
        getTemplateData: function () {
          const factor = this.options.appState.get('factor');
          const factorEmailVal = factor && factor.profile ? factor.profile.email : '';

          const factorEmail = _Handlebars2.template({
            "compiler": [8, ">= 4.3.0"],
            "main": function (container, depth0, helpers, partials, data) {
              var helper,
                  lookupProperty = container.lookupProperty || function (parent, propertyName) {
                if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
                  return parent[propertyName];
                }

                return undefined;
              };

              return "<span class=\"mask-email\">" + container.escapeExpression((helper = (helper = lookupProperty(helpers, "email") || (depth0 != null ? lookupProperty(depth0, "email") : depth0)) != null ? helper : container.hooks.helperMissing, typeof helper === "function" ? helper.call(depth0 != null ? depth0 : container.nullContext || {}, {
                "name": "email",
                "hash": {},
                "data": data,
                "loc": {
                  "start": {
                    "line": 1,
                    "column": 25
                  },
                  "end": {
                    "line": 1,
                    "column": 34
                  }
                }
              }) : helper)) + "</span>";
            },
            "useData": true
          })({
            email: factorEmailVal
          });

          return {
            factorEmail: factorEmail
          };
        }
      })
    }), // send again callout message and link button
    FormType.View({
      View: ResendEmailView
    }), // passcode input
    FormType.Input({
      label: loc('email.code.label', 'login'),
      'label-top': true,
      name: 'passCode',
      type: 'text',
      wide: true
    })]
  };
};

var EnrollActivateEmailController = FormController.extend({
  className: 'enroll-activate-email',
  Model: Model,
  Form: Form,
  Footer: Footer
});

export { EnrollActivateEmailController as default };
//# sourceMappingURL=EnrollActivateEmailController.js.map
