import _Handlebars2 from '../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { View, loc } from '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import '../internals/BaseHeader.js';
import BaseFooter from '../internals/BaseFooter.js';
import '../internals/BaseForm.js';
import '../internals/BaseFormWithPolling.js';
import '../internals/BaseOktaVerifyChallengeView.js';
import '../internals/BaseView.js';
import './AuthenticatorEnrollOptions.js';
import './AuthenticatorVerifyOptions.js';
import '../../../util/FactorUtil.js';
import { FORMS } from '../../ion/RemediationConstants.js';
import '../../../v1/views/admin-consent/ScopeList.js';
import '../../../v1/views/consent/ScopeList.js';
import '../views/captcha/CaptchaView.js';
import { getSignUpLink, getForgotPasswordLink } from '../utils/LinksUtil.js';
import Link from './Link.js';

var IdentifierFooter = BaseFooter.extend({
  showForgotPasswordLink: function () {
    return !this.options.appState.isIdentifierOnlyView();
  },
  footerInfo: function () {
    const signUpLinkData = getSignUpLink(this.options.appState, this.options.settings);
    let SignUpLinkWithText; //Build sign up link view appended with a text. Link class can only build anchor tags

    if (signUpLinkData.length) {
      SignUpLinkWithText = View.extend({
        className: 'signup-info',
        template: _Handlebars2.template({
          "compiler": [8, ">= 4.3.0"],
          "main": function (container, depth0, helpers, partials, data) {
            var lookupProperty = container.lookupProperty || function (parent, propertyName) {
              if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
                return parent[propertyName];
              }

              return undefined;
            };

            return "<span>" + container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
              "name": "i18n",
              "hash": {
                "bundle": "login",
                "code": "registration.signup.label"
              },
              "data": data,
              "loc": {
                "start": {
                  "line": 1,
                  "column": 6
                },
                "end": {
                  "line": 1,
                  "column": 62
                }
              }
            })) + "</span><span class=\"signup-link\"></span>";
          },
          "useData": true
        }),
        initialize: function () {
          this.add(Link, '.signup-link', {
            options: signUpLinkData[0]
          });
        }
      });
    }

    return SignUpLinkWithText;
  },
  links: function () {
    const {
      appState: appState,
      settings: settings
    } = this.options;
    let helpLinkHref;

    if (settings.get('helpLinks.help')) {
      helpLinkHref = settings.get('helpLinks.help');
    } else {
      const baseUrl = settings.get('baseUrl');
      helpLinkHref = baseUrl + '/help/login';
    }

    const helpLink = [{
      'name': 'help',
      'label': loc('help', 'login'),
      'href': helpLinkHref
    }];
    let forgotPasswordLink = [];

    if (this.showForgotPasswordLink()) {
      forgotPasswordLink = getForgotPasswordLink(appState, settings);
    }

    const customHelpLinks = [];

    if (settings.get('helpLinks.custom')) {
      //add custom helpLinks
      settings.get('helpLinks.custom').forEach(customHelpLink => {
        customHelpLink.name = 'custom';
        customHelpLink.label = customHelpLink.text;
        customHelpLinks.push(customHelpLink);
      });
    }

    const unlockAccountLink = [];

    if (settings.get('helpLinks.unlock')) {
      unlockAccountLink.push({
        'type': 'link',
        'label': loc('unlockaccount', 'login'),
        'name': 'unlock',
        'href': settings.get('helpLinks.unlock')
      });
    } else if (appState.hasRemediationObject(FORMS.UNLOCK_ACCOUNT)) {
      unlockAccountLink.push({
        'type': 'link',
        'label': loc('unlockaccount', 'login'),
        'name': 'unlock',
        'actionPath': FORMS.UNLOCK_ACCOUNT
      });
    }

    return forgotPasswordLink.concat(unlockAccountLink).concat(helpLink).concat(customHelpLinks);
  }
});

export { IdentifierFooter as default };
//# sourceMappingURL=IdentifierFooter.js.map
