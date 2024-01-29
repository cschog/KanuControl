import { loc, createCallout } from '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import { FORMS } from '../../ion/RemediationConstants.js';
import '../internals/BaseHeader.js';
import '../internals/BaseFooter.js';
import BaseForm from '../internals/BaseForm.js';
import '../internals/BaseFormWithPolling.js';
import '../internals/BaseOktaVerifyChallengeView.js';
import BaseView from '../internals/BaseView.js';
import { createIdpButtons, createCustomButtons } from '../internals/FormInputFactory.js';
import DeviceFingerprinting from '../utils/DeviceFingerprinting.js';
import IdentifierFooter from '../components/IdentifierFooter.js';
import Link from '../components/Link.js';
import signInWithIdps from './signin/SignInWithIdps.js';
import customButtonsView from './signin/CustomButtons.js';
import SignInWithDeviceOption from './signin/SignInWithDeviceOption.js';
import signInWithWebAuthn from './signin/SignInWithWebAuthn.js';
import { isCustomizedI18nKey } from '../../ion/i18nTransformer.js';
import { getForgotPasswordLink } from '../utils/LinksUtil.js';
import fn from '../../../util/CookieUtil.js';
import CustomAccessDeniedErrorMessage from './shared/CustomAccessDeniedErrorMessage.js';

