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
import oktaUnderscore from '../../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';

var EnrollChannelPollDescriptionView = View.extend({
  template: _Handlebars2.template({
    "1": function (container, depth0, helpers, partials, data) {
      var helper,
          alias1 = depth0 != null ? depth0 : container.nullContext || {},
          alias2 = container.hooks.helperMissing,
          alias3 = container.escapeExpression,
          lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<ol class=\"qrcode-info ov-info\"><li>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "oie.enroll.okta_verify.qrcode.step1"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 48
          },
          "end": {
            "line": 1,
            "column": 114
          }
        }
      })) + "</li><li>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "oie.enroll.okta_verify.qrcode.step2"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 123
          },
          "end": {
            "line": 1,
            "column": 189
          }
        }
      })) + "</li><li>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "oie.enroll.okta_verify.qrcode.step3"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 198
          },
          "end": {
            "line": 1,
            "column": 264
          }
        }
      })) + "</li></ol><div class=\"qrcode-container\"><img class=\"qrcode\" src=" + alias3((helper = (helper = lookupProperty(helpers, "href") || (depth0 != null ? lookupProperty(depth0, "href") : depth0)) != null ? helper : alias2, typeof helper === "function" ? helper.call(alias1, {
        "name": "href",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 328
          },
          "end": {
            "line": 1,
            "column": 336
          }
        }
      }) : helper)) + " alt=\"" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "mfa.altQrCode"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 342
          },
          "end": {
            "line": 1,
            "column": 387
          }
        }
      })) + "\"></img></div>";
    },
    "3": function (container, depth0, helpers, partials, data) {
      var stack1,
          lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<ul class=\"email-info ov-info\"><li>" + ((stack1 = (lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
        "name": "i18n",
        "hash": {
          "arguments": "email",
          "bundle": "login",
          "code": "oie.enroll.okta_verify.email.info"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 456
          },
          "end": {
            "line": 1,
            "column": 540
          }
        }
      })) != null ? stack1 : "") + "</li><li class=\"switch-channel-content\"></li></ul>";
    },
    "5": function (container, depth0, helpers, partials, data) {
      var stack1,
          lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<ul class=\"sms-info ov-info\"><li>" + ((stack1 = (lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
        "name": "i18n",
        "hash": {
          "arguments": "phoneNumber",
          "bundle": "login",
          "code": "oie.enroll.okta_verify.sms.info"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 649
          },
          "end": {
            "line": 1,
            "column": 737
          }
        }
      })) != null ? stack1 : "") + "</li><li class=\"switch-channel-content\"></li></ul>";
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

      return ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "href") : depth0, {
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
            "column": 408
          }
        }
      })) != null ? stack1 : "") + ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "email") : depth0, {
        "name": "if",
        "hash": {},
        "fn": container.program(3, data, 0),
        "inverse": container.noop,
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 408
          },
          "end": {
            "line": 1,
            "column": 597
          }
        }
      })) != null ? stack1 : "") + ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "phoneNumber") : depth0, {
        "name": "if",
        "hash": {},
        "fn": container.program(5, data, 0),
        "inverse": container.noop,
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 597
          },
          "end": {
            "line": 1,
            "column": 794
          }
        }
      })) != null ? stack1 : "");
    },
    "useData": true
  }),
  getTemplateData: function () {
    var _contextualData$qrcod;

    const contextualData = this.options.appState.get('currentAuthenticator').contextualData;
    return {
      href: (_contextualData$qrcod = contextualData.qrcode) === null || _contextualData$qrcod === void 0 ? void 0 : _contextualData$qrcod.href,
      email: oktaUnderscore.escape(contextualData === null || contextualData === void 0 ? void 0 : contextualData.email),
      phoneNumber: oktaUnderscore.escape(contextualData === null || contextualData === void 0 ? void 0 : contextualData.phoneNumber)
    };
  }
});

export { EnrollChannelPollDescriptionView as default };
//# sourceMappingURL=EnrollChannelPollDescriptionView.js.map
