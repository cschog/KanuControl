import _Handlebars2 from '../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { View, loc, createButton, internal } from '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import util from '../../../util/OAuth2Util.js';

const SharedUtil = internal.util.Util;

const dividerTpl = _Handlebars2.template({
  "compiler": [8, ">= 4.3.0"],
  "main": function (container, depth0, helpers, partials, data) {
    var helper,
        lookupProperty = container.lookupProperty || function (parent, propertyName) {
      if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
        return parent[propertyName];
      }

      return undefined;
    };

    return "<div class=\"auth-divider\"><span class=\"auth-divider-text\">" + container.escapeExpression((helper = (helper = lookupProperty(helpers, "text") || (depth0 != null ? lookupProperty(depth0, "text") : depth0)) != null ? helper : container.hooks.helperMissing, typeof helper === "function" ? helper.call(depth0 != null ? depth0 : container.nullContext || {}, {
      "name": "text",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 58
        },
        "end": {
          "line": 1,
          "column": 66
        }
      }
    }) : helper)) + "</span></div>";
  },
  "useData": true
});

const formTitleTpl = _Handlebars2.template({
  "compiler": [8, ">= 4.3.0"],
  "main": function (container, depth0, helpers, partials, data) {
    var helper,
        lookupProperty = container.lookupProperty || function (parent, propertyName) {
      if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
        return parent[propertyName];
      }

      return undefined;
    };

    return "<h2 data-se=\"o-form-head\" class=\"okta-form-title o-form-head\">" + container.escapeExpression((helper = (helper = lookupProperty(helpers, "title") || (depth0 != null ? lookupProperty(depth0, "title") : depth0)) != null ? helper : container.hooks.helperMissing, typeof helper === "function" ? helper.call(depth0 != null ? depth0 : container.nullContext || {}, {
      "name": "title",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 62
        },
        "end": {
          "line": 1,
          "column": 71
        }
      }
    }) : helper)) + "</h2>";
  },
  "useData": true
});

var CustomButtons = View.extend({
  className: 'primary-auth-container',
  children: function () {
    const children = [];
    const socialIdpButtons = this.settings.get('configuredSocialIdps');
    const pivButton = this.settings.get('piv');
    const customButtons = this.settings.get('customButtons');
    const divider = dividerTpl({
      text: loc('socialauth.divider.text', 'login')
    });

    if (this.settings.get('hasPivCard')) {
      children.push(this._createPivButton(pivButton));
    }

    oktaUnderscore.each(socialIdpButtons, function (button) {
      children.push(this._createSocialIdpButton(button));
    }, this);

    oktaUnderscore.each(customButtons, function (button) {
      children.push(this._createCustomButton(button));
    }, this); // If the social auth buttons have to be above the Okta form, the title moves from
    // primary auth form to the social auth (above the buttons) and the divider goes below
    // the buttons (in between social auth and primary auth). If social auth needs to go below
    // Okta form, just add the divider at the top of the social auth container. The title still
    // lives in primary auth form.


    if (this.settings.get('socialAuthPositionTop')) {
      children.unshift(formTitleTpl({
        title: loc('primaryauth.title', 'login')
      })); // Divider between Primary Auth and the Social Auth

      children.push(divider);
    } else {
      children.unshift(divider);
    }

    return children;
  },
  _createSocialIdpButton: function (options) {
    return createButton({
      attributes: {
        'data-se': options.dataAttr
      },
      className: options.className,
      title: function () {
        return options.text || loc(options.i18nKey);
      },
      click: function (e) {
        e.preventDefault();

        if (this.settings.get('oauth2Enabled')) {
          util.getTokens(this.settings, {
            idp: options.id
          }, this.options.currentController);
        } else {
          const baseUrl = this.settings.get('baseUrl');
          let params;
          const lastAuthResponse = this.options.appState.get('lastAuthResponse');

          if (this.options.appState.get('usingDeviceFlow')) {
            params = oktaJQueryStatic.param({
              stateToken: lastAuthResponse === null || lastAuthResponse === void 0 ? void 0 : lastAuthResponse.stateToken
            });
          } else {
            params = oktaJQueryStatic.param({
              fromURI: this.settings.get('relayState')
            });
          }

          const targetUri = `${baseUrl}/sso/idps/${options.id}?${params}`;
          SharedUtil.redirect(targetUri);
        }
      }
    });
  },
  _createPivButton: function (options) {
    let className = options.className || '';
    return createButton({
      attributes: {
        'data-se': 'piv-card-button'
      },
      className: className + ' piv-button',
      title: options.text || loc('piv.cac.card', 'login'),
      click: function (e) {
        e.preventDefault();
        this.options.appState.trigger('navigate', 'signin/verify/piv');
      }
    });
  },
  _createCustomButton: function (options) {
    return createButton({
      attributes: {
        'data-se': options.dataAttr
      },
      className: options.className + ' default-custom-button',
      title: function () {
        return options.title || loc(options.i18nKey);
      },
      click: options.click
    });
  }
});

export { CustomButtons as default };
//# sourceMappingURL=CustomButtons.js.map
