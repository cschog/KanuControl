import _Handlebars2 from '../../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { loc } from '../../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import '../../internals/BaseFooter.js';
import BaseForm from '../../internals/BaseForm.js';
import '../../internals/BaseFormWithPolling.js';
import '../../internals/BaseOktaVerifyChallengeView.js';
import '../../internals/BaseView.js';
import '../../components/AuthenticatorEnrollOptions.js';
import '../../components/AuthenticatorVerifyOptions.js';
import '../../../../util/FactorUtil.js';
import '../../../../v1/views/admin-consent/ScopeList.js';
import '../../../../v1/views/consent/ScopeList.js';
import '../captcha/CaptchaView.js';
import ConsentViewForm from './ConsentViewForm.js';
import BaseAuthenticatorView from '../../components/BaseAuthenticatorView.js';
import EmailAuthenticatorHeader from '../../components/EmailAuthenticatorHeader.js';

const getInfo = _Handlebars2.template({
  "1": function (container, depth0, helpers, partials, data) {
    var helper,
        lookupProperty = container.lookupProperty || function (parent, propertyName) {
      if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
        return parent[propertyName];
      }

      return undefined;
    };

    return "<div class=\"enduser-email-consent--info no-translate\"><i class=\"enduser-email-consent--icon icon--desktop\"></i><div>" + container.escapeExpression((helper = (helper = lookupProperty(helpers, "browser") || (depth0 != null ? lookupProperty(depth0, "browser") : depth0)) != null ? helper : container.hooks.helperMissing, typeof helper === "function" ? helper.call(depth0 != null ? depth0 : container.nullContext || {}, {
      "name": "browser",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 131
        },
        "end": {
          "line": 1,
          "column": 142
        }
      }
    }) : helper)) + "</div></div>";
  },
  "3": function (container, depth0, helpers, partials, data) {
    var helper,
        lookupProperty = container.lookupProperty || function (parent, propertyName) {
      if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
        return parent[propertyName];
      }

      return undefined;
    };

    return "<div class=\"enduser-email-consent--info no-translate\"><i class=\"enduser-email-consent--icon icon--app\"></i><div>" + container.escapeExpression((helper = (helper = lookupProperty(helpers, "app") || (depth0 != null ? lookupProperty(depth0, "app") : depth0)) != null ? helper : container.hooks.helperMissing, typeof helper === "function" ? helper.call(depth0 != null ? depth0 : container.nullContext || {}, {
      "name": "app",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 284
        },
        "end": {
          "line": 1,
          "column": 291
        }
      }
    }) : helper)) + "</div></div>";
  },
  "compiler": [8, ">= 4.3.0"],
  "main": function (container, depth0, helpers, partials, data) {
    var stack1,
        alias1 = depth0 != null ? depth0 : container.nullContext || {},
        lookupProperty = container.lookupProperty || function (parent, propertyName) {
      if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
        return parent[propertyName];
      }

      return undefined;
    };

    return ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "browser") : depth0, {
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
          "column": 161
        }
      }
    })) != null ? stack1 : "") + ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "app") : depth0, {
      "name": "if",
      "hash": {},
      "fn": container.program(3, data, 0),
      "inverse": container.noop,
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 161
        },
        "end": {
          "line": 1,
          "column": 310
        }
      }
    })) != null ? stack1 : "");
  },
  "useData": true
});

const enduserEmailConsentViewBody = ConsentViewForm.extend({
  className: 'enduser-email-consent',
  title: function () {
    return loc('oie.consent.enduser.title', 'login');
  },
  save: function () {
    return loc('oie.consent.enduser.accept.label', 'login');
  },
  cancel: function () {
    return loc('oie.consent.enduser.deny.label', 'login');
  },
  initialize: function () {
    BaseForm.prototype.initialize.apply(this, arguments);
    const info = getInfo(this.model.pick('browser', 'app'));
    this.add(info);
  },
  getUISchema: function () {
    const uiSchemas = BaseForm.prototype.getUISchema.apply(this, arguments);
    return uiSchemas.filter(uiSchema => uiSchema.name !== 'consent');
  }
});
var EnduserEmailConsentView = BaseAuthenticatorView.extend({
  Header: EmailAuthenticatorHeader,
  buttonOrder: ['cancel', 'save'],
  postRender: function () {
    const buttonContainer = this.$el.find('.o-form-button-bar');
    buttonContainer.find('.button-primary').removeClass('button-primary');
  },
  Body: enduserEmailConsentViewBody,
  createModelClass: function ({
    requestInfo: requestInfo
  }) {
    const ModelClass = BaseAuthenticatorView.prototype.createModelClass.apply(this, arguments);
    const browser = requestInfo.find(({
      name: name
    }) => name === 'browser');
    const app = requestInfo.find(({
      name: name
    }) => name === 'appName');
    const local = Object.assign({
      browser: {
        type: 'string',
        value: browser === null || browser === void 0 ? void 0 : browser.value
      },
      app: {
        type: 'string',
        value: app === null || app === void 0 ? void 0 : app.value
      }
    }, ModelClass.prototype.local);
    return ModelClass.extend({
      local: local,
      toJSON: function () {
        return {
          consent: this.get('consent')
        };
      }
    });
  }
});

export { EnduserEmailConsentView as default };
//# sourceMappingURL=EnduserEmailConsentView.js.map
