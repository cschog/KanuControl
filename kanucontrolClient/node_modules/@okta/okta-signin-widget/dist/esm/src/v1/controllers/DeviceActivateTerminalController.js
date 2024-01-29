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
import FormController from '../util/FormController.js';
import FormType from '../util/FormType.js';

const DEVICE_ACTIVATED = 'DEVICE_ACTIVATED';
const DEVICE_NOT_ACTIVATED_CONSENT_DENIED = 'DEVICE_NOT_ACTIVATED_CONSENT_DENIED';
const DEVICE_NOT_ACTIVATED = 'DEVICE_NOT_ACTIVATED';
const DEVICE_CODE_ERROR_KEYS = [DEVICE_NOT_ACTIVATED_CONSENT_DENIED, DEVICE_NOT_ACTIVATED];
var DeviceActivateTerminalController = FormController.extend({
  className: 'device-code-terminal',
  postRender: function () {
    FormController.prototype.postRender.apply(this, arguments); // show device code terminal icons

    const iconClass = this.options.appState.get('deviceActivationStatus') === DEVICE_ACTIVATED ? 'success-24-green' : 'error-24-red';
    this.$('.o-form-head').before('<div class="device-code-terminal--icon-container">' + '<span class="device-code-terminal--icon ' + iconClass + '"></span>' + '</div>');
  },
  Model: {},
  Form: {
    noCancelButton: true,
    noButtonBar: true,
    title: function () {
      const deviceActivationStatus = this.options.appState.get('deviceActivationStatus');

      if (deviceActivationStatus === DEVICE_ACTIVATED) {
        return loc('device.code.activated.success.title', 'login');
      }

      if (oktaUnderscore.contains(DEVICE_CODE_ERROR_KEYS, deviceActivationStatus)) {
        return loc('device.code.activated.error.title', 'login');
      }
    },
    subtitle: function () {
      const deviceActivationStatus = this.options.appState.get('deviceActivationStatus');

      if (deviceActivationStatus === DEVICE_ACTIVATED) {
        return loc('idx.device.activated', 'login');
      }

      if (deviceActivationStatus === DEVICE_NOT_ACTIVATED_CONSENT_DENIED) {
        return loc('idx.device.not.activated.consent.denied', 'login');
      }

      if (deviceActivationStatus === DEVICE_NOT_ACTIVATED) {
        return loc('idx.device.not.activated.internal.error', 'login');
      }
    },
    formChildren: function () {
      return [FormType.View({
        View: View.extend({
          template: _Handlebars2.template({
            "1": function (container, depth0, helpers, partials, data) {
              var lookupProperty = container.lookupProperty || function (parent, propertyName) {
                if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
                  return parent[propertyName];
                }

                return undefined;
              };

              return "<a href=\"/activate\" class=\"button button-primary text-align-c retry-button\" data-se=\"try-again\">" + container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
                "name": "i18n",
                "hash": {
                  "bundle": "login",
                  "code": "oie.try.again"
                },
                "data": data,
                "loc": {
                  "start": {
                    "line": 1,
                    "column": 121
                  },
                  "end": {
                    "line": 1,
                    "column": 165
                  }
                }
              })) + "</a>";
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

              return (stack1 = lookupProperty(helpers, "if").call(depth0 != null ? depth0 : container.nullContext || {}, depth0 != null ? lookupProperty(depth0, "isDeviceCodeError") : depth0, {
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
                    "column": 176
                  }
                }
              })) != null ? stack1 : "";
            },
            "useData": true
          }),
          getTemplateData: function () {
            return {
              isDeviceCodeError: oktaUnderscore.contains(DEVICE_CODE_ERROR_KEYS, this.options.appState.get('deviceActivationStatus'))
            };
          }
        })
      })];
    }
  }
});

export { DeviceActivateTerminalController as default };
//# sourceMappingURL=DeviceActivateTerminalController.js.map
