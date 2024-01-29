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
import oktaJQueryStatic from '../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import oktaUnderscore from '../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import Enums from '../../util/Enums.js';
import FormController from '../util/FormController.js';
import FormType from '../util/FormType.js';
import EnduserScopeList from '../views/consent/ScopeList.js';
import ScopeCheckBox from '../views/consent/ScopeCheckBox.js';
import SkipLink from '../views/shared/SkipLink.js';
import consentLogoHeaderTemplate from '../views/shared/templates/consentLogoHeaderTemplate.js';

const granularConsentHeaderTemplate = _Handlebars2.template({
  "1": function (container, depth0, helpers, partials, data) {
    var helper,
        lookupProperty = container.lookupProperty || function (parent, propertyName) {
      if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
        return parent[propertyName];
      }

      return undefined;
    };

    return "<a href=\"" + container.escapeExpression((helper = (helper = lookupProperty(helpers, "clientURI") || (depth0 != null ? lookupProperty(depth0, "clientURI") : depth0)) != null ? helper : container.hooks.helperMissing, typeof helper === "function" ? helper.call(depth0 != null ? depth0 : container.nullContext || {}, {
      "name": "clientURI",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 26
        },
        "end": {
          "line": 1,
          "column": 39
        }
      }
    }) : helper)) + "\" class=\"client-logo-link\" target=\"_blank\">";
  },
  "3": function (container, depth0, helpers, partials, data) {
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

    return "<img class=\"client-logo custom-logo\" src=\"" + alias3((helper = (helper = lookupProperty(helpers, "customLogo") || (depth0 != null ? lookupProperty(depth0, "customLogo") : depth0)) != null ? helper : alias2, typeof helper === "function" ? helper.call(alias1, {
      "name": "customLogo",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 149
        },
        "end": {
          "line": 1,
          "column": 163
        }
      }
    }) : helper)) + "\" alt=\"" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
      "name": "i18n",
      "hash": {
        "bundle": "login",
        "code": "common.logo.alt"
      },
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 170
        },
        "end": {
          "line": 1,
          "column": 216
        }
      }
    })) + "\" aria-hidden=\"true\" />";
  },
  "5": function (container, depth0, helpers, partials, data) {
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

    return "<img class=\"client-logo default-logo\" src=\"" + alias3((helper = (helper = lookupProperty(helpers, "defaultLogo") || (depth0 != null ? lookupProperty(depth0, "defaultLogo") : depth0)) != null ? helper : alias2, typeof helper === "function" ? helper.call(alias1, {
      "name": "defaultLogo",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 290
        },
        "end": {
          "line": 1,
          "column": 305
        }
      }
    }) : helper)) + "\" alt=\"" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
      "name": "i18n",
      "hash": {
        "bundle": "login",
        "code": "common.logo.alt"
      },
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 312
        },
        "end": {
          "line": 1,
          "column": 358
        }
      }
    })) + "\" aria-hidden=\"true\" />";
  },
  "7": function (container, depth0, helpers, partials, data) {
    return "</a>";
  },
  "9": function (container, depth0, helpers, partials, data) {
    var helper,
        lookupProperty = container.lookupProperty || function (parent, propertyName) {
      if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
        return parent[propertyName];
      }

      return undefined;
    };

    return "<div class=\"issuer\"><span>" + container.escapeExpression((helper = (helper = lookupProperty(helpers, "issuer") || (depth0 != null ? lookupProperty(depth0, "issuer") : depth0)) != null ? helper : container.hooks.helperMissing, typeof helper === "function" ? helper.call(depth0 != null ? depth0 : container.nullContext || {}, {
      "name": "issuer",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 624
        },
        "end": {
          "line": 1,
          "column": 634
        }
      }
    }) : helper)) + "</span></div>";
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

    return ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "clientURI") : depth0, {
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
          "column": 89
        }
      }
    })) != null ? stack1 : "") + ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "customLogo") : depth0, {
      "name": "if",
      "hash": {},
      "fn": container.program(3, data, 0),
      "inverse": container.program(5, data, 0),
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 89
        },
        "end": {
          "line": 1,
          "column": 388
        }
      }
    })) != null ? stack1 : "") + ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "clientURI") : depth0, {
      "name": "if",
      "hash": {},
      "fn": container.program(7, data, 0),
      "inverse": container.noop,
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 388
        },
        "end": {
          "line": 1,
          "column": 416
        }
      }
    })) != null ? stack1 : "") + "<h1><span class=\"title-text\">" + container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(alias1, {
      "name": "i18n",
      "hash": {
        "$2": "<p>$2</p>",
        "$1": "<b class='no-translate'>$1</b>",
        "arguments": "appName",
        "bundle": "login",
        "code": "granular.consent.scopes.title"
      },
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 445
        },
        "end": {
          "line": 1,
          "column": 577
        }
      }
    })) + "</span>" + ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "issuer") : depth0, {
      "name": "if",
      "hash": {},
      "fn": container.program(9, data, 0),
      "inverse": container.noop,
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 584
        },
        "end": {
          "line": 1,
          "column": 654
        }
      }
    })) != null ? stack1 : "") + "</h1>";
  },
  "useData": true
});

