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
import { FORMS } from '../../../ion/RemediationConstants.js';
import '../../../../v1/views/admin-consent/ScopeList.js';
import '../../../../v1/views/consent/ScopeList.js';
import '../captcha/CaptchaView.js';
import BaseAuthenticatorView from '../../components/BaseAuthenticatorView.js';
import fn from '../../../../util/CryptoUtil.js';
import webauthn from '../../../../util/webauthn.js';
import fn$1 from '../../../../util/BrowserFeatures.js';
import ChallengeWebauthnInfoView from './ChallengeWebauthnInfoView.js';
import { getMessageFromBrowserError } from '../../../ion/i18nTransformer.js';
import ChallengeWebauthnFooter from '../../components/ChallengeWebauthnFooter.js';
import EnrollWebAuthnResidentKeyLinkView from './EnrollWebAuthnResidentkeyLinkView.js';

const Body = BaseForm.extend({
  title: function () {
    return loc('oie.verify.webauth.title', 'login');
  },
  className: 'oie-verify-webauthn',
  getUISchema: function () {
    const schema = []; // Returning custom array so no input fields are displayed for webauthn

    if (webauthn.isNewApiAvailable()) {
      const retryButton = createButton({
        className: 'retry-webauthn button-primary default-custom-button',
        title: loc('mfa.challenge.verify', 'login'),
        click: () => {
          this.getCredentialsAndSave();
        }
      });
      schema.push({
        View: ChallengeWebauthnInfoView
      }, {
        View: retryButton
      });

      if (this._canSetupWebAuthnResidentKey()) {
        schema.push({
          View: EnrollWebAuthnResidentKeyLinkView
        });
      }
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
  remove: function () {
    BaseForm.prototype.remove.apply(this, arguments);

    if (this.webauthnAbortController) {
      this.webauthnAbortController.abort();
      this.webauthnAbortController = null;
    }
  },
  noButtonBar: true,
  modelEvents: {
    'error': '_stopVerification'
  },
  getCredentialsAndSave: function () {
    var _this$options$appStat;

    this.clearErrors();

    this._startVerification(); // AbortController is not supported in IE11


    if (typeof AbortController !== 'undefined') {
      this.webauthnAbortController = new AbortController();
    }

    const relatesToObject = this.options.currentViewState.relatesTo;
    const authenticatorData = (relatesToObject === null || relatesToObject === void 0 ? void 0 : relatesToObject.value) || {};
    const allowCredentials = [];
    const authenticatorEnrollments = ((_this$options$appStat = this.options.appState.get('authenticatorEnrollments')) === null || _this$options$appStat === void 0 ? void 0 : _this$options$appStat.value) || [];
    authenticatorEnrollments.forEach(enrollement => {
      if (enrollement.key === 'webauthn') {
        allowCredentials.push({
          type: 'public-key',
          id: fn.strToBin(enrollement.credentialId)
        });
      }
    });
    const challengeData = authenticatorData.contextualData.challengeData;

    const options = oktaUnderscore.extend({}, challengeData, {
      allowCredentials: allowCredentials,
      challenge: fn.strToBin(challengeData.challenge)
    }); // navigator.credentials() is not supported in IE11
    // eslint-disable-next-line compat/compat


    navigator.credentials.get({
      publicKey: options,
      signal: this.webauthnAbortController && this.webauthnAbortController.signal
    }).then(assertion => {
      const credentials = {
        clientData: fn.binToStr(assertion.response.clientDataJSON),
        authenticatorData: fn.binToStr(assertion.response.authenticatorData),
        signatureData: fn.binToStr(assertion.response.signature)
      };
      const hasUserHandleSchema = this.options.appState.getSchemaByName('credentials.userHandle');

      if (hasUserHandleSchema) {
        var _assertion$response$u;

        oktaUnderscore.extend(credentials, {
          userHandle: fn.binToStr((_assertion$response$u = assertion.response.userHandle) !== null && _assertion$response$u !== void 0 ? _assertion$response$u : '')
        });
      }

      this.model.set({
        credentials: credentials
      });
      this.saveForm(this.model);
    }, error => {
      // Do not display if it is abort error triggered by code when switching.
      // this.webauthnAbortController would be null if abort was triggered by code.
      if (this.webauthnAbortController) {
        this.model.trigger('error', this.model, {
          responseJSON: {
            errorSummary: getMessageFromBrowserError(error)
          }
        });
      }
    }).finally(() => {
      // unset webauthnAbortController on successful authentication or error
      this.webauthnAbortController = null;
    });
  },
  _startVerification: function () {
    this.$('.okta-waiting-spinner').show();
    this.$('.retry-webauthn').hide();
    this.$('.setup-webauthn-residentkey-text').hide();
    this.$('.retry-webauthn')[0].textContent = loc('retry', 'login');
  },
  _stopVerification: function () {
    this.$('.okta-waiting-spinner').hide();
    this.$('.retry-webauthn').show();
    this.$('.setup-webauthn-residentkey-text').show();
  },
  _canSetupWebAuthnResidentKey: function () {
    return this.options.appState.hasRemediationObject(FORMS.ENROLL_WEBAUTHN_RESIDENTKEY);
  }
});
var ChallengeWebauthnView = BaseAuthenticatorView.extend({
  Body: Body,
  Footer: ChallengeWebauthnFooter,
  postRender: function () {
    BaseAuthenticatorView.prototype.postRender.apply(this, arguments); // Trigger browser prompt automatically for other browsers for better UX.

    if (webauthn.isNewApiAvailable() && !fn$1.isSafari()) {
      this.form.getCredentialsAndSave();
    }
  }
});

export { ChallengeWebauthnView as default };
//# sourceMappingURL=ChallengeWebauthnView.js.map
