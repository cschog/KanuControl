import Logger from '../../util/Logger.js';
import { FORMS, AUTHENTICATOR_KEY } from '../ion/RemediationConstants.js';
import './internals/BaseHeader.js';
import './internals/BaseFooter.js';
import './internals/BaseForm.js';
import './internals/BaseFormWithPolling.js';
import './internals/BaseOktaVerifyChallengeView.js';
import '../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import BaseView from './internals/BaseView.js';
import './components/AuthenticatorEnrollOptions.js';
import './components/AuthenticatorVerifyOptions.js';
import '../../util/FactorUtil.js';
import '../../v1/views/admin-consent/ScopeList.js';
import '../../v1/views/consent/ScopeList.js';
import './views/captcha/CaptchaView.js';
import IdentifierView from './views/IdentifierView.js';
import RedirectIdPView from './views/RedirectIdPView.js';
import IdentifyRecoveryView from './views/IdentifyRecoveryView.js';
import TerminalView from './views/TerminalView.js';
import AutoRedirectView from './views/AutoRedirectView.js';
import PollView from './views/PollView.js';
import AdminConsentView from './views/consent/AdminConsentView.js';
import EnduserConsentView from './views/consent/EnduserConsentView.js';
import GranularConsentView from './views/consent/GranularConsentView.js';
import EnduserEmailConsentView from './views/consent/EnduserEmailConsentView.js';
import DeviceChallengePollView from './views/device/DeviceChallengePollView.js';
import SSOExtensionView from './views/device/SSOExtensionView.js';
import SignInDeviceView from './views/device/SignInDeviceView.js';
import DeviceEnrollmentTerminalView from './views/device/DeviceEnrollmentTerminalView.js';
import EnrollProfileView from './views/EnrollProfileView.js';
import EnrollProfileUpdateView from './views/EnrollProfileUpdateView.js';
import RequestActivationEmail from './views/activation/RequestActivationEmailView.js';
import SelectAuthenticatorEnrollView from './views/SelectAuthenticatorEnrollView.js';
import SelectAuthenticatorVerifyView from './views/SelectAuthenticatorVerifyView.js';
import SelectAuthenticatorUnlockAccountView from './views/authenticator/SelectAuthenticatorUnlockAccountView.js';
import EnrollAuthenticatorPasswordView from './views/password/EnrollAuthenticatorPasswordView.js';
import ChallengeAuthenticatorPasswordView from './views/password/ChallengeAuthenticatorPasswordView.js';
import ReEnrollAuthenticatorPasswordView from './views/password/ReEnrollAuthenticatorPasswordView.js';
import ReEnrollAuthenticatorWarningPasswordView from './views/password/ReEnrollAuthenticatorWarningPasswordView.js';
import ResetAuthenticatorPasswordView from './views/password/ResetAuthenticatorPasswordView.js';
import EnrollAuthenticatorPhoneView from './views/phone/EnrollAuthenticatorPhoneView.js';
import EnrollAuthenticatorDataPhoneView from './views/phone/EnrollAuthenticatorDataPhoneView.js';
import ChallengeAuthenticatorPhoneView from './views/phone/ChallengeAuthenticatorPhoneView.js';
import ChallengeAuthenticatorDataPhoneView from './views/phone/ChallengeAuthenticatorDataPhoneView.js';
import EnrollAuthenticatorSecurityQuestion from './views/security-question/EnrollAuthenticatorSecurityQuestionView.js';
import ChallengeAuthenticatorSecurityQuestion from './views/security-question/ChallengeAuthenticatorSecurityQuestion.js';
import EnrollWebauthnView from './views/webauthn/EnrollWebauthnView.js';
import ChallengeWebauthnView from './views/webauthn/ChallengeWebauthnView.js';
import EnrollAuthenticatorEmailView from './views/email/EnrollAuthenticatorEmailView.js';
import ChallengeAuthenticatorEmailView from './views/email/ChallengeAuthenticatorEmailView.js';
import ChallengeAuthenticatorDataEmailView from './views/email/ChallengeAuthenticatorDataEmailView.js';
import EnrollPollOktaVerifyView from './views/ov/EnrollPollOktaVerifyView.js';
import SelectEnrollmentChannelOktaVerifyView from './views/ov/SelectEnrollmentChannelOktaVerifyView.js';
import EnrollementChannelDataOktaVerifyView from './views/ov/EnrollementChannelDataOktaVerifyView.js';
import ChallengeOktaVerifyView from './views/ov/ChallengeOktaVerifyView.js';
import ChallengeOktaVerifyTotpView from './views/ov/ChallengeOktaVerifyTotpView.js';
import ChallengeOktaVerifyResendPushView from './views/ov/ChallengeOktaVerifyResendPushView.js';
import ChallengeOktaVerifyCustomAppDataView from './views/shared/ChallengeOktaVerifyCustomAppDataView.js';
import ChallengeOktaVerifySSOExtensionView from './views/ov/ChallengeOktaVerifySSOExtensionView.js';
import EnrollAuthenticatorGoogleAuthenticatorView from './views/google-authenticator/EnrollAuthenticatorGoogleAuthenticatorView.js';
import ChallengeGoogleAuthenticatorView from './views/google-authenticator/ChallengeGoogleAuthenticatorView.js';
import EnrollAuthenticatorOnPremView from './views/on-prem/EnrollAuthenticatorOnPremView.js';
import ChallengeAuthenticatorOnPremView from './views/on-prem/ChallengeAuthenticatorOnPremView.js';
import EnrollDuoAuthenticatorView from './views/duo/EnrollDuoAuthenticatorView.js';
import ChallengeDuoAuthenticatorView from './views/duo/ChallengeDuoAuthenticatorView.js';
import AuthenticatorIdPVerifyView from './views/idp/AuthenticatorIdPVerifyView.js';
import AuthenticatorIdPEnrollView from './views/idp/AuthenticatorIdPEnrollView.js';
import ChallengeCustomOTPAuthenticatorView from './views/custom-otp/ChallengeCustomOTPAuthenticatorView.js';
import AuthenticatorSymantecView from './views/symantec/AuthenticatorSymantecView.js';
import DeviceCodeActivateView from './views/device/DeviceCodeActivateView.js';
import ChallengePIVView from './views/piv/ChallengePIVView.js';
import AuthenticatorYubiKeyView from './views/yubikey/AuthenticatorYubiKeyView.js';
import AuthenticatorView from './views/shared/ChallengePushView.js';
import ChallengeCustomAppResendPushView from './views/custom-app/ChallengeCustomAppResendPushView.js';

