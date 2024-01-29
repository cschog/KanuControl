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
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import { FORMS, ACTIONS } from '../../ion/RemediationConstants.js';

const {
  ENROLLED_PASSWORD_RECOVERY_LINK: ENROLLED_PASSWORD_RECOVERY_LINK,
  ORG_PASSWORD_RECOVERY_LINK: ORG_PASSWORD_RECOVERY_LINK
} = ACTIONS;

const getSwitchAuthenticatorLink = appState => {
  if (appState.getRemediationAuthenticationOptions(FORMS.SELECT_AUTHENTICATOR_AUTHENTICATE).length > 1) {
    return [{
      'type': 'link',
      'label': loc('oie.verification.switch.authenticator', 'login'),
      'name': 'switchAuthenticator',
      'formName': FORMS.SELECT_AUTHENTICATOR_AUTHENTICATE
    }];
  }

  if (appState.getRemediationAuthenticationOptions(FORMS.SELECT_AUTHENTICATOR_ENROLL).length >= 1) {
    return [{
      'type': 'link',
      'label': loc('oie.enroll.switch.authenticator', 'login'),
      'name': 'switchAuthenticator',
      'formName': FORMS.SELECT_AUTHENTICATOR_ENROLL
    }];
  }

  return [];
};

const getForgotPasswordLink = (appState, settings) => {
  const forgotPasswordLink = {
    'type': 'link',
    'label': loc('forgotpassword', 'login'),
    'name': 'forgot-password'
  };
  const customForgotPasswordHref = settings.get('helpLinks.forgotPassword');

  if (customForgotPasswordHref) {
    return [Object.assign({}, {
      'href': customForgotPasswordHref,
      'label': loc('forgotpassword', 'login'),
      'name': 'forgot-password'
    })];
  } // at identify page when only Org Authenticator Password may be available
  else if (appState.getActionByPath(ORG_PASSWORD_RECOVERY_LINK)) {
    return [Object.assign({}, forgotPasswordLink, {
      actionPath: ORG_PASSWORD_RECOVERY_LINK
    })];
  } // at password verify page
  else if (appState.getActionByPath(ENROLLED_PASSWORD_RECOVERY_LINK)) {
    return [Object.assign({}, forgotPasswordLink, {
      actionPath: ENROLLED_PASSWORD_RECOVERY_LINK
    })];
  }

  return [];
};

const getSkipSetupLink = (appState, linkName) => {
  if (appState.hasRemediationObject(FORMS.SKIP)) {
    return [{
      'type': 'link',
      'label': linkName !== null && linkName !== void 0 ? linkName : loc('oie.enroll.skip.setup', 'login'),
      'name': 'skip-setup',
      'actionPath': FORMS.SKIP
    }];
  }

  return [];
}; // When there is a 'cancel' object in remediation


const getSignOutLink = (settings, options = {}) => {
  if (settings !== null && settings !== void 0 && settings.get('backToSignInUri')) {
    return [{
      'label': loc('goback', 'login'),
      'name': 'cancel',
      'href': settings.get('backToSignInUri')
    }];
  }

  return [{
    'actionPath': 'cancel',
    'label': !options.label ? loc('goback', 'login') : options.label,
    'name': 'cancel',
    'type': 'link'
  }];
}; // Use it to create a widget configured link in the absence of `cancel` object in remediation


const getBackToSignInLink = ({
  settings: settings,
  appState: appState
}) => {
  const link = {}; // If backToSignInLink is set, use this value for all scenarios

  if (settings !== null && settings !== void 0 && settings.get('backToSignInUri')) {
    link.href = settings.get('backToSignInUri');
  } // embedded scenarios
  else if (settings !== null && settings !== void 0 && settings.get('useInteractionCodeFlow')) {
    link.clickHandler = () => {
      appState.trigger('restartLoginFlow');
    };
  } // fallback for okta-hosted scenarios (backend should set signOutLink or backToSignOutLink)
  else {
    link.href = settings === null || settings === void 0 ? void 0 : settings.get('baseUrl');
  }

  return [{
    'type': 'link',
    'label': loc('goback', 'login'),
    'name': 'go-back',
    ...link
  }];
};

const getReloadPageButtonLink = () => {
  return [{
    'type': 'link',
    'label': loc('oie.try.again', 'login'),
    'name': 'try-again',
    'href': window.location,
    'className': 'button button-primary text-align-c'
  }];
};

const getSignUpLink = (appState, settings) => {
  const signupLink = [];

  if (appState.hasRemediationObject(FORMS.SELECT_ENROLL_PROFILE)) {
    const signupLinkData = {
      'type': 'link',
      'label': loc('oie.registration.form.title', 'login'),
      'name': 'enroll'
    };

    if (oktaUnderscore.isFunction(settings.get('registration.click'))) {
      signupLinkData.clickHandler = settings.get('registration.click');
    } else {
      signupLinkData.actionPath = FORMS.SELECT_ENROLL_PROFILE;
    }

    signupLink.push(signupLinkData);
  }

  return signupLink;
};

const getFactorPageCustomLink = (appState, settings) => {
  const factorPageCustomLink = [];
  const formsNeedFactorPageCustomLink = [FORMS.CHALLENGE_AUTHENTICATOR, FORMS.SELECT_AUTHENTICATOR_AUTHENTICATE, FORMS.CHALLENGE_POLL, FORMS.AUTHENTICATOR_VERIFICATION_DATA];

  if (!appState.get('isPasswordRecovery') && formsNeedFactorPageCustomLink.includes(appState.get('currentFormName'))) {
    const helpLinksFactorPageLabel = settings.get('helpLinks.factorPage.text');
    const helpLinksFactorPageHref = settings.get('helpLinks.factorPage.href');

    if (helpLinksFactorPageLabel && helpLinksFactorPageHref) {
      factorPageCustomLink.push({
        type: 'link',
        label: helpLinksFactorPageLabel,
        name: 'factorPageHelpLink',
        href: helpLinksFactorPageHref,
        target: '_blank'
      });
    }
  }

  return factorPageCustomLink;
};

export { getBackToSignInLink, getFactorPageCustomLink, getForgotPasswordLink, getReloadPageButtonLink, getSignOutLink, getSignUpLink, getSkipSetupLink, getSwitchAuthenticatorLink };
//# sourceMappingURL=LinksUtil.js.map
