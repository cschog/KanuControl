import _Handlebars2 from '../../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { View, createCallout, loc } from '../../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
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
import fn from '../../../../util/BrowserFeatures.js';

var EnrollWebauthnInfoView = View.extend({
  // eslint-disable-next-line max-len
  template: _Handlebars2.template({
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
      var lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<p class=\"idx-webauthn-enroll-text\">" + container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "oie.enroll.webauthn.instructions"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 36
          },
          "end": {
            "line": 1,
            "column": 99
          }
        }
      })) + "</p>";
    },
    "useData": true
  }),
  initialize: function () {
    const relatesToObject = this.options.currentViewState.relatesTo;
    const activationData = relatesToObject === null || relatesToObject === void 0 ? void 0 : relatesToObject.value.contextualData.activationData;

    if (fn.isEdge()) {
      this.add(View.extend({
        tagName: 'p',
        className: 'idx-webauthn-enroll-text-edge',
        template: _Handlebars2.template({
          "compiler": [8, ">= 4.3.0"],
          "main": function (container, depth0, helpers, partials, data) {
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
                "code": "oie.enroll.webauthn.instructions.edge"
              },
              "data": data,
              "loc": {
                "start": {
                  "line": 1,
                  "column": 0
                },
                "end": {
                  "line": 1,
                  "column": 68
                }
              }
            }));
          },
          "useData": true
        })
      }));
    }

    if (activationData.authenticatorSelection.userVerification === 'required') {
      this.add(createCallout({
        className: 'uv-required-callout',
        size: 'slim',
        type: 'warning',
        subtitle: loc('oie.enroll.webauthn.uv.required.instructions', 'login')
      }));
    }

    this.add('<div data-se="webauthn-waiting" class="okta-waiting-spinner"></div>');
  }
});

export { EnrollWebauthnInfoView as default };
//# sourceMappingURL=EnrollWebauthnInfoView.js.map
