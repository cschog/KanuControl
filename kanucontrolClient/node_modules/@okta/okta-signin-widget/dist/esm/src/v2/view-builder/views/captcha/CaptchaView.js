import _Handlebars2 from '../../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { View } from '../../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import Enums from '../../../../util/Enums.js';
import { WIDGET_FOOTER_CLASS } from '../../utils/Constants.js';

const OktaSignInWidgetOnCaptchaLoadedCallback = 'OktaSignInWidgetOnCaptchaLoaded';
const OktaSignInWidgetOnCaptchaSolvedCallback = 'OktaSignInWidgetOnCaptchaSolved';
const HCAPTCHA_URL = `https://hcaptcha.com/1/api.js?onload=${OktaSignInWidgetOnCaptchaLoadedCallback}&render=explicit`;
const RECAPTCHAV2_URL = `https://www.google.com/recaptcha/api.js?onload=${OktaSignInWidgetOnCaptchaLoadedCallback}&render=explicit`;
var CaptchaView = View.extend({
  className: 'captcha-view',
  template: _Handlebars2.template({
    "1": function (container, depth0, helpers, partials, data) {
      var helper,
          alias1 = depth0 != null ? depth0 : container.nullContext || {},
          alias2 = container.hooks.helperMissing,
          alias3 = "function",
          alias4 = container.escapeExpression,
          lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<div id =\"captcha-container\" class=\"" + alias4((helper = (helper = lookupProperty(helpers, "className") || (depth0 != null ? lookupProperty(depth0, "className") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "className",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 63
          },
          "end": {
            "line": 1,
            "column": 76
          }
        }
      }) : helper)) + "\" data-sitekey=\"" + alias4((helper = (helper = lookupProperty(helpers, "siteKey") || (depth0 != null ? lookupProperty(depth0, "siteKey") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "siteKey",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 92
          },
          "end": {
            "line": 1,
            "column": 103
          }
        }
      }) : helper)) + "\" data-callback=\"" + alias4((helper = (helper = lookupProperty(helpers, "onCaptchaSolvedCallback") || (depth0 != null ? lookupProperty(depth0, "onCaptchaSolvedCallback") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "onCaptchaSolvedCallback",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 120
          },
          "end": {
            "line": 1,
            "column": 147
          }
        }
      }) : helper)) + "\" data-size=\"invisible\"></div>";
    },
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
      var stack1,
          lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return (stack1 = lookupProperty(helpers, "if").call(depth0 != null ? depth0 : container.nullContext || {}, depth0 != null ? lookupProperty(depth0, "isCaptchaConfigured") : depth0, {
        "name": "if",
        "hash": {},
        "fn": container.program(1, data, 0),
        "inverse": container.noop,
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 0
          },
          "end": {
            "line": 1,
            "column": 184
          }
        }
      })) != null ? stack1 : "";
    },
    "useData": true
  }),
  getTemplateData: function () {
    if (this.captchaConfig) {
      const className = this.captchaConfig.type === 'HCAPTCHA' ? 'h-captcha' : 'g-recaptcha';
      return {
        siteKey: this.captchaConfig.siteKey,
        isCaptchaConfigured: true,
        onCaptchaSolvedCallback: OktaSignInWidgetOnCaptchaSolvedCallback,
        className: className
      };
    } else {
      return {};
    }
  },
  initialize: function () {
    if (this.options.appState.get('captcha')) {
      this.captchaConfig = this.options.appState.get('captcha');

      this._addCaptcha();
    }
  },
  remove: function () {
    View.prototype.remove.apply(this, arguments); // Cleanup global Captcha references

    if (this.captchaConfig.type === 'HCAPTCHA') {
      window.hcaptcha = undefined;
    } else if (this.captchaConfig.type === 'RECAPTCHA_V2') {
      window.grecaptcha = undefined;
    }
  },

  /**
   *  Load the CAPTCHA lib dynamically (either HCAPTCHA or RECAPTCHAV2). Once loaded, trigger an event to inform
   *  the parent form to actually render the CAPTCHA.
  * */
  _addCaptcha: function () {
    // Callback invoked when CAPTCHA is solved.
    const onCaptchaSolved = token => {
      const captchaObject = this._getCaptchaOject(); // We reset the Captcha. We need to reset because every time the 
      // Captcha resolves back with a token and say we have a server side error, 
      // if we submit the form again it won't work otherwise. The Captcha 
      // has to be reset for it to work again in that scenario.


      const captchaId = this.$el.find('#captcha-container').attr('data-captcha-id');
      captchaObject.reset(captchaId); // Set the token in the model

      const fieldName = this.options.name;
      this.model.set(fieldName, token);
      this.options.appState.trigger('saveForm', this.model);
    }; // Callback when CAPTCHA lib is loaded


    const onCaptchaLoaded = () => {
      // This is just a safeguard to ensure we don't bind Captcha to an already bound element.
      // It shouldn't happen in practice.
      if (this.$el.find('#captcha-container').attr('data-captcha-id')) {
        return;
      }

      const captchaObject = this._getCaptchaOject(); // We set a temporary token for Captcha because this is a required field for the form and is normally set
      // at a later time. In order to prevent client-side validation errors because of this, we have to set a 
      // dummy value. We then overwrite this with the proper token in the onCaptchaSolved callback.


      this.model.set(this.options.name, 'tempToken');
      const captchaId = captchaObject.render('captcha-container', {
        sitekey: this.captchaConfig.siteKey,
        callback: onCaptchaSolved
      });
      this.$el.find('#captcha-container').attr('data-captcha-id', captchaId); // Let the Baseform know that Captcha is loaded.

      this.options.appState.trigger('onCaptchaLoaded', captchaObject); // Render the HCAPTCHA footer - we need to do this manually since the HCAPTCHA lib doesn't do it

      if (this.captchaConfig.type === 'HCAPTCHA') {
        this._addHCaptchaFooter();
      }
    }; // Attaching the callback to the window object so that the CAPTCHA script that we dynamically render
    // can have access to it since it won't have access to this view's scope.


    window[OktaSignInWidgetOnCaptchaLoadedCallback] = onCaptchaLoaded; // Attaching the Captcha solved callback to the window object because we reference it in our template under
    // the 'data-callback' attribute which the Captcha library uses to invoke the callback.

    window[OktaSignInWidgetOnCaptchaSolvedCallback] = onCaptchaSolved;

    if (this.captchaConfig.type === 'HCAPTCHA') {
      this._loadCaptchaLib(this._getCaptchaUrl(HCAPTCHA_URL));
    } else if (this.captchaConfig.type === 'RECAPTCHA_V2') {
      this._loadCaptchaLib(this._getCaptchaUrl(RECAPTCHAV2_URL));
    }
  },

  /**
   *  We dynamically inject <script> tag into our login container because in case the customer is hosting
   *  the SIW, we need to ensure we don't go out of scope when injecting the script.
  * */
  _loadCaptchaLib: function (url) {
    let scriptTag = document.createElement('script');
    scriptTag.src = url;
    scriptTag.async = true;
    scriptTag.defer = true;
    document.getElementById(Enums.WIDGET_CONTAINER_ID).appendChild(scriptTag);
  },
  _addHCaptchaFooter: function () {
    // NOTE: insetAdjacentHTML() is supported in all major browsers: 
    // https://developer.mozilla.org/en-US/docs/Web/API/Element/insertAdjacentHTML#browser_compatibility
    const footerContainer = document.getElementsByClassName(WIDGET_FOOTER_CLASS);

    if (footerContainer.length) {
      const template = _Handlebars2.template({
        "compiler": [8, ">= 4.3.0"],
        "main": function (container, depth0, helpers, partials, data) {
          var lookupProperty = container.lookupProperty || function (parent, propertyName) {
            if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
              return parent[propertyName];
            }

            return undefined;
          };

          return "<div class=\"captcha-footer\"><span class=\"footer-text\">" + container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
            "name": "i18n",
            "hash": {
              "$2": "<a href='https://hcaptcha.com/terms' target='_blank'>$2</a>",
              "$1": "<a href='https://hcaptcha.com/privacy' target='_blank'>$1</a>",
              "bundle": "login",
              "code": "hcaptcha.footer.label"
            },
            "data": data,
            "loc": {
              "start": {
                "line": 1,
                "column": 54
              },
              "end": {
                "line": 1,
                "column": 238
              }
            }
          })) + "</span></div>";
        },
        "useData": true
      });

      footerContainer[0].insertAdjacentHTML('beforeend', template());
    }
  },
  _getCaptchaOject: function () {
    const captchaObject = this.captchaConfig.type === 'HCAPTCHA' ? window.hcaptcha : window.grecaptcha;
    return captchaObject;
  },
  _getCaptchaUrl: function (baseURL) {
    const locale = this.options.settings.get('language');
    return `${baseURL}&hl=${locale || navigator.language}`;
  }
});

export { CaptchaView as default };
//# sourceMappingURL=CaptchaView.js.map