const getConsentHeader = template => FormType.View({
  View: View.extend({
    className: 'consent-title detail-row',
    template: template,
    getTemplateData: function () {
      const appState = this.options.appState;
      return {
        appName: appState.escape('targetLabel'),
        customLogo: appState.get('targetLogo') && appState.get('targetLogo').href,
        defaultLogo: appState.get('defaultAppLogo'),
        clientURI: appState.get('targetClientURI') && appState.get('targetClientURI').href
      };
    }
  })
});

const consentRequiredDescription = FormType.View({
  View: View.extend({
    className: 'consent-description detail-row',
    template: _Handlebars2.template({
      "compiler": [8, ">= 4.3.0"],
      "main": function (container, depth0, helpers, partials, data) {
        var lookupProperty = container.lookupProperty || function (parent, propertyName) {
          if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
            return parent[propertyName];
          }

          return undefined;
        };

        return "<p>" + container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
          "name": "i18n",
          "hash": {
            "bundle": "login",
            "code": "consent.required.description"
          },
          "data": data,
          "loc": {
            "start": {
              "line": 1,
              "column": 3
            },
            "end": {
              "line": 1,
              "column": 62
            }
          }
        })) + "</p>";
      },
      "useData": true
    })
  })
});
const granularConsentDescription = FormType.View({
  View: View.extend({
    className: 'consent-description',
    template: _Handlebars2.template({
      "compiler": [8, ">= 4.3.0"],
      "main": function (container, depth0, helpers, partials, data) {
        var lookupProperty = container.lookupProperty || function (parent, propertyName) {
          if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
            return parent[propertyName];
          }

          return undefined;
        };

        return "<p>" + container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
          "name": "i18n",
          "hash": {
            "bundle": "login",
            "code": "granular.consent.scopes.description"
          },
          "data": data,
          "loc": {
            "start": {
              "line": 1,
              "column": 3
            },
            "end": {
              "line": 1,
              "column": 69
            }
          }
        })) + "</p>";
      },
      "useData": true
    })
  })
});

const getScopeCheckBoxes = scopes => {
  const sortedScopes = scopes.slice().sort((scope1, scope2) => scope2.optional - scope1.optional);
  return oktaUnderscore.map(sortedScopes, ({
    name: name,
    displayName: displayName,
    description: description,
    optional: optional,
    isCustomized: isCustomized
  }) => FormType.Input({
    name: name,
    input: ScopeCheckBox,
    placeholder: displayName,
    label: false,
    modelType: 'boolean',
    required: true,
    options: {
      description: description,
      optional: optional,
      isCustomized: isCustomized
    }
  }));
};

const isGranularConsent = appState => {
  return 'optional' in appState.get('scopes')[0];
};

