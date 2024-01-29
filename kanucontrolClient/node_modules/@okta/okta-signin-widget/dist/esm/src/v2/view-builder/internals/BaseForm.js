import _Handlebars2 from '../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { Form, loc, View, createCallout, internal } from '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import { create } from './FormInputFactory.js';

const {
  FormUtil: FormUtil
} = internal.views.forms.helpers;
const INFO_MESSAGE_CLASS = 'INFO';
var BaseForm = Form.extend({
  layout: 'o-form-theme',
  className: 'ion-form',
  hasSavingState: true,
  autoSave: false,
  noCancelButton: true,
  title: function () {
    return loc('oform.title.authenticate', 'login');
  },
  save: function () {
    return loc('oform.next', 'login');
  },
  modelEvents: {
    'clearFormError': 'handleClearFormError',
    'error': 'triggerAfterError'
  },
  initialize: function () {
    const uiSchemas = this.getUISchema();
    const inputOptions = uiSchemas.map(create); //should be used before adding any other input components

    this.showMessages(); // Render CAPTCHA if one of the form fields requires us to.

    this.listenTo(this.options.appState, 'onCaptchaLoaded', captchaObject => {
      this.captchaObject = captchaObject;
    });
    inputOptions.forEach(input => {
      this.addInputOrView(input);
    });
    this.listenTo(this, 'save', this.saveForm);
    this.listenTo(this, 'cancel', this.cancelForm);
  },
  focus: function (...args) {
    if (this.settings.get('features.autoFocus')) {
      Form.prototype.focus.apply(this, args);
    }
  },
  handleClearFormError: function () {
    const formErrorContainer = this.$('.o-form-error-container');
    formErrorContainer.empty();

    if (formErrorContainer.hasClass('o-form-has-errors')) {
      this.clearErrors();
    }
  },
  triggerAfterError: function (model, error) {
    this.options.appState.trigger('afterError', error);
  },
  saveForm: function (model) {
    //remove any existing warnings or messages before saving form
    this.$el.find('.o-form-error-container').empty(); // Execute Captcha if enabled for this form.

    if (this.captchaObject) {
      this.captchaObject.execute();
    } else {
      this.options.appState.trigger('saveForm', model);
    }
  },
  postRender: function () {
    /**
     * Widget would use infoContainer to display interactive messages that should be persisted during
     * invalid form submissions. For eg resend-warning callout should not be cleared upon invalid form submit.
     * Rerender would clear infoContainer or views classes can clear it explicitly.
     */
    const infoContainer = '<div class=\'o-form-info-container\'></div>';
    this.$el.find('.o-form-error-container').before(infoContainer);
    this.addIdentifier();
  },

  /**
   * Render user identifier below title, or if no title, render below message.
   */
  addIdentifier: function () {
    const {
      identifier: identifier
    } = this.options.appState.get('user') || {};

    if (!identifier) {
      return;
    } else if (!this.settings.get('features.showIdentifier')) {
      return;
    }

    const header = this.$el.find('[data-se="o-form-head"]');

    const identifierHTMLString = _Handlebars2.template({
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

        return "<div class=\"identifier-container\"><span class=\"identifier no-translate\" data-se=\"identifier\" title=" + alias4((helper = (helper = lookupProperty(helpers, "identifier") || (depth0 != null ? lookupProperty(depth0, "identifier") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
          "name": "identifier",
          "hash": {},
          "data": data,
          "loc": {
            "start": {
              "line": 1,
              "column": 99
            },
            "end": {
              "line": 1,
              "column": 113
            }
          }
        }) : helper)) + ">" + alias4((helper = (helper = lookupProperty(helpers, "identifier") || (depth0 != null ? lookupProperty(depth0, "identifier") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
          "name": "identifier",
          "hash": {},
          "data": data,
          "loc": {
            "start": {
              "line": 1,
              "column": 114
            },
            "end": {
              "line": 1,
              "column": 128
            }
          }
        }) : helper)) + "</span></div>";
      },
      "useData": true
    })({
      identifier: identifier
    });

    if (header.length) {
      header.after(identifierHTMLString);
    } else {
      this.$el.find('.o-form-error-container').after(identifierHTMLString);
    }
  },
  cancelForm: function () {
    this.options.appState.trigger('invokeAction', 'cancel');
  },
  getUISchema: function () {
    if (Array.isArray(this.options.currentViewState.uiSchema)) {
      return this.options.currentViewState.uiSchema;
    } else {
      return [];
    }
  },
  addInputOrView: function (input) {
    if (input.visible === false || input.mutable === false && input.visible !== true) {
      return;
    }

    if (input.View) {
      this.add(input.View, oktaUnderscore.omit(input, 'View', 'showWhen'));

      if (input.showWhen) {
        FormUtil.applyShowWhen(this.last(), input.showWhen);
      }
    } else {
      this.addInput(input);
    }

    if (Array.isArray(input.optionsUiSchemas)) {
      if (this.options.optionUiSchemaConfig[input.name]) {
        const optionUiSchemaIndex = Number(this.options.optionUiSchemaConfig[input.name]);
        const optionUiSchemas = input.optionsUiSchemas[optionUiSchemaIndex] || [];
        optionUiSchemas.forEach(this.addInputOrView.bind(this));
      }
    }
  },

  /*
  * Views should override this function to render custom error callouts for invalid form actions.
  * Should return true when callout is customized
  */
  showCustomFormErrorCallout: null,

  /*
   * Renders the contents of messages object (if any, on error) during initialize
   * This function is called during Form.initialize, and will display
   * messages when the form reloads.
   * Note: Any user action related form errors handled by FormController.showFormErrors
   */
  showMessages: function (options) {
    const messages = this.options.appState.get('messages') || {};
    const errContainer = '.o-form-error-container';

    if (Array.isArray(messages.value) && !(options instanceof View)) {
      this.add('<div class="ion-messages-container"></div>', errContainer);
      messages.value.forEach(obj => {
        if (!(obj !== null && obj !== void 0 && obj.class) || obj.class === INFO_MESSAGE_CLASS) {
          // add message as plain text
          this.add(`<p>${obj.message}</p>`, '.ion-messages-container');
        } else {
          var _obj$class;

          const errorObj = {
            class: (_obj$class = obj === null || obj === void 0 ? void 0 : obj.class) !== null && _obj$class !== void 0 ? _obj$class : '',
            message: obj === null || obj === void 0 ? void 0 : obj.message,
            title: '',
            ...options
          };
          this.add(createCallout({
            content: errorObj.message,
            type: errorObj.class.toLowerCase(),
            title: errorObj.title
          }), errContainer);
          options = null; // prevent repeating first error message
        }
      });
    } else if (options instanceof View) {
      // if callee is showCustomFormErrorCallout. show custom error views
      this.add(options, errContainer);
    }
  }
});

export { BaseForm as default };
//# sourceMappingURL=BaseForm.js.map
