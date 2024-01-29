import _Handlebars2 from '../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { loc, View, internal } from '../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import oktaJQueryStatic from '../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import oktaUnderscore from '../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import FormController from '../util/FormController.js';
import FormType from '../util/FormType.js';
import FooterWithBackLink from '../views/shared/FooterWithBackLink.js';

let {
  Util: Util
} = internal.util;
var VerifyPIVController = FormController.extend({
  className: 'piv-cac-card',
  Model: {
    save: async function () {
      this.trigger('request');
      const self = this;
      const pivConfig = this.settings.get('piv');
      const data = {
        fromURI: this.settings.get('relayState'),
        isCustomDomain: pivConfig.isCustomDomain
      };

      try {
        await this.getCert(pivConfig.certAuthUrl);
        const res = await this.postCert(pivConfig.certAuthUrl, data);
        Util.redirect(res.redirectUrl);
      } catch (err) {
        if (oktaUnderscore.isEmpty(err.responseJSON) && !err.responseText) {
          err.responseJSON = {
            errorSummary: loc('piv.cac.error', 'login')
          };
        }

        self.trigger('error', self, err);
      }
    },
    getCert: function (certAuthUrl) {
      return oktaJQueryStatic.get({
        url: certAuthUrl,
        xhrFields: {
          withCredentials: true
        },
        beforeSend: function () {// overriding this function to prevent our jquery-wrapper from
          // setting headers. Need to keep this a simple request in order for
          // PIV / CAC to work in IE.
        }
      });
    },
    postCert: function (certAuthUrl, data) {
      return oktaJQueryStatic.post({
        url: certAuthUrl,
        xhrFields: {
          withCredentials: true
        },
        data: JSON.stringify(data),
        contentType: 'text/plain',
        beforeSend: function () {// overriding this function to prevent our jquery-wrapper from
          // setting headers. Need to keep this a simple request in order for
          // PIV / CAC to work in IE.
        }
      });
    }
  },
  Form: {
    autoSave: true,
    hasSavingState: false,
    title: oktaUnderscore.partial(loc, 'piv.cac.title', 'login'),
    className: 'piv-cac-card',
    noCancelButton: true,
    save: oktaUnderscore.partial(loc, 'retry', 'login'),
    modelEvents: {
      request: '_startEnrollment',
      error: '_stopEnrollment'
    },
    formChildren: [FormType.View({
      View: View.extend({
        template: _Handlebars2.template({
          "compiler": [8, ">= 4.3.0"],
          "main": function (container, depth0, helpers, partials, data) {
            var lookupProperty = container.lookupProperty || function (parent, propertyName) {
              if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
                return parent[propertyName];
              }

              return undefined;
            };

            return "<div class=\"piv-verify-text\"><p>" + container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
              "name": "i18n",
              "hash": {
                "bundle": "login",
                "code": "piv.cac.card.insert"
              },
              "data": data,
              "loc": {
                "start": {
                  "line": 1,
                  "column": 32
                },
                "end": {
                  "line": 1,
                  "column": 82
                }
              }
            })) + "</p><div data-se=\"piv-waiting\" class=\"okta-waiting-spinner\"></div></div>";
          },
          "useData": true
        })
      })
    })],
    _startEnrollment: function () {
      this.$('.okta-waiting-spinner').show();
      this.$('.o-form-button-bar').hide();
    },
    _stopEnrollment: function () {
      this.$('.okta-waiting-spinner').hide();
      this.$('.o-form-button-bar').show();
    },
    postRender: function () {
      oktaUnderscore.defer(() => {
        this.model.save();
      });
    }
  },
  back: function () {// Empty function on verify controllers to prevent users
    // from navigating back during 'verify' using the browser's
    // back button. The URL will still change, but the view will not
    // More details in OKTA-135060.
  },
  Footer: FooterWithBackLink
});

export { VerifyPIVController as default };
//# sourceMappingURL=VerifyPIVController.js.map