var ConsentRequiredController = FormController.extend({
  className: 'consent-required',
  initialize: function () {
    this.model.set('expiresAt', this.options.appState.get('expiresAt'));
    this.model.set('scopes', this.options.appState.get('scopes'));
    this.listenTo(this.form, 'cancel', oktaUnderscore.bind(this.model.cancel, this.model)); // add Skip to main content link

    const skipLink = new SkipLink();
    oktaJQueryStatic(`#${Enums.WIDGET_LOGIN_CONTAINER_ID}`).prepend(skipLink.render().$el);

    if (isGranularConsent(this.options.appState)) {
      this.$el.addClass('granular-consent').removeClass('consent-required');
      this.form.cancel = oktaUnderscore.partial(loc, 'oform.cancel', 'login');

      oktaUnderscore.forEach(this.options.appState.get('scopes'), scope => {
        this.model.set(scope.name, true);
      });
    }
  },
  postRender: function () {
    FormController.prototype.postRender.apply(this, arguments); // Update the "don't allow" and "allow access" buttons to be neutral by changing "allow button" to be gray.

    this.$('.o-form-button-bar .button-primary').removeClass('button-primary');
  },
  Model: {
    props: {
      expiresAt: ['string', true],
      scopes: ['array', true]
    },
    save: function () {
      return this.doTransaction(function (transaction) {
        let scopeNames = oktaUnderscore.pluck(this.get('scopes'), 'name');

        let consent = {
          expiresAt: this.get('expiresAt')
        };

        if (isGranularConsent(this)) {
          consent['optedScopes'] = oktaUnderscore.reduce(scopeNames, (optedScopes, scope) => {
            optedScopes[scope] = this.get(scope);
            return optedScopes;
          }, {});
        } else {
          consent['scopes'] = scopeNames;
        }

        return transaction.consent({
          consent: consent
        });
      });
    },
    cancel: function () {
      const self = this;
      return this.doTransaction(function (transaction) {
        return transaction.cancel();
      }).then(function () {
        const consentCancelFn = self.settings.get('consent.cancel');

        if (oktaUnderscore.isFunction(consentCancelFn)) {
          consentCancelFn();
        }
      });
    }
  },
  Form: {
    noCancelButton: false,
    buttonOrder: ['cancel', 'save'],
    autoSave: true,
    save: oktaUnderscore.partial(loc, 'consent.required.consentButton', 'login'),
    cancel: oktaUnderscore.partial(loc, 'consent.required.cancelButton', 'login'),
    formChildren: function () {
      if (isGranularConsent(this.options.appState)) {
        return [getConsentHeader(granularConsentHeaderTemplate), granularConsentDescription].concat(getScopeCheckBoxes(this.options.appState.get('scopes')));
      } else {
        return [getConsentHeader(consentLogoHeaderTemplate), FormType.View({
          View: new EnduserScopeList({
            model: this.model
          })
        }), consentRequiredDescription];
      }
    }
  },
  Footer: View.extend({
    className: 'consent-footer',
    template: _Handlebars2.template({
      "1": function (container, depth0, helpers, partials, data) {
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

        return "<a class=\"terms-of-service\" href=\"" + alias3((helper = (helper = lookupProperty(helpers, "termsOfService") || (depth0 != null ? lookupProperty(depth0, "termsOfService") : depth0)) != null ? helper : alias2, typeof helper === "function" ? helper.call(alias1, {
          "name": "termsOfService",
          "hash": {},
          "data": data,
          "loc": {
            "start": {
              "line": 1,
              "column": 56
            },
            "end": {
              "line": 1,
              "column": 74
            }
          }
        }) : helper)) + "\" target=\"_blank\">" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
          "name": "i18n",
          "hash": {
            "bundle": "login",
            "code": "consent.required.termsOfService"
          },
          "data": data,
          "loc": {
            "start": {
              "line": 1,
              "column": 92
            },
            "end": {
              "line": 1,
              "column": 154
            }
          }
        })) + "</a>" + ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "privacyPolicy") : depth0, {
          "name": "if",
          "hash": {},
          "fn": container.program(2, data, 0),
          "inverse": container.noop,
          "data": data,
          "loc": {
            "start": {
              "line": 1,
              "column": 158
            },
            "end": {
              "line": 1,
              "column": 194
            }
          }
        })) != null ? stack1 : "");
      },
      "2": function (container, depth0, helpers, partials, data) {
        return " &#8226 ";
      },
      "4": function (container, depth0, helpers, partials, data) {
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

        return "<a class=\"privacy-policy\" href=\"" + alias3((helper = (helper = lookupProperty(helpers, "privacyPolicy") || (depth0 != null ? lookupProperty(depth0, "privacyPolicy") : depth0)) != null ? helper : alias2, typeof helper === "function" ? helper.call(alias1, {
          "name": "privacyPolicy",
          "hash": {},
          "data": data,
          "loc": {
            "start": {
              "line": 1,
              "column": 254
            },
            "end": {
              "line": 1,
              "column": 271
            }
          }
        }) : helper)) + "\" target=\"_blank\">" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
          "name": "i18n",
          "hash": {
            "bundle": "login",
            "code": "consent.required.privacyPolicy"
          },
          "data": data,
          "loc": {
            "start": {
              "line": 1,
              "column": 289
            },
            "end": {
              "line": 1,
              "column": 350
            }
          }
        })) + "</a>";
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

        return ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "termsOfService") : depth0, {
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
              "column": 201
            }
          }
        })) != null ? stack1 : "") + ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "privacyPolicy") : depth0, {
          "name": "if",
          "hash": {},
          "fn": container.program(4, data, 0),
          "inverse": container.noop,
          "data": data,
          "loc": {
            "start": {
              "line": 1,
              "column": 201
            },
            "end": {
              "line": 1,
              "column": 361
            }
          }
        })) != null ? stack1 : "");
      },
      "useData": true
    }),
    getTemplateData: function () {
      const appState = this.options.appState;
      return {
        termsOfService: appState.get('targetTermsOfService') && appState.get('targetTermsOfService').href,
        privacyPolicy: appState.get('targetPrivacyPolicy') && appState.get('targetPrivacyPolicy').href
      };
    }
  })
});

export { ConsentRequiredController as default };
//# sourceMappingURL=ConsentRequiredController.js.map
