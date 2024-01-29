import _Handlebars2 from '../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { Form, loc, View } from '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import ResendEmailView from '../ResendEmailView.js';
import TextBox from '../shared/TextBox.js';

const createEmailMaskElement = function () {
  const email = this.model.get('email');

  const emailTpl = _Handlebars2.template({
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
  });

  return {
    factorEmail: emailTpl({
      email: email
    })
  };
};

const SendEmailAndVerifyCodeFormVerifyEmailCodeForm = Form.extend({
  layout: 'o-form-theme',
  className: 'mfa-verify-email',
  title: oktaUnderscore.partial(loc, 'email.mfa.title', 'login'),
  noButtonBar: false,
  autoSave: true,
  noCancelButton: true,
  attributes: {
    'data-se': 'factor-email'
  },
  save: function () {
    return this.options.appState.get('isMfaChallenge') ? loc('mfa.challenge.verify', 'login') : loc('email.button.send', 'login');
  },
  events: Object.assign({}, Form.prototype.events, {
    submit: function (e) {
      e.preventDefault();
      this.handleSubmit();
    }
  }),
  handleSubmit: function () {
    this.clearErrors();

    if (this.options.appState.get('isMfaChallenge')) {
      if (this.isValid()) {
        this.model.save();
      }
    } else {
      // Send email and switch to verification view
      this.model.set('answer', '');
      this.model.save().then(this.renderChallengView.bind(this));
    }
  },
  initialize: function () {
    Form.prototype.initialize.apply(this, arguments); // render 'Send Email' page at first place

    this.add(View.extend({
      attributes: {
        'data-se': 'mfa-send-email-content'
      },
      className: 'mfa-send-email-content',
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
              "code": "email.mfa.description"
            },
            "data": data,
            "loc": {
              "start": {
                "line": 1,
                "column": 0
              },
              "end": {
                "line": 1,
                "column": 78
              }
            }
          })) != null ? stack1 : "";
        },
        "useData": true
      }),
      getTemplateData: createEmailMaskElement
    }));
  },
  renderChallengView: function () {
    this.removeChildren();
    this.add(View.extend({
      className: 'mfa-email-sent-content',
      attributes: {
        'data-se': 'mfa-email-sent-content'
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
      getTemplateData: createEmailMaskElement
    }));
    this.add(ResendEmailView);
    this.addInput({
      label: loc('email.code.label', 'login'),
      'label-top': true,
      name: 'answer',
      input: TextBox,
      wide: true,
      type: 'tel'
    });

    if (this.options.appState.get('allowRememberDevice')) {
      this.addInput({
        label: false,
        'label-top': true,
        placeholder: this.options.appState.get('rememberDeviceLabel'),
        className: 'margin-btm-0',
        name: 'rememberDevice',
        type: 'checkbox'
      });
    }

    this.render();
  }
});

export { SendEmailAndVerifyCodeFormVerifyEmailCodeForm as default };
//# sourceMappingURL=SendEmailAndVerifyCodeForm.js.map
