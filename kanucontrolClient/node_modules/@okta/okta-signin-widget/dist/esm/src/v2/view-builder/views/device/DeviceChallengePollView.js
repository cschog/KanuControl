import { loc, createCallout } from '../../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import '../../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import '../../internals/BaseHeader.js';
import BaseFooter from '../../internals/BaseFooter.js';
import '../../internals/BaseForm.js';
import '../../internals/BaseFormWithPolling.js';
import Body$1 from '../../internals/BaseOktaVerifyChallengeView.js';
import BaseView from '../../internals/BaseView.js';
import '../../components/AuthenticatorEnrollOptions.js';
import '../../components/AuthenticatorVerifyOptions.js';
import '../../../../util/FactorUtil.js';
import Enums from '../../../../util/Enums.js';
import '../../../../v1/views/admin-consent/ScopeList.js';
import '../../../../v1/views/consent/ScopeList.js';
import '../captcha/CaptchaView.js';
import { CANCEL_POLLING_ACTION, IDENTIFIER_FLOW, AUTHENTICATION_CANCEL_REASONS } from '../../utils/Constants.js';
import Link from '../../components/Link.js';
import { doChallenge, cancelPollingWithParams } from '../../utils/ChallengeViewUtil.js';
import OktaVerifyAuthenticatorHeader from '../../components/OktaVerifyAuthenticatorHeader.js';
import { getSignOutLink } from '../../utils/LinksUtil.js';

const Body = Body$1.extend({
  pollingCancelAction: CANCEL_POLLING_ACTION,
  getDeviceChallengePayload: function () {
    return this.options.currentViewState.relatesTo.value;
  },
  doChallenge: function () {
    doChallenge(this, IDENTIFIER_FLOW);
  },
  onPollingFail: function () {
    Body$1.prototype.onPollingFail.apply(this, arguments); // When SIW receives form error, polling is already stopped
    // SIW needs to update footer link from /poll/cancel to /idp/idx/cancel

    const data = {
      label: loc('loopback.polling.cancel.link.with.form.error', 'login')
    };
    this.options.appState.trigger('updateFooterLink', data);
  },
  showCustomFormErrorCallout: function (error) {
    const responseJSON = error.responseJSON;
    const options = {
      type: 'error',
      className: 'okta-verify-uv-callout-content',
      subtitle: responseJSON.errorSummary
    };
    const containsSignedNonceError = responseJSON.errorSummaryKeys && responseJSON.errorSummaryKeys.some(key => key.includes('auth.factor.signedNonce.error'));

    if (containsSignedNonceError) {
      options.title = loc('user.fail.verifyIdentity', 'login');
    }

    this.showMessages(createCallout(options));
    return true;
  }
});
const Footer = BaseFooter.extend({
  initialize: function () {
    this.listenTo(this.options.appState, 'updateFooterLink', this.handleUpdateFooterLink);

    if (this.isFallbackApproach() && !this.isFallbackDelayed()) {
      BaseFooter.prototype.initialize.apply(this, arguments);
    } else {
      this.backLink = this.add(Link, {
        options: {
          name: 'cancel-authenticator-challenge',
          label: loc('loopback.polling.cancel.link', 'login'),
          clickHandler: () => {
            cancelPollingWithParams(this.options.appState, CANCEL_POLLING_ACTION, AUTHENTICATION_CANCEL_REASONS.USER_CANCELED, null);
          }
        }
      }).last();
    }
  },
  handleUpdateFooterLink: function (data) {
    // only update link for loopback
    if (!this.isFallbackApproach() || this.isFallbackDelayed()) {
      this.backLink && this.backLink.remove();
      this.backLink = this.add(Link, {
        options: getSignOutLink(this.options.settings, data)[0]
      }).last();
    }
  },
  isFallbackApproach: function () {
    return [Enums.CUSTOM_URI_CHALLENGE, Enums.UNIVERSAL_LINK_CHALLENGE, Enums.APP_LINK_CHALLENGE].includes(this.options.currentViewState.relatesTo.value.challengeMethod);
  },
  isFallbackDelayed: function () {
    // only delay showing the reopen Okta Verify button for the app link approach for now
    // until we have more data shows other approaches have the slow cold start problem of the Okta Verify app as well
    return this.options.currentViewState.relatesTo.value.challengeMethod === Enums.APP_LINK_CHALLENGE;
  }
});
var DeviceChallengePollView = BaseView.extend({
  Header: OktaVerifyAuthenticatorHeader,
  Body: Body,
  Footer: Footer
});

export { DeviceChallengePollView as default };
//# sourceMappingURL=DeviceChallengePollView.js.map
