import { View } from '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import oktaJQueryStatic from '../../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import oktaUnderscore from '../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import Link from '../components/Link.js';
import ToggleTextLink from '../components/ToggleTextLink.js';
import { getSignOutLink } from '../utils/LinksUtil.js';

/**
 * When `href` is present, the Link behaviors as normal link (anchor element).
 * When `actionPath` is present, the Link behaviors as link button
 *   on which user clicks, will trigger the action `actionPath`.
 * When `formName` is present, the link behaviors as link button
 *   on which user clicks, will submit a remediation form.
 *
 * @typedef {Object} Link
 * @property {string} label
 * @property {string} name
 * @property {string=} href
 * @property {string=} actionPath
 * @property {string=} formName
 */

var BaseFooter = View.extend({
  className: 'auth-footer',

  /**
   * {Link[]} links
   */
  links: [],

  /**
   * View
   * adds any view to the footer in footer info section
   */
  footerInfo: null,

  /**
   * Boolean
   * If false then 'Back to sign in' does not get added to the view
   */
  hasBackToSignInLink: true,
  initialize: function () {
    let links = oktaUnderscore.resultCtx(this, 'links', this);

    const footerInfo = oktaUnderscore.resultCtx(this, 'footerInfo', this);

    const hasBackToSignInLink = oktaUnderscore.resultCtx(this, 'hasBackToSignInLink', this); // safe check
    // 1. avoid none array from override
    // 2. ignore any none plain object arguments


    if (!Array.isArray(links)) {
      links = [];
    } else {
      links = links.filter(l => oktaJQueryStatic.isPlainObject(l));
    } // add 'back to sign in' link if the form qualifies for it by default.
    // Previously called cancel/Sign Out links


    if (this.options.appState.shouldShowSignOutLinkInCurrentForm(this.options.settings.get('features.hideSignOutLinkInMFA') || this.settings.get('features.mfaOnlyFlow')) && hasBackToSignInLink) {
      links = links.concat(getSignOutLink(this.options.settings));
    }

    links.forEach(link => {
      let LinkView = Link;

      if (link.type === 'toggle-text-link') {
        LinkView = ToggleTextLink;
      }

      this.add(LinkView, {
        options: link
      });
    });

    if (footerInfo) {
      this.add(View.extend({
        className: 'footer-info'
      }));
      this.add(footerInfo, '.footer-info');
    }
  }
});

export { BaseFooter as default };
//# sourceMappingURL=BaseFooter.js.map
