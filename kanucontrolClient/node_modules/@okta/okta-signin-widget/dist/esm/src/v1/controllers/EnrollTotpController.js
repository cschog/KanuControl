import _Handlebars2 from '../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { View, loc } from '../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import fn from '../../util/FactorUtil.js';
import FormController from '../util/FormController.js';
import FormType from '../util/FormType.js';
import StoreLinks from '../util/StoreLinks.js';
import Footer from '../views/enroll-factors/Footer.js';

const showWhenDeviceTypeSelected = {
  __deviceType__: function (val) {
    return val !== undefined;
  }
};
const EnrollTotpControllerAppDownloadInstructionsView = View.extend({
  attributes: {
    'data-se': 'app-download-instructions'
  },
  className: 'app-download-instructions',
  template: _Handlebars2.template({
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
      var stack1,
          helper,
          alias1 = depth0 != null ? depth0 : container.nullContext || {},
          alias2 = container.hooks.helperMissing,
          alias3 = "function",
          lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<span class=\"app-logo " + container.escapeExpression((helper = (helper = lookupProperty(helpers, "appIcon") || (depth0 != null ? lookupProperty(depth0, "appIcon") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "appIcon",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 22
          },
          "end": {
            "line": 1,
            "column": 33
          }
        }
      }) : helper)) + "\"></span><p class=\"instructions\">" + ((stack1 = (helper = (helper = lookupProperty(helpers, "appStoreLinkText") || (depth0 != null ? lookupProperty(depth0, "appStoreLinkText") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "appStoreLinkText",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 66
          },
          "end": {
            "line": 1,
            "column": 88
          }
        }
      }) : helper)) != null ? stack1 : "") + "</p>";
    },
    "useData": true
  }),
  initialize: function () {
    this.listenTo(this.model, 'change:__deviceType__', this.render);
  },
  postRender: function () {
    const link = this.$el.find('.instructions a');

    if (link.length) {
      link[0].setAttribute('target', '_blank');
      link[0].setAttribute('rel', 'noreferer noopener');
    }
  },
  getTemplateData: function () {
    let appStoreLink;
    let appIcon;
    let appStoreName;
    const factorName = fn.getFactorLabel(this.model.get('__provider__'), this.model.get('__factorType__'));
    appStoreName = StoreLinks.STORE[this.model.get('__deviceType__')];

    if (this.model.get('__provider__') === 'GOOGLE') {
      appStoreLink = StoreLinks.GOOGLE[this.model.get('__deviceType__')];
      appIcon = 'google-auth-38';
    } else {
      appStoreLink = StoreLinks.OKTA[this.model.get('__deviceType__')];
      appIcon = 'okta-verify-download-icon';
    }

    return {
      appStoreLinkText: loc('enroll.totp.downloadApp', 'login', [appStoreLink, factorName, appStoreName]),
      appIcon: appIcon
    };
  }
});
const EnrollTotpControllerEnrollTotpController = FormController.extend({
  className: 'enroll-totp',
  Model: function () {
    return {
      local: {
        __deviceType__: 'string',
        __factorType__: ['string', false, this.options.factorType],
        __provider__: ['string', false, this.options.provider]
      },
      save: function () {
        return this.doTransaction(function (transaction) {
          const factor = oktaUnderscore.findWhere(transaction.factors, {
            factorType: this.get('__factorType__'),
            provider: this.get('__provider__')
          });

          return factor.enroll();
        });
      }
    };
  },
  Form: {
    title: function () {
      const factorName = fn.getFactorLabel(this.model.get('__provider__'), this.model.get('__factorType__'));
      return loc('enroll.totp.title', 'login', [factorName]);
    },
    noButtonBar: true,
    attributes: {
      'data-se': 'step-device-type'
    },
    formChildren: function () {
      const inputOptions = {
        APPLE: loc('iphone', 'login'),
        ANDROID: loc('android', 'login')
      };
      const children = [FormType.Input({
        name: '__deviceType__',
        type: 'radio',
        options: inputOptions,
        group: true,
        label: oktaUnderscore.partial(loc, 'enroll.totp.selectDevice', 'login')
      }), FormType.Divider({
        showWhen: showWhenDeviceTypeSelected
      }), FormType.View({
        View: EnrollTotpControllerAppDownloadInstructionsView,
        showWhen: showWhenDeviceTypeSelected
      }), FormType.Button({
        title: loc('oform.next', 'login'),
        attributes: {
          'data-type': 'save'
        },
        className: 'button button-primary default-custom-button',
        click: function () {
          this.model.save();
        },
        showWhen: showWhenDeviceTypeSelected
      })];
      return children;
    }
  },
  Footer: Footer
});

export { EnrollTotpControllerEnrollTotpController as default };
//# sourceMappingURL=EnrollTotpController.js.map
