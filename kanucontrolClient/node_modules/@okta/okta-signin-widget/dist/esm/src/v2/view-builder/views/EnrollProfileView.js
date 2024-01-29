import { loc } from '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
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
import Model from '../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import '../internals/BaseHeader.js';
import BaseFooter from '../internals/BaseFooter.js';
import BaseForm from '../internals/BaseForm.js';
import '../internals/BaseFormWithPolling.js';
import '../internals/BaseOktaVerifyChallengeView.js';
import BaseView from '../internals/BaseView.js';
import { createIdpButtons } from '../internals/FormInputFactory.js';
import { FORMS } from '../../ion/RemediationConstants.js';
import { getPasswordComplexityDescriptionForHtmlList } from '../utils/AuthenticatorUtil.js';
import { generatePasswordPolicyHtml } from './password/PasswordPolicyUtil.js';
import signInWithIdps from './signin/SignInWithIdps.js';

const Body = BaseForm.extend({
  title: function () {
    if (this.options.appState.getCurrentViewState().href.endsWith('idp/idx/enroll/update')) {
      var _attributes$uiDisplay;

      const attributes = this.options.appState.attributes;

      if (attributes !== null && attributes !== void 0 && (_attributes$uiDisplay = attributes.uiDisplay) !== null && _attributes$uiDisplay !== void 0 && _attributes$uiDisplay.label) {
        return loc('oie.registration.form.customize.label', 'login', [attributes.uiDisplay.label]);
      } else {
        return loc('oie.primaryauth.submit', 'login');
      }
    }

    return loc('oie.registration.form.title', 'login');
  },
  save: function () {
    if (this.options.appState.getCurrentViewState().href.endsWith('idp/idx/enroll/update')) {
      var _attributes$uiDisplay2;

      const attributes = this.options.appState.attributes;

      if (attributes !== null && attributes !== void 0 && (_attributes$uiDisplay2 = attributes.uiDisplay) !== null && _attributes$uiDisplay2 !== void 0 && _attributes$uiDisplay2.buttonLabel) {
        return loc('oie.registration.form.customize.buttonLabel', 'login', [attributes.uiDisplay.buttonLabel]);
      } else {
        return loc('oie.registration.form.update.submit', 'login');
      }
    }

    return loc('oie.registration.form.submit', 'login');
  },
  saveForm: function () {
    // SIW customization hook for registration
    this.settings.preRegistrationSubmit(this.model.toJSON(), postData => {
      this.model.attributes = { ...this.model.attributes,
        ...this.model.parse(postData)
      };
      BaseForm.prototype.saveForm.call(this, this.model);
    }, error => this.model.trigger('error', this.model, {
      responseJSON: error
    }));
  },
  triggerAfterError: function (model, error) {
    var _error$responseJSON;

    // render errors to view
    const hasErrors = (error === null || error === void 0 ? void 0 : (_error$responseJSON = error.responseJSON) === null || _error$responseJSON === void 0 ? void 0 : _error$responseJSON.errorCauses) && Array.isArray(error.responseJSON.errorCauses);

    if (hasErrors) {
      error.responseJSON.errorCauses.forEach(err => {
        var _err$errorKey;

        // only do this for invalid password for password with SSR
        if ((_err$errorKey = err.errorKey) !== null && _err$errorKey !== void 0 && _err$errorKey.includes('password.passwordRequirementsNotMet')) {
          err.errorSummary = loc('registration.error.password.passwordRequirementsNotMet', 'login');
        }
      });
    }

    this.options.appState.trigger('afterError', error);
  }
});
const Footer = BaseFooter.extend({
  links: function () {
    const links = [];

    if (this.options.appState.hasRemediationObject(FORMS.SELECT_IDENTIFY)) {
      links.push({
        'type': 'link',
        'label': loc('haveaccount', 'login'),
        'name': 'back',
        'actionPath': FORMS.SELECT_IDENTIFY
      });
    }

    return links;
  }
});
var EnrollProfileView = BaseView.extend({
  Body: Body,
  Footer: Footer,
  createModelClass: function (currentViewState, optionUiSchemaConfig, settings) {
    const currentSchema = JSON.parse(JSON.stringify(currentViewState.uiSchema));
    let ModelClass = BaseView.prototype.createModelClass.apply(this, arguments, currentViewState);
    ModelClass = ModelClass.extend({
      toJSON: function () {
        const modelJSON = Model.prototype.toJSON.call(this, arguments, currentViewState); // delete optional attributes if they are empty and not required

        if (modelJSON.userProfile) {
          const uiSchema = currentViewState.uiSchema;
          const userProfile = modelJSON.userProfile;

          oktaUnderscore.each(userProfile, (value, name) => {
            if (oktaUnderscore.isEmpty(value)) {
              const uiSchemaProperty = uiSchema.find(schema => schema.name === `userProfile.${name}`);

              if (!oktaUnderscore.isUndefined(uiSchemaProperty) && !uiSchemaProperty.required) {
                delete userProfile[name];
              }
            }
          });
        }

        return modelJSON;
      }
    });
    settings.parseRegistrationSchema(currentSchema, schema => {
      if (!oktaUnderscore.isEqual(schema, currentViewState.uiSchema)) {
        currentViewState.uiSchema = schema;
        ModelClass = BaseView.prototype.createModelClass.call(this, currentViewState, optionUiSchemaConfig);
      }
    }, error => {
      ModelClass = ModelClass.extend({
        local: {
          parseSchemaError: {
            value: error,
            type: 'object'
          },
          ...ModelClass.prototype.local
        }
      });
    });
    return ModelClass;
  },
  postRender: function () {
    BaseView.prototype.postRender.apply(this, arguments);
    const modelError = this.model.get('parseSchemaError');

    if (modelError) {
      this.model.trigger('error', this.model, {
        responseJSON: modelError
      });
    } // Prompt for password w/ SSR if enabled (credentials object in remediation)


    this.renderPasswordPolicySettings();
    const idpButtons = createIdpButtons(this.options);

    if (Array.isArray(idpButtons) && idpButtons.length) {
      this._addIdpView(idpButtons);
    }
  },
  renderPasswordPolicySettings: function () {
    var _credentials$form;

    // retrieve password policy from "credentials" object in remediation
    const currentViewState = this.options.currentViewState.value;
    const credentials = currentViewState.filter(obj => {
      return obj.name === 'credentials';
    })[0]; // if "passcode" is present in "credentials", render password rules

    const form = credentials === null || credentials === void 0 ? void 0 : (_credentials$form = credentials.form) === null || _credentials$form === void 0 ? void 0 : _credentials$form.value;

    if (form && form.filter(obj => {
      return obj.name === 'passcode';
    })) {
      var _credentials$relatesT, _credentials$relatesT2;

      generatePasswordPolicyHtml(this, getPasswordComplexityDescriptionForHtmlList(credentials === null || credentials === void 0 ? void 0 : (_credentials$relatesT = credentials.relatesTo) === null || _credentials$relatesT === void 0 ? void 0 : (_credentials$relatesT2 = _credentials$relatesT.value) === null || _credentials$relatesT2 === void 0 ? void 0 : _credentials$relatesT2.settings), false);
    }
  },
  _addIdpView: function (idpButtons) {
    // We check the 'idpDisplay' option config to determine whether to render the idp buttons 
    // above or below the login fields
    const idpDisplay = this.options.settings.get('idpDisplay');
    const isPrimaryIdpDisplay = idpDisplay && idpDisplay.toUpperCase() === 'PRIMARY';
    this.add(signInWithIdps, {
      prepend: isPrimaryIdpDisplay,
      selector: isPrimaryIdpDisplay ? '.o-form-fieldset-container' : '.o-form-button-bar',
      options: {
        idpButtons: idpButtons,
        isPrimaryIdpDisplay: isPrimaryIdpDisplay
      }
    });
  }
});

export { EnrollProfileView as default };
//# sourceMappingURL=EnrollProfileView.js.map
