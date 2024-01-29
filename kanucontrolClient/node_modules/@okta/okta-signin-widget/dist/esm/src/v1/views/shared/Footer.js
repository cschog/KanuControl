import _Handlebars2 from '../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { View, internal } from '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import oktaUnderscore from '../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';

const {
  Util: Util
} = internal.util;
var Footer = View.extend({
  template: _Handlebars2.template({
    "1": function (container, depth0, helpers, partials, data) {
      var lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<li><a href=\"#\" data-se=\"unlock\" class=\"link js-unlock\">" + container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "unlockaccount"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 440
          },
          "end": {
            "line": 1,
            "column": 484
          }
        }
      })) + "</a></li>";
    },
    "3": function (container, depth0, helpers, partials, data) {
      var stack1,
          helper,
          alias1 = depth0 != null ? depth0 : container.nullContext || {},
          alias2 = container.hooks.helperMissing,
          alias3 = "function",
          alias4 = container.escapeExpression,
          lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<li><a href=\"" + alias4((helper = (helper = lookupProperty(helpers, "href") || (depth0 != null ? lookupProperty(depth0, "href") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "href",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 539
          },
          "end": {
            "line": 1,
            "column": 547
          }
        }
      }) : helper)) + "\" class=\"link js-custom\" rel=\"noopener noreferrer\" " + ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "target") : depth0, {
        "name": "if",
        "hash": {},
        "fn": container.program(4, data, 0),
        "inverse": container.noop,
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 598
          },
          "end": {
            "line": 1,
            "column": 638
          }
        }
      })) != null ? stack1 : "") + ">" + alias4((helper = (helper = lookupProperty(helpers, "text") || (depth0 != null ? lookupProperty(depth0, "text") : depth0)) != null ? helper : alias2, typeof helper === alias3 ? helper.call(alias1, {
        "name": "text",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 639
          },
          "end": {
            "line": 1,
            "column": 647
          }
        }
      }) : helper)) + "</a></li>";
    },
    "4": function (container, depth0, helpers, partials, data) {
      var helper,
          lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "target=\"" + container.escapeExpression((helper = (helper = lookupProperty(helpers, "target") || (depth0 != null ? lookupProperty(depth0, "target") : depth0)) != null ? helper : container.hooks.helperMissing, typeof helper === "function" ? helper.call(depth0 != null ? depth0 : container.nullContext || {}, {
        "name": "target",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 620
          },
          "end": {
            "line": 1,
            "column": 630
          }
        }
      }) : helper)) + "\"";
    },
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
      var stack1,
          helper,
          alias1 = depth0 != null ? depth0 : container.nullContext || {},
          alias2 = container.hooks.helperMissing,
          alias3 = container.escapeExpression,
          lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<a href=\"#\" data-se=\"needhelp\" aria-expanded=\"false\" aria-controls=\"help-links-container\" class=\"link help js-help\">" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "needhelp"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 116
          },
          "end": {
            "line": 1,
            "column": 155
          }
        }
      })) + "</a><ul class=\"help-links js-help-links\" id=\"help-links-container\"><li><a href=\"#\" data-se=\"forgot-password\" class=\"link js-forgot-password\">" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "forgotpassword"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 296
          },
          "end": {
            "line": 1,
            "column": 341
          }
        }
      })) + "</a></li>" + ((stack1 = lookupProperty(helpers, "if").call(alias1, (stack1 = depth0 != null ? lookupProperty(depth0, "features") : depth0) != null ? lookupProperty(stack1, "selfServiceUnlock") : stack1, {
        "name": "if",
        "hash": {},
        "fn": container.program(1, data, 0),
        "inverse": container.noop,
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 350
          },
          "end": {
            "line": 1,
            "column": 500
          }
        }
      })) != null ? stack1 : "") + ((stack1 = lookupProperty(helpers, "each").call(alias1, (stack1 = depth0 != null ? lookupProperty(depth0, "helpLinks") : depth0) != null ? lookupProperty(stack1, "custom") : stack1, {
        "name": "each",
        "hash": {},
        "fn": container.program(3, data, 0),
        "inverse": container.noop,
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 500
          },
          "end": {
            "line": 1,
            "column": 665
          }
        }
      })) != null ? stack1 : "") + "<li><a href=\"" + alias3((helper = (helper = lookupProperty(helpers, "helpLinkUrl") || (depth0 != null ? lookupProperty(depth0, "helpLinkUrl") : depth0)) != null ? helper : alias2, typeof helper === "function" ? helper.call(alias1, {
        "name": "helpLinkUrl",
        "hash": {},
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 678
          },
          "end": {
            "line": 1,
            "column": 693
          }
        }
      }) : helper)) + "\" data-se=\"help-link\" class=\"link js-help-link\" rel=\"noopener noreferrer\" target=\"_blank\">" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "help"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 783
          },
          "end": {
            "line": 1,
            "column": 818
          }
        }
      })) + "</a></li></ul>";
    },
    "useData": true
  }),
  className: 'auth-footer',
  initialize: function () {
    this.listenTo(this.state, 'change:enabled', function (model, enable) {
      this.$('.link').toggleClass('o-form-disabled', !enable);
    });
  },
  getTemplateData: function () {
    let helpLinkUrl;
    const customHelpPage = this.settings.get('helpLinks.help');

    if (customHelpPage) {
      helpLinkUrl = customHelpPage;
    } else {
      helpLinkUrl = _Handlebars2.template({
        "compiler": [8, ">= 4.3.0"],
        "main": function (container, depth0, helpers, partials, data) {
          var helper,
              lookupProperty = container.lookupProperty || function (parent, propertyName) {
            if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
              return parent[propertyName];
            }

            return undefined;
          };

          return container.escapeExpression((helper = (helper = lookupProperty(helpers, "baseUrl") || (depth0 != null ? lookupProperty(depth0, "baseUrl") : depth0)) != null ? helper : container.hooks.helperMissing, typeof helper === "function" ? helper.call(depth0 != null ? depth0 : container.nullContext || {}, {
            "name": "baseUrl",
            "hash": {},
            "data": data,
            "loc": {
              "start": {
                "line": 1,
                "column": 0
              },
              "end": {
                "line": 1,
                "column": 11
              }
            }
          }) : helper)) + "/help/login";
        },
        "useData": true
      })({
        baseUrl: this.settings.get('baseUrl')
      });
    }

    return oktaUnderscore.extend(this.settings.toJSON({
      verbose: true
    }), {
      helpLinkUrl: helpLinkUrl
    });
  },
  postRender: function () {
    this.$('.js-help-links').hide();
  },
  toggleLinks: function (e) {
    e.preventDefault();
    this.$('.js-help-links').slideToggle(200, () => {
      this.$('.js-help').attr('aria-expanded', this.$('.js-help-links').is(':visible'));
    });
  },
  events: {
    'click .js-help': function (e) {
      e.preventDefault();

      if (!this.state.get('enabled')) {
        return;
      }

      this.toggleLinks(e);
    },
    'click .js-forgot-password': function (e) {
      e.preventDefault();

      if (!this.state.get('enabled')) {
        return;
      }

      const customResetPasswordPage = this.settings.get('helpLinks.forgotPassword');

      if (customResetPasswordPage) {
        Util.redirect(customResetPasswordPage);
      } else {
        this.options.appState.trigger('navigate', 'signin/forgot-password');
      }
    },
    'click .js-unlock': function (e) {
      e.preventDefault();

      if (!this.state.get('enabled')) {
        return;
      }

      const customUnlockPage = this.settings.get('helpLinks.unlock');

      if (customUnlockPage) {
        Util.redirect(customUnlockPage);
      } else {
        this.options.appState.trigger('navigate', 'signin/unlock');
      }
    }
  }
});

export { Footer as default };
//# sourceMappingURL=Footer.js.map
