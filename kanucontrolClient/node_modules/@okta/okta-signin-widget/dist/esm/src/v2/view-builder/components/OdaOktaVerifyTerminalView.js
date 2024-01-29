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
import Enums from '../../../util/Enums.js';

const {
  Notification: Notification
} = internal.views.components;
const {
  Clipboard: Clipboard
} = internal.util;

const getDeviceEnrollmentContext = deviceEnrollment => {
  const platform = (deviceEnrollment.platform || '').toLowerCase();
  const challengeMethod = deviceEnrollment.challengeMethod;
  const enrollmentType = deviceEnrollment.name;
  const isIOS = platform === Enums.IOS;
  const isAndroidAppLink = platform === Enums.ANDROID && challengeMethod === 'APP_LINK';
  const isAndroidLoopback = platform === Enums.ANDROID && challengeMethod === 'LOOPBACK';
  return {
    signInUrl: deviceEnrollment.signInUrl,
    isIOS: isIOS,
    enrollmentType: enrollmentType,
    isAndroidLoopback: isAndroidLoopback,
    isAndroidAppLink: isAndroidAppLink,
    appStoreLink: isIOS ? Enums.OKTA_VERIFY_APPLE_APP_STORE_URL : Enums.OKTA_VERIFY_GOOGLE_PLAY_STORE_URL,
    orgName: deviceEnrollment.orgName
  };
};

