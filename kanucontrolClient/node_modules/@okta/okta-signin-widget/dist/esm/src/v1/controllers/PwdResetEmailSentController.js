import _Handlebars2 from '../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { loc, View } from '../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import oktaUnderscore from '../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import Enums from '../../util/Enums.js';
import FormController from '../util/FormController.js';
import FormType from '../util/FormType.js';

var PwdResetEmailSentController = FormController.extend({
  className: 'password-reset-email-sent',
  Model: function () {
    return {
      local: {
        userFullName: ['string', false, this.options.appState.get('userFullName')]
      }
    };
  },
  Form: {
    title: oktaUnderscore.partial(loc, 'password.forgot.emailSent.title', 'login'),
    subtitle: function () {
      const username = this.options.appState.get('username');
      return loc('password.forgot.emailSent.desc', 'login', [username]);
    },
    noButtonBar: true,
    attributes: {
      'data-se': 'pwd-reset-email-sent'
    },
    formChildren: function () {
      let children = [FormType.View({
        View: View.extend({
          template: _Handlebars2.template({
            "compiler": [8, ">= 4.3.0"],
            "main": function (container, depth0, helpers, partials, data) {
              var helper,
                  lookupProperty = container.lookupProperty || function (parent, propertyName) {
                if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
                  return parent[propertyName];
                }

                return undefined;
              };

              return "<span class=\"accessibility-text\" role=\"status\">" + container.escapeExpression((helper = (helper = lookupProperty(helpers, "alert") || (depth0 != null ? lookupProperty(depth0, "alert") : depth0)) != null ? helper : container.hooks.helperMissing, typeof helper === "function" ? helper.call(depth0 != null ? depth0 : container.nullContext || {}, {
                "name": "alert",
                "hash": {},
                "data": data,
                "loc": {
                  "start": {
                    "line": 1,
                    "column": 47
                  },
                  "end": {
                    "line": 1,
                    "column": 56
                  }
                }
              }) : helper)) + "</span>";
            },
            "useData": true
          }),
          getTemplateData: function () {
            return {
              alert: loc('password.forgot.emailSent.title', 'login')
            };
          }
        })
      })];

      if (!this.settings.get('features.hideBackToSignInForReset')) {
        children.push(FormType.Button({
          title: loc('goback', 'login'),
          className: 'button button-primary button-wide',
          attributes: {
            'data-se': 'back-button'
          },
          click: function () {
            const self = this;
            return this.model.doTransaction(function (transaction) {
              return transaction.cancel();
            }).then(function () {
              self.state.set('navigateDir', Enums.DIRECTION_BACK);
              self.options.appState.trigger('navigate', '');
            });
          }
        }));
      }

      return children;
    }
  },
  initialize: function (options) {
    this.settings.callGlobalSuccess(Enums.FORGOT_PASSWORD_EMAIL_SENT, {
      username: options.appState.get('username')
    });
  }
});

export { PwdResetEmailSentController as default };
//# sourceMappingURL=PwdResetEmailSentController.js.map
