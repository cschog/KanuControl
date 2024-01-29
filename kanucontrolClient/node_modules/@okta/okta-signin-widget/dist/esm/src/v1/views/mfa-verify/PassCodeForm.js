import _Handlebars2 from '../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { View, Form, loc, createButton } from '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import Q from 'q';
import Enums from '../../../util/Enums.js';
import TextBox from '../shared/TextBox.js';

const subtitleTpl = _Handlebars2.template({
  "compiler": [8, ">= 4.3.0"],
  "main": function (container, depth0, helpers, partials, data) {
    var helper,
        lookupProperty = container.lookupProperty || function (parent, propertyName) {
      if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
        return parent[propertyName];
      }

      return undefined;
    };

    return "(" + container.escapeExpression((helper = (helper = lookupProperty(helpers, "subtitle") || (depth0 != null ? lookupProperty(depth0, "subtitle") : depth0)) != null ? helper : container.hooks.helperMissing, typeof helper === "function" ? helper.call(depth0 != null ? depth0 : container.nullContext || {}, {
      "name": "subtitle",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 1
        },
        "end": {
          "line": 1,
          "column": 13
        }
      }
    }) : helper)) + ")";
  },
  "useData": true
});

const PassCodeFormwarningTemplate = View.extend({
  className: 'okta-form-infobox-warning infobox infobox-warning login-timeout-warning',
  attributes: {
    'aria-live': 'polite'
  },
  template: _Handlebars2.template({
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
      var stack1,
          helper,
          lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<span class=\"icon warning-16\"></span><p>" + ((stack1 = (helper = (helper = lookupProperty(helpers, "warning") || (depth0 != null ? lookupProperty(depth0, "warning") : depth0)) != null ? helper : container.hooks.helperMissing, typeof helper === "function" ? helper.call(depth0 != null ? depth0 : container.nullContext || {}, {
        "name": "warning",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 40
          },
          "end": {
            "line": 1,
            "column": 53
          }
        }
      }) : helper)) != null ? stack1 : "") + "</p>";
    },
    "useData": true
  })
});

function getFormAndButtonDetails(factorType) {
  switch (factorType) {
    case 'sms':
      return {
        buttonDataSe: 'sms-send-code',
        buttonClassName: 'sms-request-button',
        formSubmit: loc('mfa.sendCode', 'login'),
        formRetry: loc('mfa.resendCode', 'login'),
        formSubmitted: loc('mfa.sent', 'login'),
        subtitle: subtitleTpl({
          subtitle: this.model.get('phoneNumber')
        }),
        warning: loc('factor.sms.time.warning', 'login')
      };

    case 'call':
      return {
        buttonDataSe: 'make-call',
        buttonClassName: 'call-request-button',
        formSubmit: loc('mfa.call', 'login'),
        formRetry: loc('mfa.redial', 'login'),
        formSubmitted: loc('mfa.calling', 'login'),
        subtitle: subtitleTpl({
          subtitle: this.model.get('phoneNumber')
        }),
        warning: loc('factor.call.time.warning', 'login')
      };

    case 'email':
      return {
        buttonDataSe: 'email-send-code',
        buttonClassName: 'email-request-button',
        formSubmit: loc('mfa.sendEmail', 'login'),
        formRetry: loc('mfa.resendEmail', 'login'),
        formSubmitted: loc('mfa.sent', 'login'),
        subtitle: subtitleTpl({
          subtitle: this.model.get('email')
        })
      };

    default:
      return {
        buttonDataSe: '',
        buttonClassName: '',
        formSubmit: '',
        formRetry: '',
        formSubmitted: ''
      };
  }
}

var PassCodeForm = Form.extend({
  className: 'mfa-verify-passcode',
  autoSave: true,
  noCancelButton: true,
  save: oktaUnderscore.partial(loc, 'mfa.challenge.verify', 'login'),
  scrollOnError: false,
  layout: 'o-form-theme',
  disableSubmitButton: function () {
    return this.model.appState.get('isMfaChallenge') && this.model.get('answer');
  },
  showWarning: function (msg) {
    this.clearWarnings();
    this.add(PassCodeFormwarningTemplate, '.o-form-error-container', {
      options: {
        warning: msg
      }
    });
  },
  clearWarnings: function () {
    this.$('.okta-form-infobox-warning').remove();
  },
  initialize: function () {
    const form = this;
    this.title = this.model.get('factorLabel');
    const factorType = this.model.get('factorType');
    const formAndButtonDetails = getFormAndButtonDetails.call(this, factorType);
    const warningDetails = formAndButtonDetails.warning;
    this.$el.attr('data-se', 'factor-' + factorType);
    this.subtitle = formAndButtonDetails.subtitle;
    this.listenTo(this.model, 'error', function () {
      this.clearErrors();
    });
    this.addInput({
      label: loc('mfa.challenge.enterCode.placeholder', 'login'),
      'label-top': true,
      className: 'o-form-fieldset o-form-label-top auth-passcode',
      name: 'answer',
      input: TextBox,
      type: 'tel'
    });
    this.add(createButton({
      attributes: {
        'data-se': formAndButtonDetails.buttonDataSe
      },
      className: 'button ' + formAndButtonDetails.buttonClassName,
      title: formAndButtonDetails.formSubmit,
      click: function () {
        form.clearErrors();
        this.disable();
        form.clearWarnings();
        this.options.title = formAndButtonDetails.formSubmitted;
        this.render(); // To send an OTP to the device, make the same request but use
        // an empty passCode

        this.model.set('answer', '');
        this.model.save().then(function () {
          // render and focus on the passcode input field.
          form.getInputs().first().render().focus();
          return Q.delay(Enums.API_RATE_LIMIT);
        }).then(() => {
          this.options.title = formAndButtonDetails.formRetry;
          this.enable();

          if (factorType === 'call' || factorType === 'sms') {
            form.showWarning(warningDetails);
          }

          this.render();
        });
      }
    }));

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
  }
});

export { PassCodeForm as default };
//# sourceMappingURL=PassCodeForm.js.map