const BaseOdaOktaVerifyTerminalView = View.extend({
  getTemplateData: function () {
    const deviceEnrollment = this.options.appState.get('deviceEnrollment');
    return getDeviceEnrollmentContext(deviceEnrollment);
  }
});
const IosAndAndroidLoopbackOdaTerminalView = BaseOdaOktaVerifyTerminalView.extend({
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

      return "<li>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "enroll.mdm.step.copyLink"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 120
          },
          "end": {
            "line": 1,
            "column": 175
          }
        }
      })) + "<a data-clipboard-text=\"" + alias3((helper = (helper = lookupProperty(helpers, "appStoreLink") || (depth0 != null ? lookupProperty(depth0, "appStoreLink") : depth0)) != null ? helper : alias2, typeof helper === "function" ? helper.call(alias1, {
        "name": "appStoreLink",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 199
          },
          "end": {
            "line": 1,
            "column": 215
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
            "column": 266
          },
          "end": {
            "line": 1,
            "column": 316
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
            "column": 329
          },
          "end": {
            "line": 1,
            "column": 385
          }
        }
      })) + "</li><li>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "enroll.oda.step3"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 394
          },
          "end": {
            "line": 1,
            "column": 441
          }
        }
      })) + "</li>";
    },
    "3": function (container, depth0, helpers, partials, data) {
      var lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<li>" + container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "enroll.oda.android.step1"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 482
          },
          "end": {
            "line": 1,
            "column": 537
          }
        }
      })) + "</li>";
    },
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
      var stack1,
          helper,
          alias1 = depth0 != null ? depth0 : container.nullContext || {},
          alias2 = container.hooks.helperMissing,
          alias3 = container.escapeExpression,
          alias4 = "function",
          lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<p class=\"explanation\" data-se=\"subheader\">" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "enroll.explanation.p1"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 43
          },
          "end": {
            "line": 1,
            "column": 95
          }
        }
      })) + "</p><ol>" + ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "isIOS") : depth0, {
        "name": "if",
        "hash": {},
        "fn": container.program(1, data, 0),
        "inverse": container.noop,
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 103
          },
          "end": {
            "line": 1,
            "column": 453
          }
        }
      })) != null ? stack1 : "") + ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "isAndroidLoopback") : depth0, {
        "name": "if",
        "hash": {},
        "fn": container.program(3, data, 0),
        "inverse": container.noop,
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 453
          },
          "end": {
            "line": 1,
            "column": 549
          }
        }
      })) != null ? stack1 : "") + "<li>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "enroll.oda.step1"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 553
          },
          "end": {
            "line": 1,
            "column": 600
          }
        }
      })) + "</li><li>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "enroll.oda.step2"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 609
          },
          "end": {
            "line": 1,
            "column": 656
          }
        }
      })) + "<p class=\"org-signin-link\"><span class=\"no-translate\">" + alias3((helper = (helper = lookupProperty(helpers, "signInUrl") || (depth0 != null ? lookupProperty(depth0, "signInUrl") : depth0)) != null ? helper : alias2, typeof helper === alias4 ? helper.call(alias1, {
        "name": "signInUrl",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 710
          },
          "end": {
            "line": 1,
            "column": 723
          }
        }
      }) : helper)) + "</span></p><a data-clipboard-text=\"" + alias3((helper = (helper = lookupProperty(helpers, "signInUrl") || (depth0 != null ? lookupProperty(depth0, "signInUrl") : depth0)) != null ? helper : alias2, typeof helper === alias4 ? helper.call(alias1, {
        "name": "signInUrl",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 758
          },
          "end": {
            "line": 1,
            "column": 771
          }
        }
      }) : helper)) + "\" class=\"button link-button copy-org-clipboard-button\">" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "enroll.oda.org.copyLink"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 826
          },
          "end": {
            "line": 1,
            "column": 880
          }
        }
      })) + "</a></li><li>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "enroll.oda.step6"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 893
          },
          "end": {
            "line": 1,
            "column": 940
          }
        }
      })) + "</li></ol>";
    },
    "useData": true
  }),
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
    Clipboard.attach('.copy-org-clipboard-button').done(() => {
      let notification = new Notification({
        message: loc('enroll.oda.org.copyLink.success', 'login'),
        level: 'success'
      });
      this.el.prepend(notification.render().el);
      return false;
    });
  }
});
const AndroidAppLinkWithAccountOdaTerminalView = BaseOdaOktaVerifyTerminalView.extend({
  template: _Handlebars2.template({
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
      var alias1 = depth0 != null ? depth0 : container.nullContext || {},
          alias2 = container.hooks.helperMissing,
          alias3 = container.escapeExpression,
          lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<p class=\"explanation\" data-se=\"subheader\">" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "enroll.oda.with.account.explanation"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 43
          },
          "end": {
            "line": 1,
            "column": 109
          }
        }
      })) + "</p><p class=\"subtitle\">" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "enroll.oda.with.account.subtitile1 "
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 133
          },
          "end": {
            "line": 1,
            "column": 199
          }
        }
      })) + "</p><ul><li>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "enroll.oda.with.account.step1"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 211
          },
          "end": {
            "line": 1,
            "column": 271
          }
        }
      })) + "</li><li>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "enroll.oda.with.account.step2"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 280
          },
          "end": {
            "line": 1,
            "column": 340
          }
        }
      })) + "</li><li>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "enroll.oda.with.account.step3"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 349
          },
          "end": {
            "line": 1,
            "column": 409
          }
        }
      })) + "</li></ul><p class=\"subtitle\">" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "enroll.oda.with.account.subtitile2 "
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 439
          },
          "end": {
            "line": 1,
            "column": 505
          }
        }
      })) + "</p><ol><li>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "enroll.oda.with.account.step4"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 517
          },
          "end": {
            "line": 1,
            "column": 577
          }
        }
      })) + "</li><li>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "arguments": "signInUrl",
          "bundle": "login",
          "code": "enroll.oda.with.account.step5"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 586
          },
          "end": {
            "line": 1,
            "column": 668
          }
        }
      })) + "</li><li>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "enroll.oda.with.account.step6"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 677
          },
          "end": {
            "line": 1,
            "column": 737
          }
        }
      })) + "</li><li>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "enroll.oda.with.account.step7"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 746
          },
          "end": {
            "line": 1,
            "column": 806
          }
        }
      })) + "</li></ol>";
    },
    "useData": true
  })
});
const AndroidAppLinkWithoutAccountOdaTerminalView = BaseOdaOktaVerifyTerminalView.extend({
  template: _Handlebars2.template({
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
      var stack1,
          helper,
          alias1 = depth0 != null ? depth0 : container.nullContext || {},
          alias2 = container.hooks.helperMissing,
          alias3 = container.escapeExpression,
          alias4 = "function",
          lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<p class=\"explanation\" data-se=\"subheader\">" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "enroll.oda.without.account.explanation"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 43
          },
          "end": {
            "line": 1,
            "column": 112
          }
        }
      })) + "</p><ol><li>" + ((stack1 = (lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "arguments": "appStoreLink",
          "bundle": "login",
          "code": "enroll.oda.without.account.step1"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 124
          },
          "end": {
            "line": 1,
            "column": 214
          }
        }
      })) != null ? stack1 : "") + "</li><li>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "enroll.oda.step1"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 223
          },
          "end": {
            "line": 1,
            "column": 270
          }
        }
      })) + "</li><li>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "enroll.oda.step2"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 279
          },
          "end": {
            "line": 1,
            "column": 326
          }
        }
      })) + "<p class=\"org-signin-link\"><span class=\"no-translate\">" + alias3((helper = (helper = lookupProperty(helpers, "signInUrl") || (depth0 != null ? lookupProperty(depth0, "signInUrl") : depth0)) != null ? helper : alias2, typeof helper === alias4 ? helper.call(alias1, {
        "name": "signInUrl",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 380
          },
          "end": {
            "line": 1,
            "column": 393
          }
        }
      }) : helper)) + "</span></p><a data-clipboard-text=\"" + alias3((helper = (helper = lookupProperty(helpers, "signInUrl") || (depth0 != null ? lookupProperty(depth0, "signInUrl") : depth0)) != null ? helper : alias2, typeof helper === alias4 ? helper.call(alias1, {
        "name": "signInUrl",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 428
          },
          "end": {
            "line": 1,
            "column": 441
          }
        }
      }) : helper)) + "\" class=\"button link-button copy-org-clipboard-button\">" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "enroll.oda.org.copyLink"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 496
          },
          "end": {
            "line": 1,
            "column": 550
          }
        }
      })) + "</a></li><li>" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "enroll.oda.without.account.step4"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 563
          },
          "end": {
            "line": 1,
            "column": 626
          }
        }
      })) + "</li></ol>";
    },
    "useData": true
  }),
  postRender: function () {
    // Attach each button to the respective 'data-clipboard-text'
    Clipboard.attach('.copy-org-clipboard-button').done(() => {
      let notification = new Notification({
        message: loc('enroll.oda.org.copyLink.success', 'login'),
        level: 'success'
      });
      this.el.prepend(notification.render().el);
      return false;
    });
  }
});

export { AndroidAppLinkWithAccountOdaTerminalView, AndroidAppLinkWithoutAccountOdaTerminalView, IosAndAndroidLoopbackOdaTerminalView, getDeviceEnrollmentContext };
//# sourceMappingURL=OdaOktaVerifyTerminalView.js.map
