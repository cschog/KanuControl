import { loc, createButton, createCallout } from '../../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import oktaUnderscore from '../../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import '../../internals/BaseHeader.js';
import '../../internals/BaseFooter.js';
import BaseForm from '../../internals/BaseForm.js';
import '../../internals/BaseFormWithPolling.js';
import '../../internals/BaseOktaVerifyChallengeView.js';
import '../../internals/BaseView.js';
import '../../components/AuthenticatorEnrollOptions.js';
import '../../components/AuthenticatorVerifyOptions.js';
import '../../../../util/FactorUtil.js';
import '../../../../v1/views/admin-consent/ScopeList.js';
import '../../../../v1/views/consent/ScopeList.js';
import '../captcha/CaptchaView.js';
import BaseAuthenticatorView from '../../components/BaseAuthenticatorView.js';
import webauthn from '../../../../util/webauthn.js';
import fn from '../../../../util/CryptoUtil.js';
import EnrollWebauthnInfoView from './EnrollWebauthnInfoView.js';
import { getMessageFromBrowserError } from '../../../ion/i18nTransformer.js';

function getExcludeCredentials(authenticatorEnrollments = []) {
  const credentials = [];
  authenticatorEnrollments.forEach(enrollement => {
    if (enrollement.key === 'webauthn') {
      credentials.push({
        type: 'public-key',
        id: fn.strToBin(enrollement.credentialId)
      });
    }
  });
  return credentials;
}

const Body = BaseForm.extend({
  title: function () {
    return loc('oie.enroll.webauthn.title', 'login');
  },
  className: 'oie-enroll-webauthn',
  modelEvents: {
    'error': '_stopEnrollment'
  },
  getUISchema: function () {
    const schema = []; // Returning custom array so no input fields are displayed for webauthn

    if (webauthn.isNewApiAvailable()) {
      schema.push({
        View: EnrollWebauthnInfoView
      });
      schema.push({
        View: createButton({
          className: 'webauthn-setup button button-primary button-wide',
          title: loc('oie.enroll.webauthn.save', 'login'),
          click: () => {
            this.triggerWebauthnPrompt();
          }
        })
      });
    } else {
      schema.push({
        View: createCallout({
          className: 'webauthn-not-supported',
          type: 'error',
          subtitle: loc('oie.webauthn.error.not.supported', 'login')
        })
      });
    }

    return schema;
  },
  triggerWebauthnPrompt: function () {
    this.$el.find('.o-form-error-container').empty();

    this._startEnrollment();

    const relatesToObject = this.options.currentViewState.relatesTo;
    const activationData = relatesToObject === null || relatesToObject === void 0 ? void 0 : relatesToObject.value.contextualData.activationData;

    if (webauthn.isNewApiAvailable()) {
      var _activationData$authe;

      const excludeCredentials = ((_activationData$authe = activationData.authenticatorSelection) === null || _activationData$authe === void 0 ? void 0 : _activationData$authe.requireResidentKey) === true ? [] : getExcludeCredentials(this.options.appState.get('authenticatorEnrollments').value);

      const options = oktaUnderscore.extend({}, activationData, {
        challenge: fn.strToBin(activationData.challenge),
        user: {
          id: fn.strToBin(activationData.user.id),
          name: activationData.user.name,
          displayName: activationData.user.displayName
        },
        excludeCredentials: excludeCredentials
      }); // AbortController is not supported in IE11


      if (typeof AbortController !== 'undefined') {
        this.webauthnAbortController = new AbortController();
      }

      navigator.credentials.create({
        publicKey: options,
        signal: this.webauthnAbortController && this.webauthnAbortController.signal
      }).then(newCredential => {
        this.model.set({
          credentials: {
            clientData: fn.binToStr(newCredential.response.clientDataJSON),
            attestation: fn.binToStr(newCredential.response.attestationObject)
          }
        });
        this.saveForm(this.model);
      }).catch(error => {
        this.model.trigger('error', this.model, {
          responseJSON: {
            errorSummary: getMessageFromBrowserError(error)
          }
        });
      }).finally(() => {
        this.webauthnAbortController = null;
      });
    }
  },
  _startEnrollment: function () {
    this.$('.okta-waiting-spinner').show();
    this.$('.webauthn-setup').hide();
  },
  _stopEnrollment: function () {
    this.$('.okta-waiting-spinner').hide();
    this.$('.webauthn-setup').show();
  }
});
var EnrollWebauthnView = BaseAuthenticatorView.extend({
  Body: Body,
  postRender: function () {
    BaseAuthenticatorView.prototype.postRender.apply(this, arguments);
    this.$el.find('.o-form-button-bar [type="submit"]').remove();
  }
});

export { EnrollWebauthnView as default };
//# sourceMappingURL=EnrollWebauthnView.js.map