const DEFAULT = '_';
const VIEWS_MAPPING = {
  [FORMS.IDENTIFY]: {
    [DEFAULT]: IdentifierView
  },
  [FORMS.IDENTIFY_RECOVERY]: {
    [DEFAULT]: IdentifyRecoveryView
  },
  [FORMS.DEVICE_CHALLENGE_POLL]: {
    [DEFAULT]: DeviceChallengePollView
  },
  [FORMS.LAUNCH_AUTHENTICATOR]: {
    [DEFAULT]: SignInDeviceView
  },
  [FORMS.DEVICE_APPLE_SSO_EXTENSION]: {
    [DEFAULT]: SSOExtensionView,
    [AUTHENTICATOR_KEY.OV]: ChallengeOktaVerifySSOExtensionView
  },
  [FORMS.CANCEL_TRANSACTION]: {
    [DEFAULT]: SSOExtensionView
  },
  [FORMS.ENROLL_PROFILE]: {
    [DEFAULT]: EnrollProfileView
  },
  [FORMS.ENROLL_PROFILE_UPDATE]: {
    [DEFAULT]: EnrollProfileUpdateView
  },
  [FORMS.POLL]: {
    [DEFAULT]: PollView
  },
  [FORMS.REQUEST_ACTIVATION]: {
    [DEFAULT]: RequestActivationEmail
  },
  [FORMS.SELECT_AUTHENTICATOR_ENROLL]: {
    [DEFAULT]: SelectAuthenticatorEnrollView
  },
  [FORMS.AUTHENTICATOR_ENROLLMENT_DATA]: {
    [AUTHENTICATOR_KEY.PHONE]: EnrollAuthenticatorDataPhoneView
  },
  [FORMS.CONSENT_ADMIN]: {
    [DEFAULT]: AdminConsentView
  },
  [FORMS.CONSENT_ENDUSER]: {
    [DEFAULT]: EnduserConsentView
  },
  [FORMS.CONSENT_GRANULAR]: {
    [DEFAULT]: GranularConsentView
  },
  [FORMS.CONSENT_EMAIL_CHALLENGE]: {
    [DEFAULT]: EnduserEmailConsentView
  },
  [FORMS.ENROLL_AUTHENTICATOR]: {
    [AUTHENTICATOR_KEY.DUO]: EnrollDuoAuthenticatorView,
    [AUTHENTICATOR_KEY.EMAIL]: EnrollAuthenticatorEmailView,
    [AUTHENTICATOR_KEY.GOOGLE_OTP]: EnrollAuthenticatorGoogleAuthenticatorView,
    [AUTHENTICATOR_KEY.IDP]: AuthenticatorIdPEnrollView,
    [AUTHENTICATOR_KEY.ON_PREM]: EnrollAuthenticatorOnPremView,
    [AUTHENTICATOR_KEY.PASSWORD]: EnrollAuthenticatorPasswordView,
    [AUTHENTICATOR_KEY.PHONE]: EnrollAuthenticatorPhoneView,
    [AUTHENTICATOR_KEY.RSA]: EnrollAuthenticatorOnPremView,
    [AUTHENTICATOR_KEY.SECURITY_QUESTION]: EnrollAuthenticatorSecurityQuestion,
    [AUTHENTICATOR_KEY.SYMANTEC_VIP]: AuthenticatorSymantecView,
    [AUTHENTICATOR_KEY.WEBAUTHN]: EnrollWebauthnView,
    [AUTHENTICATOR_KEY.YUBIKEY]: AuthenticatorYubiKeyView
  },
  [FORMS.CHALLENGE_AUTHENTICATOR]: {
    [AUTHENTICATOR_KEY.CUSTOM_OTP]: ChallengeCustomOTPAuthenticatorView,
    [AUTHENTICATOR_KEY.DUO]: ChallengeDuoAuthenticatorView,
    [AUTHENTICATOR_KEY.EMAIL]: ChallengeAuthenticatorEmailView,
    [AUTHENTICATOR_KEY.GOOGLE_OTP]: ChallengeGoogleAuthenticatorView,
    [AUTHENTICATOR_KEY.IDP]: AuthenticatorIdPVerifyView,
    [AUTHENTICATOR_KEY.ON_PREM]: ChallengeAuthenticatorOnPremView,
    [AUTHENTICATOR_KEY.OV]: ChallengeOktaVerifyTotpView,
    [AUTHENTICATOR_KEY.PASSWORD]: ChallengeAuthenticatorPasswordView,
    [AUTHENTICATOR_KEY.PHONE]: ChallengeAuthenticatorPhoneView,
    [AUTHENTICATOR_KEY.RSA]: ChallengeAuthenticatorOnPremView,
    [AUTHENTICATOR_KEY.SECURITY_QUESTION]: ChallengeAuthenticatorSecurityQuestion,
    [AUTHENTICATOR_KEY.SYMANTEC_VIP]: AuthenticatorSymantecView,
    [AUTHENTICATOR_KEY.WEBAUTHN]: ChallengeWebauthnView,
    [AUTHENTICATOR_KEY.YUBIKEY]: AuthenticatorYubiKeyView
  },
  [FORMS.ENROLL_POLL]: {
    [AUTHENTICATOR_KEY.OV]: EnrollPollOktaVerifyView
  },
  [FORMS.SELECT_ENROLLMENT_CHANNEL]: {
    [AUTHENTICATOR_KEY.OV]: SelectEnrollmentChannelOktaVerifyView
  },
  [FORMS.ENROLLMENT_CHANNEL_DATA]: {
    [AUTHENTICATOR_KEY.OV]: EnrollementChannelDataOktaVerifyView
  },
  // Expired scenarios for authenticators..
  [FORMS.REENROLL_AUTHENTICATOR]: {
    // Password expired scenario..
    [AUTHENTICATOR_KEY.PASSWORD]: ReEnrollAuthenticatorPasswordView
  },
  // Will expire soon warnings for authenticators..
  [FORMS.REENROLL_AUTHENTICATOR_WARNING]: {
    // Password will expire soon scenario..
    [AUTHENTICATOR_KEY.PASSWORD]: ReEnrollAuthenticatorWarningPasswordView
  },
  // Reset forms for authenticators..
  [FORMS.RESET_AUTHENTICATOR]: {
    // Admin driven password reset..
    [AUTHENTICATOR_KEY.PASSWORD]: ResetAuthenticatorPasswordView
  },
  [FORMS.SELECT_AUTHENTICATOR_AUTHENTICATE]: {
    [DEFAULT]: SelectAuthenticatorVerifyView
  },
  [FORMS.SELECT_AUTHENTICATOR_UNLOCK]: {
    [DEFAULT]: SelectAuthenticatorUnlockAccountView
  },
  [FORMS.CHALLENGE_POLL]: {
    [AUTHENTICATOR_KEY.OV]: ChallengeOktaVerifyView,
    [AUTHENTICATOR_KEY.CUSTOM_APP]: AuthenticatorView
  },
  [FORMS.RESEND]: {
    [AUTHENTICATOR_KEY.OV]: ChallengeOktaVerifyResendPushView,
    [AUTHENTICATOR_KEY.CUSTOM_APP]: ChallengeCustomAppResendPushView
  },
  [FORMS.AUTHENTICATOR_VERIFICATION_DATA]: {
    [AUTHENTICATOR_KEY.PHONE]: ChallengeAuthenticatorDataPhoneView,
    [AUTHENTICATOR_KEY.OV]: ChallengeOktaVerifyCustomAppDataView,
    [AUTHENTICATOR_KEY.EMAIL]: ChallengeAuthenticatorDataEmailView,
    [AUTHENTICATOR_KEY.CUSTOM_APP]: ChallengeOktaVerifyCustomAppDataView
  },
  [FORMS.FAILURE_REDIRECT]: {
    [DEFAULT]: AutoRedirectView
  },
  [FORMS.SUCCESS_REDIRECT]: {
    [DEFAULT]: AutoRedirectView
  },
  [FORMS.REDIRECT_IDP]: {
    [DEFAULT]: RedirectIdPView
  },
  [FORMS.PIV_IDP]: {
    [DEFAULT]: ChallengePIVView
  },
  [FORMS.DEVICE_ENROLLMENT_TERMINAL]: {
    [DEFAULT]: DeviceEnrollmentTerminalView
  },
  [FORMS.USER_CODE]: {
    [DEFAULT]: DeviceCodeActivateView
  },
  [FORMS.TERMINAL]: {
    [DEFAULT]: TerminalView
  }
};
var ViewFactory = {
  create: function (formName, authenticatorKey = DEFAULT) {
    const config = VIEWS_MAPPING[formName];

    if (!config) {
      Logger.warn(`Cannot find customized View for ${formName}.`);
      return BaseView;
    }

    const View = config[authenticatorKey] || config[DEFAULT];

    if (!View) {
      Logger.warn(`Cannot find customized View for ${formName} + ${authenticatorKey}.`);
      return BaseView;
    }

    return View;
  }
};

export { ViewFactory as default };
//# sourceMappingURL=ViewFactory.js.map
