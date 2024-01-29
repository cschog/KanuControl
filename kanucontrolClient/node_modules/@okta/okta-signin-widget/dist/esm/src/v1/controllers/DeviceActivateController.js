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
import '../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import FormController from '../util/FormController.js';
import FormType from '../util/FormType.js';
import TextBox from '../views/shared/TextBox.js';

const InvalidUserCodeErrorView = View.extend({
  template: _Handlebars2.template({
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
      var lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<div class=\"okta-form-infobox-error infobox infobox-error\" role=\"alert\"><span class=\"icon error-16\"></span><p>" + container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "api.authn.error.PASSCODE_INVALID"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 110
          },
          "end": {
            "line": 1,
            "column": 173
          }
        }
      })) + "</p></div>";
    },
    "useData": true
  })
});
var DeviceActivateController = FormController.extend({
  className: 'device-code-activate',
  Model: function () {
    var _this$options, _this$options$appStat;

    return {
      props: {
        userCode: ['string', true, (_this$options = this.options) === null || _this$options === void 0 ? void 0 : (_this$options$appStat = _this$options.appState) === null || _this$options$appStat === void 0 ? void 0 : _this$options$appStat.get('userCode')],
        stateToken: 'string'
      },
      save: function () {
        const self = this;
        return this.doTransaction(function (transaction) {
          return transaction.deviceActivate({
            userCode: self.get('userCode')
          });
        });
      }
    };
  },
  Form: {
    noCancelButton: true,
    autoSave: true,
    save: function () {
      return loc('oform.next', 'login');
    },
    title: function () {
      return loc('device.code.activate.title', 'login');
    },
    subtitle: function () {
      return loc('device.code.activate.subtitle', 'login');
    },
    formChildren: function () {
      if (this.options.appState.get('deviceActivationStatus') === 'INVALID_USER_CODE') {
        this.add(InvalidUserCodeErrorView, '.o-form-error-container');
      }

      return [FormType.Input({
        label: loc('device.code.activate.label', 'login'),
        'label-top': true,
        name: 'userCode',
        input: TextBox,
        inputId: 'user-code',
        type: 'text',
        inlineValidation: false
      })];
    },
    events: {
      'keyup input[name="userCode"]': function (e) {
        e.preventDefault();
        this.addHyphen(e);
      }
    },
    addHyphen: function (evt) {
      const currentVal = evt.target.value; // add hyphen after 4th character

      if (currentVal && currentVal.length === 4 && !['Backspace', 'Delete', '-'].includes(evt.key)) {
        evt.target.value = currentVal.concat('-');
      }
    }
  }
});

export { DeviceActivateController as default };
//# sourceMappingURL=DeviceActivateController.js.map
