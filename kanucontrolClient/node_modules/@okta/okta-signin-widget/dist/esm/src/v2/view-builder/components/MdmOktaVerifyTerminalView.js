import _Handlebars2 from '../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { View, loc, internal } from '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';

const {
  Notification: Notification
} = internal.views.components;
const {
  Clipboard: Clipboard
} = internal.util;
var MdmOktaVerifyTerminalView = View.extend({
  template: _Handlebars2.template({
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
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

      return "<div>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "enroll.explanation.mdm"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 5
          },
          "end": {
            "line": 1,
            "column": 58
          }
        }
      })) + "</div><ol><li>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "enroll.mdm.step.copyLink"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 72
          },
          "end": {
            "line": 1,
            "column": 127
          }
        }
      })) + "<a data-clipboard-text=\"" + alias3((helper = (helper = lookupProperty(helpers, "enrollmentLink") || (depth0 != null ? lookupProperty(depth0, "enrollmentLink") : depth0)) != null ? helper : alias2, typeof helper === "function" ? helper.call(alias1, {
        "name": "enrollmentLink",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 151
          },
          "end": {
            "line": 1,
            "column": 169
          }
        }
      }) : helper)) + "\" class=\"button link-button copy-clipboard-button\">" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "enroll.mdm.copyLink"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 220
          },
          "end": {
            "line": 1,
            "column": 270
          }
        }
      })) + "</a></li><li>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "enroll.mdm.step.pasteLink"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 283
          },
          "end": {
            "line": 1,
            "column": 339
          }
        }
      })) + "</li><li>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "$1": "<span class='semi-strong'>$1</span>",
          "arguments": "vendor",
          "bundle": "login",
          "code": "enroll.mdm.step.followInstructions"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 348
          },
          "end": {
            "line": 1,
            "column": 473
          }
        }
      })) + "</li><li>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "enroll.mdm.step.relogin"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 482
          },
          "end": {
            "line": 1,
            "column": 536
          }
        }
      })) + "</li></ol>";
    },
    "useData": true
  }),
  getTemplateData: function () {
    return this.options.appState.get('deviceEnrollment');
  },
  postRender: function () {
    // Attach each button to the respective 'data-clipboard-text'
    Clipboard.attach('.copy-clipboard-button').done(() => {
      let notification = new Notification({
        message: loc('enroll.mdm.copyLink.success', 'login'),
        level: 'success'
      });
      this.el.prepend(notification.render().el);
      return false;
    });
  }
});

export { MdmOktaVerifyTerminalView as default };
//# sourceMappingURL=MdmOktaVerifyTerminalView.js.map
