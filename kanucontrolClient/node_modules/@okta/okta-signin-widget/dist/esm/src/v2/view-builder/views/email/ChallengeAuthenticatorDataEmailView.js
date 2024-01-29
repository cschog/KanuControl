import _Handlebars2 from '../../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { View, loc } from '../../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import BaseAuthenticatorView from '../../components/BaseAuthenticatorView.js';

const BaseAuthenticatorEmailForm = BaseAuthenticatorView.prototype.Body;
const SubtitleView = View.extend({
  template: _Handlebars2.template({
    "1": function (container, depth0, helpers, partials, data) {
      var lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
        "name": "i18n",
        "hash": {
          "$1": "<span class='strong no-translate'>$1</span>",
          "arguments": "email",
          "bundle": "login",
          "code": "oie.email.verify.subtitle.text.with.email"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 70
          },
          "end": {
            "line": 1,
            "column": 210
          }
        }
      }));
    },
    "3": function (container, depth0, helpers, partials, data) {
      var lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "oie.email.verify.subtitle.text.without.email"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 218
          },
          "end": {
            "line": 1,
            "column": 294
          }
        }
      }));
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

      return "<div class=\"okta-form-subtitle\" data-se=\"o-form-explain\">" + ((stack1 = lookupProperty(helpers, "if").call(depth0 != null ? depth0 : container.nullContext || {}, depth0 != null ? lookupProperty(depth0, "email") : depth0, {
        "name": "if",
        "hash": {},
        "fn": container.program(1, data, 0),
        "inverse": container.program(3, data, 0),
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 57
          },
          "end": {
            "line": 1,
            "column": 301
          }
        }
      })) != null ? stack1 : "") + "</div>";
    },
    "useData": true
  }),
  getTemplateData: function () {
    var _this$options$appStat, _this$options$appStat2;

    const email = (_this$options$appStat = this.options.appState.get('currentAuthenticatorEnrollment')) === null || _this$options$appStat === void 0 ? void 0 : (_this$options$appStat2 = _this$options$appStat.profile) === null || _this$options$appStat2 === void 0 ? void 0 : _this$options$appStat2.email;
    return {
      email: email
    };
  }
});
const Body = BaseAuthenticatorEmailForm.extend({
  title: function () {
    return loc('oie.email.challenge.mfa.title', 'login');
  },
  save: function () {
    return loc('oie.email.verify.primaryButton', 'login');
  },
  postRender: function () {
    BaseAuthenticatorEmailForm.prototype.postRender.apply(this, arguments);
    this.add(SubtitleView, {
      prepend: true,
      selector: '.o-form-info-container'
    });
  },
  getUISchema: function () {
    // Prevent from displaying radio buttons on the UI
    const uiSchemas = BaseAuthenticatorEmailForm.prototype.getUISchema.apply(this, arguments);
    return uiSchemas.filter(schema => schema.name !== 'authenticator.methodType');
  }
});
var ChallengeAuthenticatorDataEmailView = BaseAuthenticatorView.extend({
  Body: Body
});

export { ChallengeAuthenticatorDataEmailView as default };
//# sourceMappingURL=ChallengeAuthenticatorDataEmailView.js.map