const CUSTOM_ACCESS_DENIED_KEY = 'security.access_denied_custom_message';
const Body = BaseForm.extend({
  title: function () {
    return loc('primaryauth.title', 'login');
  },
  save: function () {
    return loc('oform.next', 'login');
  },
  initialize: function () {
    BaseForm.prototype.initialize.apply(this, arguments);
    const uiSchema = this.getUISchema();

    if (uiSchema.find(schema => schema.name === 'credentials.passcode')) {
      this.save = loc('oie.primaryauth.submit', 'login');
    } // Precedence for pre-filling identifier field:
    // 1. Use username/identifier from the config.
    // 2. Use identifier value returned in remediation response (model will have this attr set if it's there) 
    // 3. Use value from the "remember my username" cookie.


    if (this._shouldAddUsername(uiSchema)) {
      // Set username/identifier from the config (i.e. config.username)
      this.model.set('identifier', this.settings.get('username'));
    } else if (!this.model.get('identifier') && this._shouldApplyRememberMyUsername(uiSchema)) {
      // Use value from cookie if the remediation did not return identifier value.
      this._applyRememberMyUsername();
    }
  },
  saveForm: function () {
    // Ideally this can be added to a "preSaveForm" handler - but keeping this here for now.
    if (!this.settings.get('features.deviceFingerprinting')) {
      BaseForm.prototype.saveForm.apply(this, arguments);
      return;
    } // Before the XHR is made for "identify", we'll generate this one-time use fingerprint via
    // a hidden-iframe (similar to authn/v1 flows)


    const fingerprintData = {
      oktaDomainUrl: this.settings.get('baseUrl'),
      element: this.$el
    }; // For certain flows, we need to generate a device fingerprint
    // to determine if we need to send a "New Device Sign-on Notification".
    // In the future, this should be handled completely by okta-auth-js OKTA-418160

    DeviceFingerprinting.generateDeviceFingerprint(fingerprintData).then(fingerprint => {
      this.options.appState.set('deviceFingerprint', fingerprint);
    }).catch(() => {
      /* Keep going even if device fingerprint fails */
    }).finally(() => {
      BaseForm.prototype.saveForm.apply(this, arguments);
    });
  },
  render: function () {
    BaseForm.prototype.render.apply(this, arguments); // Launch Device Authenticator

    if (this.options.appState.hasRemediationObject(FORMS.LAUNCH_AUTHENTICATOR)) {
      this.add(SignInWithDeviceOption, '.o-form-fieldset-container', false, true, {
        isRequired: false
      });
    }

    if (this.options.appState.hasRemediationObject(FORMS.LAUNCH_WEBAUTHN_AUTHENTICATOR)) {
      this.add(signInWithWebAuthn, '.o-form-fieldset-container', false, true, {
        isRequired: false
      });
    } // add forgot password link and external idps buttons if needed


    const idpButtons = createIdpButtons(this.options);

    if (Array.isArray(idpButtons) && idpButtons.length) {
      // Add the forgot password link before the buttons for multiple IDPs
      this._addForgotPasswordView();

      this._addIdpView(idpButtons);
    }

    const customButtons = createCustomButtons(this.options.settings);

    if (Array.isArray(customButtons) && customButtons.length) {
      this.add(customButtonsView, {
        selector: '.o-form-button-bar',
        options: {
          customButtons: customButtons,
          addSeparateLine: true
        }
      });
    }
  },
  postRender: function () {
    BaseForm.prototype.postRender.apply(this, arguments); // When a user enters invalid credentials, /introspect returns an error,
    // along with a user object containing the identifier entered by the user.

    this.$el.find('.identifier-container').remove();
  },

  /**
   * Update UI schemas for customization from .widgetrc.js or Admin Customization settings page.
   * @returns Array
   */
  getUISchema: function () {
    const schemas = BaseForm.prototype.getUISchema.apply(this, arguments);
    const {
      settings: settings
    } = this.options;
    const identifierExplainLabeli18nKey = 'primaryauth.username.tooltip';
    const passwordExplainLabeli18nKey = 'primaryauth.password.tooltip';
    let newSchemas = schemas.map(schema => {
      let newSchema = { ...schema
      };

      if (schema.name === 'identifier') {
        if (isCustomizedI18nKey(identifierExplainLabeli18nKey, settings)) {
          newSchema = { ...newSchema,
            explain: loc(identifierExplainLabeli18nKey, 'login'),
            'explain-top': true
          };
        } // We enable the browser's autocomplete for the identifier input
        // because we want to allow the user to choose from previously used identifiers.


        newSchema = { ...newSchema,
          autoComplete: 'identifier'
        };
      } else if (schema.name === 'credentials.passcode') {
        newSchema = { ...newSchema,
          autoComplete: 'current-password'
        };

        if (isCustomizedI18nKey(passwordExplainLabeli18nKey, settings)) {
          newSchema = { ...newSchema,
            explain: loc(passwordExplainLabeli18nKey, 'login'),
            'explain-top': true
          };
        }
      }

      return newSchema;
    }); // If showKeepMeSignedIn is explicitly set to false, we ensure we don't display
    // the "Keep me signed in" checkbox

    if (this.settings.get('features.showKeepMeSignedIn') === false) {
      newSchemas = newSchemas.filter(schema => schema.name !== 'rememberMe');
    }

    return newSchemas;
  },
  showCustomFormErrorCallout: function (error, messages) {
    var _error$responseJSON, _error$responseJSON$e;

    if (!(error !== null && error !== void 0 && (_error$responseJSON = error.responseJSON) !== null && _error$responseJSON !== void 0 && (_error$responseJSON$e = _error$responseJSON.errorSummaryKeys) !== null && _error$responseJSON$e !== void 0 && _error$responseJSON$e.includes(CUSTOM_ACCESS_DENIED_KEY))) {
      return false;
    }

    const message = messages.find(message => message.i18n.key === CUSTOM_ACCESS_DENIED_KEY);

    if (!message) {
      return false;
    }

    const {
      errorSummary: errorSummary
    } = error.responseJSON;
    const options = {
      type: 'error',
      content: new CustomAccessDeniedErrorMessage({
        message: errorSummary,
        links: message.links
      })
    };
    this.showMessages(createCallout(options));
    return true;
  },
  _addForgotPasswordView: function () {
    const forgotPasswordLink = getForgotPasswordLink(this.options.appState, this.options.settings);

    if (forgotPasswordLink.length) {
      this.add('<div class="links-primary"></div>', {
        selector: '.o-form-button-bar'
      });
      this.add(Link, {
        selector: '.links-primary',
        options: forgotPasswordLink[0]
      });
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
  },
  _shouldAddUsername: function (uiSchema) {
    // We pre-populate the identifier/username field only if we're in an identifier
    // form and if the option is passed in.
    return uiSchema.find(schema => schema.name === 'identifier') && this.settings.get('username');
  },
  _shouldApplyRememberMyUsername: function (uiSchema) {
    return uiSchema.find(schema => schema.name === 'identifier') && this.settings.get('features.rememberMe') && this.settings.get('features.rememberMyUsernameOnOIE');
  },

  /**
   * When "Remember My Username" is enabled, we pre-fill the identifier
   * field with the saved userName cookie. The cookie would have been originally
   * saved when submitting the form previously.
   */
  _applyRememberMyUsername: function () {
    const cookieUsername = fn.getCookieUsername();

    if (cookieUsername) {
      this.model.set('identifier', cookieUsername);
    }
  }
});
var IdentifierView = BaseView.extend({
  Body: Body,
  createModelClass: function () {
    const ModelClass = BaseView.prototype.createModelClass.apply(this, arguments); // customize pre-submit form validation inline error messages

    const identifierRequiredi18nKey = 'error.username.required';
    const passwordRequiredi18nKey = 'error.password.required';
    const props = ModelClass.prototype.props;

    const validate = attr => {
      const inlineErrors = {};
      const isEmptyIdentifier = props['identifier'] && !(attr !== null && attr !== void 0 && attr['identifier']);
      const isEmptyPassword = props['credentials.passcode'] && !(attr !== null && attr !== void 0 && attr['credentials.passcode']);

      if (isEmptyIdentifier && isCustomizedI18nKey(identifierRequiredi18nKey, this.settings)) {
        inlineErrors['identifier'] = loc(identifierRequiredi18nKey, 'login');
      }

      if (isEmptyPassword && isCustomizedI18nKey(passwordRequiredi18nKey, this.settings)) {
        inlineErrors['credentials.passcode'] = loc(passwordRequiredi18nKey, 'login');
      }

      return inlineErrors;
    };

    return ModelClass.extend({
      validate: validate
    });
  },
  initialize: function () {
    // Override Footer by overriding showForgotPasswordLink method
    this.Footer = IdentifierFooter.extend({
      showForgotPasswordLink: () => {
        // We don't add the forgot password link in the footer if SIW renders multi IDPs,
        // instead in that case we add it before the IDP buttons in IdentifierView as primary links.
        const idpButtons = createIdpButtons(this.options);
        return !this.options.appState.isIdentifierOnlyView() && (!Array.isArray(idpButtons) || idpButtons.length === 0);
      }
    });
  },
  postRender: function () {
    BaseView.prototype.postRender.apply(this, arguments); // If user entered identifier is not found, API sends back a message with a link to sign up
    // This is the click handler for that link

    const appState = this.options.appState;
    this.$el.find('.js-sign-up').click(function () {
      appState.trigger('invokeAction', FORMS.SELECT_ENROLL_PROFILE);
      return false;
    });
  }
});

export { IdentifierView as default };
//# sourceMappingURL=IdentifierView.js.map
